/*
Copyright (C) 2016 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

/**
 * A general execution engine.
 */

package org.harctoolbox.jgirs;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.IrpUtils;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ICommandLineDevice;
import org.harctoolbox.harchardware.IHarcHardware;
import org.harctoolbox.harchardware.ir.ICapture;
import org.harctoolbox.harchardware.ir.IReceive;
import org.harctoolbox.harchardware.ir.ITransmitter;
import org.harctoolbox.readlinecommander.ReadlineCommander;
import org.xml.sax.SAXException;

public final class Engine implements ICommandLineDevice, Closeable {
    private static JCommander argumentParser;
    private static final CommandLineArgs commandLineArgs = new CommandLineArgs();

    public static IHarcHardware newHardware(String hardwareName, ArrayList<String> parameters,
            int openDelay, boolean verbose, int debug)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        Class<?>[] classArray = new Class<?>[parameters.size()];
        Object[] objectArray = new Object[parameters.size()];
        int index = 0;
        for (String p : parameters) {
            HardwareParameter parameter = new HardwareParameter(p);
            classArray[index] = parameter.getClazz();
            objectArray[index] = parameter.getObject();
        }
        return newHardware(hardwareName, classArray, objectArray, openDelay, verbose, debug);
    }

    public static IHarcHardware newHardware(String hardwareName, Class<?>[] classArray, Object[] objectArray,
            int openDelay, boolean verbose, int debug) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        String fullHardwareClassName = (hardwareName.contains(".") ? "" : "org.harctoolbox.harchardware.ir.") + hardwareName;
        Class<?> clazz = Class.forName(fullHardwareClassName);

        //@SuppressWarnings("unchecked")
        Constructor<?> constructor = clazz.getConstructor(classArray);
        IHarcHardware hw = (IHarcHardware) constructor.newInstance(objectArray);
        System.err.print("Trying to open hardware...");
        hw.open();
        System.err.println("done");
        if (openDelay > 0) {
            try {
                Thread.sleep(commandLineArgs.openDelay);
            } catch (InterruptedException ex) {
            }
        }
        hw.setVerbosity(verbose);
        hw.setDebug(debug);
        return hw;
    }

    private static void usage(int exitcode) {
        StringBuilder str = new StringBuilder(128);
        argumentParser.usage(str);

        (exitcode == IrpUtils.exitSuccess ? System.out : System.err).println(str);
        doExit(exitcode); // placifying FindBugs...
    }

    private static void doExit(int exitcode) {
        System.exit(exitcode);
    }

    private static String join(Iterable<String> strings) {
        StringBuilder str = new StringBuilder(128);
        for (String s : strings) {
            str.append(" ").append(s);
        }
        return str.substring(1);
    }

    private final HashMap<String, Module> modules;
    private final Base base;
    private final Parameters parameters;
    private final CommandExecuter commandExecuter;
    private final IHarcHardware hardware;
    private final Charset charSet;
    private boolean verbosity;
    private int debug;
    private final NamedRemotes namedCommand;
    private final List<String> outBuffer;

    public Engine(IHarcHardware hardware, Renderer renderer, Irp irp, NamedRemotes namedCommand, String charsetName, boolean verbosity, int debug) throws FileNotFoundException, IncompatibleArgumentException {
        this.outBuffer = new ArrayList<>(8);
        this.verbosity = verbosity;
        this.debug = debug;
        this.charSet = Charset.forName(charsetName);
        this.hardware = hardware;
        this.namedCommand = namedCommand;
        modules = new LinkedHashMap<>(16);
        commandExecuter = new CommandExecuter();
        parameters = new Parameters();
        registerModule(parameters);
        base = new Base(modules, commandExecuter, hardware);
        registerModule(base);
        registerModule(new Dummy());
        if (renderer != null)
            registerModule(renderer);
        if (irp != null)
            registerModule(irp);
        if (namedCommand != null)
            registerModule(namedCommand);
        if (ITransmitter.class.isInstance(hardware))
            registerModule(new Transmitters((ITransmitter) hardware));
        if (ICapture.class.isInstance(hardware))
            registerModule(new Capture((ICapture) hardware));
        if (IReceive.class.isInstance(hardware))
            registerModule(new Receive((IReceive)hardware, namedCommand));
        registerModule(Transmit.newTransmit(hardware, renderer, irp, namedCommand));
    }

    @Override
    public void flushInput() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void registerModule(Module module) {
        if (module == null)
            return;
        modules.put(module.getName(), module);
        module.getCommands().stream().forEach((cmd) -> {
            commandExecuter.addCommand(cmd);
        });
        //parameters.addParameters(module);
        module.addParametersTo(parameters);
    }

    public ArrayList<String> getModuleNames(boolean sort) {
        ArrayList<String> moduleNames = new ArrayList<>(modules.keySet());
        if (sort)
            Collections.sort(moduleNames);

        return moduleNames;
    }

    @Override
    public synchronized void sendString(String cmd) throws IOException {
        try {
              outBuffer.addAll(eval(cmd));
                    //for (String str : result)
                    //    out.println(str);
            } catch (JGirsException ex) {
                outBuffer.add("ERROR: " + ex.getMessage());
                //if (NoSuchCommandException.class.isInstance(ex)
                //        || AmbigousCommandException.class.isInstance(ex))
                //    err.println("Commands are: " + Base.join(commandExecuter.getCommandNames(true)));
            } catch (IOException | HarcHardwareException | IrpMasterException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    @Override
    public String readString() throws IOException {
        return readString(false);
    }

    @Override
    public synchronized String readString(boolean wait) throws IOException {
        if (outBuffer.size() > 0) {
            String line = outBuffer.get(0);
            outBuffer.remove(0);
            return line;
        } else
            return null;
    }

    @Override
    public boolean ready() throws IOException {
        return outBuffer.size() > 0; // ??
    }

    @Override
    public String getVersion() throws IOException {
        return hardware.getVersion(); // FIXME
    }

    @Override
    public void setVerbosity(boolean verbosity) {
        this.verbosity = verbosity;
        hardware.setVerbosity(verbosity);
    }

    @Override
    public void setDebug(int debug) {
        this.debug = debug;
        hardware.setDebug(debug);
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        hardware.setTimeout(timeout);
    }

    @Override
    public boolean isValid() {
        return true; //???
    }

    @Override
    public void open() throws HarcHardwareException, IOException {
        if (hardware != null)
            hardware.open();// ???
    }

    @Override
    public void close() throws IOException {
        if (hardware != null)
            hardware.close();
    }

    /*class ReadlineCommander implements ICommander {
        //private final String prompt;
        private final String historyFile;

        ReadlineCommander() {
            this(null, null);
        }

        ReadlineCommander(String confFile, final String historyFile) {
            //this.prompt = prompt;
            this.historyFile = historyFile;

            try {
                Readline.load(ReadlineLibrary.GnuReadline);
                Readline.initReadline(Version.appName);
                if (confFile != null && new File(confFile).exists())
                    try {
                        Readline.readInitFile(confFile);
                    } catch (IOException ex) {
                        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                if (historyFile != null && new File(historyFile).exists())
                    try {
                        Readline.readHistoryFile(historyFile);
                    } catch (EOFException | UnsupportedEncodingException ex) {
                        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                    }
            } catch (UnsatisfiedLinkError ignore_me) {
                Logger.getLogger(Engine.class.getName()).log(Level.INFO, "couldn't load readline lib. Using simple stdin.");
            }

            /*Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Readline.writeHistoryFile(historyFile);
                    } catch (EOFException | UnsupportedEncodingException ex) {
                        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Logger.getLogger(Engine.class.getName()).log(Level.FINE, "Readline.cleanup called");
                    Readline.cleanup();
                }
            });* /
        }

        @Override
        public String readline() throws IOException {
            String line;
            try {
                line = Readline.readline(prompt, false);
                int size = Readline.getHistorySize();
                if ((line != null && !line.isEmpty())
                        && (size == 0 || !line.equals(Readline.getHistoryLine(size-1))))
                    Readline.addToHistory(line);
            } catch (EOFException ex) {
                return null;
            }
            return line == null ? "" : line;
        }

        @Override
        public void close() throws IOException {
            if (historyFile != null)
                Readline.writeHistoryFile(historyFile);
            Readline.cleanup();
        }
    }

    class ReaderCommander implements ICommander {
        private final BufferedReader reader;
        private final PrintStream out;

        ReaderCommander() {
            this(System.in, charSet, System.out, prompt);
        }

        ReaderCommander(InputStream inputStream, Charset charset, PrintStream out, String prompt) {
            this(new InputStreamReader(inputStream, charSet), out, prompt);
        }

        ReaderCommander(Reader reader, PrintStream out, String prompt) {
            this.reader = new BufferedReader(reader);
            this.out = out;
        }

        @Override
        public String readline() throws IOException {
            out.print(prompt);
            return reader.readLine();
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }
    }

    public boolean readEvalPrint(boolean joinLines) {
        return readEvalPrint(System.in, System.out, System.err, joinLines);
    }

    public boolean readEvalPrint(String initFile, String historyFile, boolean joinLines) {
        return readEvalPrint(new ReadlineCommander(initFile, historyFile), System.out, System.err, joinLines);
    }

    public boolean readEvalPrint(Reader in, PrintStream out, PrintStream err, boolean joinLines) {
        return readEvalPrint(new ReaderCommander(in, out, prompt), out, err, joinLines);
    }

    public boolean readEvalPrint(InputStream in, PrintStream out, PrintStream err, boolean joinLines) {
        return readEvalPrint(new InputStreamReader(in, charSet), out, err, joinLines);
    }

    private boolean readEvalPrint(ICommander commander, PrintStream out, PrintStream err, boolean joinLines) {
        while (!isQuitRequested()) {
            String line = null;
            try {
                line = commander.readline();
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (line == null) { // EOF
                out.println();
                break;
            }

            if (line.isEmpty())
                continue;

            try {
                List<String> result = eval(line);
                if (joinLines)
                    out.println(Module.join(result));
                else
                    for (String str : result)
                        out.println(str);
            } catch (JGirsException ex) {
                err.println("ERROR: " + ex.getMessage());
                if (NoSuchCommandException.class.isInstance(ex)
                        || AmbigousCommandException.class.isInstance(ex))
                    err.println("Commands are: " + Base.join(commandExecuter.getCommandNames(true)));
            } catch (IOException | HarcHardwareException | IrpMasterException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                // Something really unexpected happened
                ex.printStackTrace();
            }
        }
        return isQuitRequested();
    }*/

    public ArrayList<String> getCommandNames(boolean sort) {
        return commandExecuter.getCommandNames(sort);
    }

    public ArrayList<String> getSubCommandNames(String command, boolean sort) {
        return commandExecuter.getSubCommandNames(command, sort);
    }

//    private boolean isQuitRequested() {
//        return base.isQuitRequested();
//    }

    private List<String> eval(String command) throws JGirsException, IOException, HarcHardwareException, IrpMasterException {
        String[] tokens = command.split("\\s+");
        return commandExecuter.exec(Arrays.asList(tokens));
    }

    private static class HardwareParameter {
        private Class<?> clazz;
        private String svalue;
        private int ivalue;
        private boolean bvalue;
        private Object object;

        HardwareParameter(String par) {
            String[] p = par.split(":");
            if (p.length == 1) {
                clazz = String.class;
                svalue = par;
            } else {
                switch (p[0]) {
                    case "string":
                        clazz = String.class;
                        svalue = p[1];
                        object = svalue;
                        break;
                    case "int":
                        clazz = int.class;
                        ivalue = Integer.parseInt(p[1]);
                        object = ivalue;
                        break;
                    case "bool":
                    case "boolean":
                        clazz = boolean.class;
                        bvalue = Boolean.parseBoolean(p[1]);
                        object = bvalue;
                        break;
                    default:
                        throw new IllegalArgumentException("Type " + p[0] + " unknown");
                }
            }
        }

        /**
         * @return the clazz
         */
        public Class<?> getClazz() {
            return clazz;
        }

        /**
         * @return the svalue
         */
        public String getSvalue() {
            return svalue;
        }

        /**
         * @return the ivalue
         */
        public int getIvalue() {
            return ivalue;
        }

        /**
         * @return the bvalue
         */
        public boolean isBvalue() {
            return bvalue;
        }

        /**
         * @return the object
         */
        public Object getObject() {
            return object;
        }
    }

    private final static class CommandLineArgs {

        @Parameter(names = {"-1"}, description = "Expect one line of response")
        private boolean oneLine;

        @Parameter(names = {"-2"}, description = "Expect two line of response")
        private boolean twoLines;

        @Parameter(names = {"--appname"}, description = "Appname for finding resourses")
        private String appName = "JGirs";

        @Parameter(names = {"--charset"}, description = "Charset for inputs")
        private String charSet = "US-ASCII";

        @Parameter(names = {"-D", "--debug"}, description = "Debug code")
        private int debug = 0;

        @Parameter(names = {"-g", "--girr"}, description = "Remotes and commands in Girr files")
        private List<String> girr = new ArrayList<>(8);

        @Parameter(names = {"--help", "-?"}, description = "Display help message")
        private boolean helpRequested = false;

        @Parameter(names = {"-h", "--hardware"}, description = "Driver hardware")
        private String hardware = null;

        @Parameter(names = {"--historyfile"}, description = "History file for readline")
        private String historyfile = System.getProperty("user.home") + File.separator + ".jgirs.history";

        @Parameter(names = {"-i", "--irpprotocolsini"}, description = "IrpMaster protocol file")
        private String irpMasterIni = null;//"IrpProtocols.ini";

        @Parameter(names = {"--initfile"}, description = "Initfile for readline")
        private String initfile = null;//System.getProperty("user.home") + File.separator + ".jgirs";

        @Parameter(names = {      "--prompt"}, description = "Interactive prompt for readline")
        private String prompt = "$$> ";

        //@Parameter(names = {"-r", "--readline"}, description = "Use the readline library, if available")
        //private boolean readLine; // TODO: must not be combined with --command

        @Parameter(names = {"-V", "--version"}, description = "Display version information")
        private boolean versionRequested;

        @Parameter(names = {"-v", "--verbose"}, description = "Have some commands executed verbosely")
        private boolean verbose;

        @Parameter(names = {"-w", "--waitforanswer"}, description = "Microseconds to wait for answer")
        private int waitForAnswer;

        @Parameter(names = {"-#", "--count"}, description = "Number of times to send sequence")
        private int count = 1;

        @Parameter(names = {"-b", "--baud"}, description = "Baud rate for the serial port")
        private int baud = 115200; //9600;

        @Parameter(names = {"--delay"}, description = "Delay between commands in milliseconds")
        private int delay = 0;

        @Parameter(names = {"-d", "--device"}, description = "Device name for serial device")
        private String device = null;

        //@Parameter(names = {"-g", "--globalcache"}, description = "Use GlobalCache")
        //private boolean globalcache = false;

        //@Parameter(names = {"--http", "--url"}, description = "Use URLs (http)")
        //private boolean url = false;

        @Parameter(names = {      "--ip"}, description = "IP address or name")
        private String ip = null;

        @Parameter(names = {      "--opendelay"}, description = "Delay after opening, in milliseconds")
        private int openDelay = 0;

        //@Parameter(names = {"-m", "--myip"}, description = "For UPD only: IP number to listen to")
        //private String myIp = null;

        @Parameter(names = {"-p", "--port"}, description = "Port number, either TCP port number, or serial port number (counting the first as 1).")
        private int portNumber = (int) IrpUtils.invalid;

        @Parameter(names = {"--prefix"}, description = "Prefix to be prepended to all sent commands.")
        private String prefix = "";

        @Parameter(names = {"-n", "--newline"}, description = "Append a newline at the end of the command.")
        private boolean appendNewline;

        @Parameter(names = {"-r", "--return"}, description = "Append a carrage return at the end of the command.")
        private boolean appendReturn;

        @Parameter(names = {"-s", "--serial"}, description = "Use local serial port.")
        private boolean serial;

        @Parameter(names = {"--suffix"}, description = "Sufffix to be appended to all sent commands.")
        private String suffix = "";

        @Parameter(names = {"-t", "--tcp"}, description = "Use tcp sockets")
        private boolean tcp;

        @Parameter(names = {"-T", "--timeout"}, description = "Timeout in milliseconds")
        private int timeout = 15000;

        @Parameter(names = {"-u", "--upper"}, description = "Translate commands to upper case.")
        private boolean toUpper;

        //@Parameter(names = {"--udp"}, description = "Use Udp sockets.")
        //private boolean udp;

        @Parameter(description = "[parameters]")
        private ArrayList<String> parameters = new ArrayList<>(8);
    }

   /**
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(Version.appName);

        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.exitUsageError);
        }

        if (commandLineArgs.helpRequested)
            usage(IrpUtils.exitSuccess);

        if (commandLineArgs.versionRequested) {
            System.out.println(Version.versionString);
            doExit(IrpUtils.exitSuccess);
        }

        IHarcHardware hardware = null;
        try {
            if (commandLineArgs.hardware == null) {
                System.err.println("Warning: no hardware given, very limited functionallity available.");
                hardware = null;
            } else {
                Class<?>[] classArray = new Class<?>[0];
                Object[] objectArray = new Object[0];
                if (commandLineArgs.serial) {
                    classArray = new Class<?>[] { String.class, int.class };
                    objectArray = new Object[] { commandLineArgs.device, commandLineArgs.baud };
                }
                hardware = newHardware(commandLineArgs.hardware, classArray, objectArray,
                        commandLineArgs.openDelay, commandLineArgs.verbose, commandLineArgs.debug);
                System.out.println("Hardware version: " + hardware.getVersion());
            }
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        Renderer renderer = null;
        if (commandLineArgs.irpMasterIni == null) {
            System.err.println("Warning: No IrpMaster.ini file, rendering will not be available.");
        } else {
            try {
                renderer = new Renderer(commandLineArgs.irpMasterIni);
            } catch (FileNotFoundException | IncompatibleArgumentException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        NamedRemotes namedRemotes = null;
        if (commandLineArgs.girr == null || commandLineArgs.girr.isEmpty()) {
            System.err.println("Warning: no remotes");
        } else {
            try {
                namedRemotes = new NamedRemotes(commandLineArgs.girr);
            } catch (ParserConfigurationException | SAXException | IOException | IrpMasterException | ParseException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int returnLines = -1; //commandLineArgs.oneLine ? 1 : commandLineArgs.twoLines ? 2 : 0;
        try (Engine engine = new Engine(hardware, renderer, new Irp(), namedRemotes,
                commandLineArgs.charSet, commandLineArgs.verbose, commandLineArgs.debug)) {
            if (commandLineArgs.parameters.isEmpty()) {
                // read-eval-print
                //FramedDevice.Framer framer = new FramedDevice.Framer();
                /*        commandLineArgs.prefix + "{0}" + commandLineArgs.suffix
                + (commandLineArgs.appendReturn ? "\r" : "")
                + (commandLineArgs.appendNewline ? "\n" : ""),
                commandLineArgs.toUpper);*/

                //FramedDevice framedDevice = new FramedDevice((ICommandLineDevice) hardware, framer);
                //FramedDevice framedDevice = new FramedDevice(engine, framer);
                ReadlineCommander.init(commandLineArgs.initfile, commandLineArgs.historyfile,
                        commandLineArgs.prompt, commandLineArgs.appName);
                ReadlineCommander.readEvalPrint(engine, commandLineArgs.waitForAnswer, returnLines);
                ReadlineCommander.close();

            } else {
                // shoot one command
                try {
                    for (int i = 0; i < commandLineArgs.count; i++) {
                        List<String> result = engine.eval(join(commandLineArgs.parameters));
                        System.out.println(result == null ? "FAIL" : result.isEmpty() ? "SUCCESS" : result);
                    }
                } catch (IOException | HarcHardwareException | IrpMasterException | JGirsException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        } catch (IncompatibleArgumentException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(3);
        }
    }
}
