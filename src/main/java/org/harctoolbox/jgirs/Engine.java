/*
Copyright (C) 2015 Bengt Martensson.

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
 * An general execution engine.
 */

package org.harctoolbox.jgirs;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.gnu.readline.Readline;
import org.gnu.readline.ReadlineLibrary;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.IrpUtils;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.harctoolbox.harchardware.ir.ICapture;
import org.harctoolbox.harchardware.ir.IReceive;
import org.harctoolbox.harchardware.ir.ITransmitter;
import org.xml.sax.SAXException;

public class Engine implements ICommandLine {

    private HashMap<String, Module> modules;
    private Base base;
    private Parameters parameters;
    private CommandExecuter commandExecuter;
    private IHarcHardware hardware;
    private Charset charSet;
    private boolean verbose;
    private int debug;
    private String appName;
    private String prompt;
    private String historyFile = ".girs.history";
    private final NamedRemotes namedCommand;

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

    private void registerModule(Module module) {
        if (module == null)
            return;
        modules.put(module.getName(), module);
        for (ICommand cmd : module.getCommands()) {
            commandExecuter.addCommand(cmd);
        }
        parameters.addParameters(module);
    }

    public static IHarcHardware newHardware(String hardwareName, ArrayList<String> parameters, boolean verbose, int debug) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        Class<?> clazz = Class.forName(hardwareName);
        Class<?>[] classArray = new Class<?>[parameters.size()];
        Object[] objectArray = new Object[parameters.size()];
        int index = 0;
        for (String p : parameters) {
            HardwareParameter parameter = new HardwareParameter(p);
            classArray[index] = parameter.getClazz();
            objectArray[index] = parameter.getObject();
        }

        @SuppressWarnings("unchecked")
        Constructor<?> constructor = clazz.getConstructor(classArray);
        IHarcHardware hw = (IHarcHardware) constructor.newInstance(objectArray);
        hw.open();
        hw.setVerbosity(verbose);
        hw.setDebug(debug);
        return hw;
    }

    public Engine(IHarcHardware hardware, Renderer renderer, Irp irp, NamedRemotes namedCommand, String appName, String charsetName, String prompt, boolean verbose, int debug) throws FileNotFoundException, IncompatibleArgumentException {
        this.appName = appName;
        this.verbose = verbose;
        this.debug = debug;
        this.charSet = Charset.forName(charsetName);
        this.hardware = hardware;
        this.namedCommand = namedCommand;
        this.prompt = prompt;
        modules = new LinkedHashMap<>();
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

    public ArrayList<String> getModuleNames(boolean sort) {
        ArrayList<String> moduleNames = new ArrayList<>(modules.keySet());
        if (sort)
            Collections.sort(moduleNames);

        return moduleNames;
    }

    interface ICommander extends Closeable {
        /**
         *
         * @return line. Empty ("") if empty line, null if EOF.
         * @throws IOException
         */
        public String readline() throws IOException;
    }

    class ReadlineCommander implements ICommander {
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
            });*/
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
    }

    @Override
    public ArrayList<String> getCommandNames(boolean sort) {
        return commandExecuter.getCommandNames(sort);
    }

    @Override
    public ArrayList<String> getSubCommandNames(String command, boolean sort) {
        return commandExecuter.getSubCommandNames(command, sort);
    }

    @Override
    public boolean isQuitRequested() {
        return base.isQuitRequested();
    }

    @Override
    public List<String> eval(String command) throws JGirsException, IOException, HarcHardwareException, IrpMasterException {
        String[] tokens = command.split("\\s+");
        return commandExecuter.exec(tokens);
    }

    private static void usage(int exitcode) {
        StringBuilder str = new StringBuilder();
        argumentParser.usage(str);

        (exitcode == IrpUtils.exitSuccess ? System.out : System.err).println(str);
        doExit(exitcode); // placifying FindBugs...
    }

    private static void doExit(int exitcode) {
        System.exit(exitcode);
    }

    @Override
    public String getAppName() {
        return appName;
    }

    /**
     * @return the prompt
     */
    @Override
    public String getPrompt() {
        return prompt;
    }

    /**
     * @param prompt the prompt to set
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * @return the historyFile
     */
    @Override
    public String getHistoryFile() {
        return historyFile;
    }

    /**
     * @param historyFile the historyFile to set
     */
    public void setHistoryFile(String historyFile) {
        this.historyFile = historyFile;
    }

    private final static class CommandLineArgs {

        @Parameter(names = {"--appname"}, description = "Appname for finding resourses")
        private String appName = "JGirs";

        @Parameter(names = {"--charset"}, description = "Charset for inputs")
        private String charSet = "US-ASCII";

        @Parameter(names = {"-c", "--command"}, description = "Command to execute instead of entering a loop")
        private String command = null;

        @Parameter(names = {"-D", "--debug"}, description = "Debug code")
        private int debug = 0;

        @Parameter(names = {"-g", "--girr"}, description = "Remotes and commands in Girr files")
        private List<String> girr = new ArrayList<>();

        @Parameter(names = {"--help", "-?"}, description = "Display help message")
        private boolean helpRequested = false;

        @Parameter(names = {"-h", "--hardware"}, description = "Driver hardware")
        private String hardware = null;

        @Parameter(names = {"--historyfile"}, description = "History file for readline")
        private String historyfile = System.getProperty("user.home") + File.separator + ".jgirs.history";

        @Parameter(names = {"-i", "--irpprotocolsini"}, description = "IrpMaster protocol file")
        private String irpMasterIni = null;//"IrpProtocols.ini";

        @Parameter(names = {"--initfile"}, description = "Initfile for readline")
        private String initfile = System.getProperty("user.home") + File.separator + ".jgirs";

        @Parameter(names = {"-r", "--readline"}, description = "Use readline")
        private boolean readline = false;

        //@Parameter(names = {"--stringarg1"}, description = "First string argument to hardware")
        //private String hwsarg1 = null;

        //@Parameter(names = {"-H", "--home", "--applicationhome", "--apphome"}, description = "Set application home (where files are located)")
        //private String applicationHome = null;

        //@Parameter(names = {"-p", "--properties"}, description = "Pathname of properties file")
        //private String propertiesFilename = null;

        //@Parameter(names = {"-r", "--readline"}, description = "Use the readline library, if available")
        //private boolean readLine; // TODO: must not be combined with --command

        @Parameter(names = {"-V", "--version"}, description = "Display version information")
        private boolean versionRequested;

        @Parameter(names = {"-v", "--verbose"}, description = "Have some commands executed verbosely")
        private boolean verbose;

        @Parameter(names = {"-x", "--experimental"}, description = "Enable experimental features")
        private boolean experimental;

        @Parameter(description = "[parameters]")
        private ArrayList<String> parameters = new ArrayList<>();
    }

    private static JCommander argumentParser;
    private static CommandLineArgs commandLineArgs = new CommandLineArgs();

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

        if (commandLineArgs.versionRequested)
            System.out.println(Version.versionString);

        IHarcHardware hardware = null;
        try {
            if (commandLineArgs.hardware == null) {
                System.err.println("Warning: no hardware given, very limited functionallity available.");
            } else {
                hardware = newHardware(commandLineArgs.hardware, commandLineArgs.parameters, commandLineArgs.verbose, commandLineArgs.debug);
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
        if (commandLineArgs.girr == null) {
            System.err.println("Warning: no remotes");
        } else {
            try {
                namedRemotes = new NamedRemotes(commandLineArgs.girr);
            } catch (ParserConfigurationException | SAXException | IOException | IrpMasterException | ParseException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /*HashMap<String, Long>params = new HashMap<>();
        params.put("F", 83L);
        params.put("D", 73L);
        RemoteCommandDataBase.RemoteCommand rc = namedRemotes.getRemoteCommand("nec1", params);
        System.out.println(rc);*/

        String prompt = "JGirs> "; // FIXME
        Engine engine = null;
        try {
            engine = new Engine(hardware, renderer, new Irp(), namedRemotes, commandLineArgs.appName,
                    commandLineArgs.charSet, prompt, commandLineArgs.verbose, commandLineArgs.debug);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        } catch (IncompatibleArgumentException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(3);
        }

        if (commandLineArgs.command != null) {
            try {
                List<String> result = engine.eval(commandLineArgs.command);
                System.out.println(result == null ? "FAIL" : result.isEmpty() ? "SUCCESS" : result);
            } catch (IOException | HarcHardwareException | IrpMasterException | JGirsException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                if (commandLineArgs.readline)
                    engine.readEvalPrint(commandLineArgs.initfile, commandLineArgs.historyfile, true);
                else
                    engine.readEvalPrint(true);
            } finally {
                try {
                    if (hardware != null)
                        hardware.close();
                } catch (IOException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
