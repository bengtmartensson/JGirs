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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.IrpUtils;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ICommandLineDevice;
import org.harctoolbox.readlinecommander.ReadlineCommander;
import org.xml.sax.SAXException;

public final class Engine implements ICommandLineDevice, Closeable {
    private static final Logger logger = Logger.getLogger(Engine.class.getName());

    private static JCommander argumentParser;
    private static final CommandLineArgs commandLineArgs = new CommandLineArgs();

    private static final String IRPPROTOCOLSINI = "irpProtocolsIni";

    private static void usage(int exitcode) {
        StringBuilder str = new StringBuilder(128);
        argumentParser.usage(str);

        (exitcode == IrpUtils.exitSuccess ? System.out : System.err).println(str);
        doExit(exitcode); // placifying FindBugs...
    }

    private static void doExit(int exitcode) {
        System.exit(exitcode);
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

        ConfigFile config = null;

        try {
            config = new ConfigFile(commandLineArgs.configFile); // ok also if arg == null
        } catch (SAXException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException | ConfigFile.NoSuchRemoteTypeException | ParseException | IrpMasterException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(IrpUtils.exitConfigReadError);
        }

        if (!commandLineArgs.hardware.isEmpty()) {
            try {
                GirsHardware irHardware = new GirsHardware(commandLineArgs.hardware, "default", true, true);
                config.addHardware(irHardware);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException | IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(IrpUtils.exitFatalProgramFailure);
            }
        }

        if (commandLineArgs.irpMasterIni != null) {
            config.setStringOption(IRPPROTOCOLSINI, commandLineArgs.irpMasterIni);
        }

        try {
            config.addGirs(commandLineArgs.girr);
        } catch (ParseException | IOException | SAXException | IrpMasterException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(IrpUtils.exitFatalProgramFailure);
        }

        if (commandLineArgs.verbosity)
            config.setBooleanOption("verbosity", true);

        int returnLines = -1; //commandLineArgs.oneLine ? 1 : commandLineArgs.twoLines ? 2 : 0;
        try (Engine engine = new Engine(config)) {
            if (commandLineArgs.parameters.isEmpty()) {
                String historyFile = commandLineArgs.historyfile != null
                        ? commandLineArgs.historyfile
                        : ReadlineCommander.defaultHistoryFile(commandLineArgs.appName);
                ReadlineCommander.init(commandLineArgs.initfile, historyFile,
                        commandLineArgs.prompt, commandLineArgs.appName);
                ReadlineCommander.setGoodbyeWord(Base.goodbyeWord);
                ReadlineCommander.readEvalPrint(engine, commandLineArgs.waitForAnswer, returnLines);
                ReadlineCommander.close();

            } else {
                // shoot one command
                try {
                    for (int i = 0; i < commandLineArgs.count; i++) {
                        String[] result = engine.eval(String.join(" ", commandLineArgs.parameters));
                        System.out.println(result == null ? "FAIL" : result.length == 0 ? "SUCCESS" : result);
                    }
                } catch (IOException | HarcHardwareException | IrpMasterException | JGirsException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | IrpMasterException ex) {
            logger.log(Level.SEVERE, null, ex);
            System.exit(IrpUtils.exitFatalProgramFailure);
        }
    }

    private final HashMap<String, Module> modules;
    private final CommandExecuter commandExecuter;

    // Speical modules
    private final Base base;
    private final Parameters parameters;
    private NamedRemotes namedRemotes;
    private Renderer renderer;
    private final Irp irp;

    // default hardware
    private final GirsHardware currentOutputGirsHardware;
    private final GirsHardware currentInputGirsHardware;
    //private final Charset charSet;

    // NOTE: "verbosity" is a silly name for a boolean property, but let's
    // keep it for compatibility with hardhardware.
    private boolean verbosity;

    // NOTE: This software does not support "debug" as in harchardware.
    // Logging (or the interactive debugger) is to be used instead.

    private final List<String> outBuffer;
    private final List<GirsHardware> irHardwareList;

    public Engine(ConfigFile config) throws FileNotFoundException, IncompatibleArgumentException {
        //this.config = config;
        this.outBuffer = new ArrayList<>(8);
        this.verbosity = config.getBooleanOption("verbosity");
//        this.charSet = Charset.forName(charsetName);

        this.irHardwareList = config.getIrHardwareList();

        currentOutputGirsHardware = new GirsHardware(config.getDefaultOutputHardware());
        currentInputGirsHardware = new GirsHardware(config.getDefaultInputHardware());
        //this.namedCommand = namedCommand;
        modules = new LinkedHashMap<>(16);
        commandExecuter = new CommandExecuter();

        // Set up the special modules
        parameters = new Parameters(commandExecuter);
        registerModule(parameters);
        base = new Base(commandExecuter, parameters, this);
        registerModule(base);

        if (config.getStringOption(IRPPROTOCOLSINI) != null) {
            renderer = new Renderer(commandExecuter, parameters, config.getStringOption(IRPPROTOCOLSINI));
            registerModule(renderer);
        }

        if (config.getRemoteCommandsDataBase() != null) {
            namedRemotes = new NamedRemotes(commandExecuter, parameters, config.getRemoteCommandsDataBase());
            registerModule(namedRemotes);
        }

        irp = new Irp(commandExecuter, parameters);
        registerModule(irp);

        registerModule(new Transmit(commandExecuter, parameters, currentOutputGirsHardware, renderer, irp, namedRemotes));

        //if (currentGirsHardware.getHardware() instanceof ITransmitter)
        registerModule(new Transmitters(commandExecuter, parameters));
        //if (currentGirsHardware.getHardware() instanceof ICapture)
        registerModule(new Capture(commandExecuter, parameters));
        //if (currentGirsHardware.getHardware() instanceof IReceive)
        registerModule(new Receive(commandExecuter, parameters, namedRemotes));
        //registerModule(Transmit.newTransmit(currentGirsHardware.getHardware(), renderer, irp, namedRemotes));

        config.getModuleList().stream().forEach((module) -> {
            try {
                registerModule(module.instantiate(commandExecuter, parameters));
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void flushInput() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void registerModule(Module module) {
        if (module == null)
            return;
        modules.put(module.getName(), module);
//        module.getCommands().stream().forEach((cmd) -> {
//            commandExecuter.addCommand(cmd);
//        });
        //parameters.addParameters(module);
        //module.addParametersTo(parameters);
    }

    public Set<String> getModuleNames() {
        return modules.keySet();
    }

    @Override
    public synchronized void sendString(String cmd) throws IOException {
        try {
            String[] result = eval(cmd);
            outBuffer.addAll(Arrays.asList(result)); //outBuffer.addAll(eval(cmd));
            //for (String str : result)
            //    out.println(str);
            } catch (JGirsException ex) {
                outBuffer.add("ERROR: " + ex.getMessage());
                //if (NoSuchCommandException.class.isInstance(ex)
                //        || AmbigousCommandException.class.isInstance(ex))
                //    err.println("Commands are: " + Base.join(commandExecuter.getCommandNames(true)));
            } catch (IOException | HarcHardwareException | IrpMasterException ex) {
                logger.log(Level.SEVERE, null, ex);
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
        return Version.versionString;
    }

    @Override
    public void setVerbosity(boolean verbosity) {
        this.verbosity = verbosity;
    }

    @Override
    public void setDebug(int debug) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        // TODO
//        currentGirsHardware.getHardware().setTimeout(timeout);
    }

    @Override
    public boolean isValid() {
        return true; //???
    }

    @Override
    public void open() throws HarcHardwareException, IOException {
//        if (currentGirsHardware.getHardware() != null)
//            currentGirsHardware.getHardware().open();// ???
    }

    @Override
    public void close() {
        irHardwareList.stream().forEach((hardware) -> {
            try {
                hardware.getHardware().close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }

    public Set<String> getCommandNames() {
        return commandExecuter.getCommandNames();
    }

    public List<String> getCommandNames(String moduleName) throws NoSuchModuleException {
        Module module = modules.get(moduleName);
        if (module == null)
            throw new NoSuchModuleException(moduleName);

        return module.getCommandNames();
    }

    public Set<String> getSubCommandNames(String command) {
        return commandExecuter.getSubCommandNames(command);
    }

//    private boolean isQuitRequested() {
//        return base.isQuitRequested();
//    }

    private String[] eval(String command) throws JGirsException, IOException, HarcHardwareException, IrpMasterException {
        String[] tokens = command.split("\\s+");
        return commandExecuter.exec(tokens);
    }

    private final static class CommandLineArgs {

//        @Parameter(names = {"-1"}, description = "Expect one line of response")
//        private boolean oneLine;

        @Parameter(names = {"--appname"}, description = "Appname for finding resourses")
        private String appName = "JGirs";

        @Parameter(names = {"-c", "--config"}, description = "Pathname to config file")
        private String configFile = null;

//        @Parameter(names = {"--charset"}, description = "Charset for inputs")
//        private String charSet = "US-ASCII";

        @Parameter(names = {"-g", "--girr"}, variableArity = true, description = "Remotes and commands in Girr files")
        private List<String> girr = new ArrayList<>(8);

        @Parameter(names = {"--help", "-?"}, description = "Display help message")
        private boolean helpRequested = false;

        @Parameter(names = {"-h", "--hardware"}, variableArity = true, description = "Driver hardware classname")
        private List<String> hardware = new ArrayList<>(4);

        @Parameter(names = {"--historyfile"}, description = "History file for readline")
        private String historyfile = null;//System.getProperty("user.home") + File.separator + ".jgirs.history";

        @Parameter(names = {"-i", "--irpprotocolsini"}, description = "IrpMaster protocol file")
        private String irpMasterIni = null;//"IrpProtocols.ini";

        @Parameter(names = {"--initfile"}, description = "Initfile for readline")
        private String initfile = null;//System.getProperty("user.home") + File.separator + ".jgirs";

        @Parameter(names = {      "--prompt"}, description = "Interactive prompt for readline")
        private String prompt = "JGirs> ";

        @Parameter(names = {"-r", "--readline"}, description = "Use the readline library, if available")
        private boolean readLine;

        @Parameter(names = {"-V", "--version"}, description = "Display version information")
        private boolean versionRequested;

        @Parameter(names = {"-v", "--verbosity"}, description = "Have some commands executed verbosely")
        private boolean verbosity;

        @Parameter(names = {"-w", "--waitforanswer"}, description = "Microseconds to wait for answer")
        private int waitForAnswer;

        @Parameter(names = {"-#", "--count"}, description = "Number of times to send sequence")
        private int count = 1;

        @Parameter(description = "[parameters]")
        private ArrayList<String> parameters = new ArrayList<>(8);
    }
}
