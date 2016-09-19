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
 *
 */

package org.harctoolbox.jgirs;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.IrpUtils;
import org.harctoolbox.IrpMaster.ParseException;
import org.harctoolbox.harchardware.HarcHardwareException;
import static org.harctoolbox.jgirs.Engine.ERROR;
import static org.harctoolbox.jgirs.Engine.OK;
import org.harctoolbox.readlinecommander.ReadlineCommander;
import org.xml.sax.SAXException;

public final class JGirs {
    private static final Logger logger = Logger.getLogger(JGirs.class.getName());

    private static void usage(int exitcode, JCommander argumentParser) {
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
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        JCommander argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName(Version.appName);

        try {
            argumentParser.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            usage(IrpUtils.exitUsageError, argumentParser);
        }

        if (commandLineArgs.helpRequested)
            usage(IrpUtils.exitSuccess, argumentParser);

        if (commandLineArgs.versionRequested) {
            System.out.println(Version.versionString);
            doExit(IrpUtils.exitSuccess);
        }

        ConfigFile config = null;
        try {
            config = readConfig(commandLineArgs);
        } catch (SAXException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | HarcHardwareException | ConfigFile.NoSuchRemoteTypeException | IrpMasterException | IOException | ConfigFile.NonUniqueHardwareName | java.text.ParseException ex) {
            //Logger.getLogger(JGirs.class.getName()).log(Level.SEVERE, null, ex);
            logger.log(Level.SEVERE, "Could not read the config file: {0}", ex.toString());
            doExit(IrpUtils.exitConfigReadError);
        }

        try {
            doWork(config, commandLineArgs);
        } catch (IOException | IncompatibleArgumentException ex) {
            logger.log(Level.SEVERE, "{0}", ex.toString());
            doExit(IrpUtils.exitFatalProgramFailure);
        }
    }

    private static ConfigFile readConfig(CommandLineArgs commandLineArgs) throws SAXException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, ConfigFile.NoSuchRemoteTypeException, IrpMasterException, ParseException, IOException, ConfigFile.NonUniqueHardwareName, java.text.ParseException {
        ConfigFile config = new ConfigFile(commandLineArgs.configFile); // ok also if arg == null

        // Additional Girr files from the command line
        config.addGirr(commandLineArgs.girr);

        // Some stuff from the command line overrides the data in the configuration file:
        if (commandLineArgs.irpMasterIni != null)
            config.setStringOption(Parameters.IRPPROTOCOLSINI, commandLineArgs.irpMasterIni);
        if (commandLineArgs.verbosity)
            config.setBooleanOption(Parameters.VERBOSITY, commandLineArgs.verbosity);
        if (commandLineArgs.transmitDevice != null)
            config.setStringOption(Parameters.TRANSMITDEVICE, commandLineArgs.transmitDevice);
        if (commandLineArgs.captureDevice != null)
            config.setStringOption(Parameters.CAPTUREDEVICE, commandLineArgs.captureDevice);
        if (commandLineArgs.receiveDevice != null)
            config.setStringOption(Parameters.RECEIVEDEVICE, commandLineArgs.receiveDevice);

        return config;
    }

    private static void doWork(ConfigFile config, CommandLineArgs commandLineArgs) throws IOException, FileNotFoundException, IncompatibleArgumentException {
        try (Engine engine = Engine.newEngine(config)) {
            if (commandLineArgs.tcp) {
                if (!commandLineArgs.parameters.isEmpty()) {
                    logger.log(Level.SEVERE, "Cannot use command arguments together with --tcp.");
                    doExit(IrpUtils.exitSemanticUsageError);
                }
                tcpServerWork(engine, commandLineArgs);
            } else if (commandLineArgs.parameters.isEmpty()) {
                readlineWork(engine, commandLineArgs);
            } else {
                oneCommandWork(engine, commandLineArgs);
            }
        }
    }

    private static void readlineWork(Engine engine, CommandLineArgs commandLineArgs) throws IOException {
        final int returnLines = -1;
        engine.greet();
//        if (commandLineArgs.immediateInit)
//            engine.openAll();
        String historyFile = commandLineArgs.historyfile != null
                ? commandLineArgs.historyfile
                : ReadlineCommander.defaultHistoryFile(commandLineArgs.appName);

        ReadlineCommander.init(commandLineArgs.initfile, historyFile,
                commandLineArgs.prompt, commandLineArgs.appName);
        ReadlineCommander.setGoodbyeWord(Base.GOODBYE);

        ReadlineCommander.readEvalPrint(engine, commandLineArgs.waitForAnswer, returnLines);
        ReadlineCommander.close();
    }

    private static void oneCommandWork(Engine engine, CommandLineArgs commandLineArgs) {
        // shoot one command
        for (int i = 0; i < commandLineArgs.count; i++) {
            String commandLine = String.join(" ", commandLineArgs.parameters);
            List<String> result = engine.exec(commandLine);
            if (result.isEmpty())
                System.out.println(OK);
            else {
                try {
                    String resultString = Utils.pack(result, Parameters.getInstance().getString("listSeparator"));
                    System.out.println(resultString);
                } catch (NoSuchParameterException ex) {
                    throw new RuntimeException();
                }
            }
        }
    }

    private static void tcpServerWork(Engine engine, CommandLineArgs commandLineArgs) throws UnknownHostException, IOException {
        InetAddress ipAddress = InetAddress.getByName(commandLineArgs.ipName);
        ServerSocket serverSocket = new ServerSocket(commandLineArgs.tcpPort, 10, ipAddress);
        logger.log(Level.INFO, "Listening on IP address {0} at port {1}",
                new Object[]{commandLineArgs.ipName, commandLineArgs.tcpPort});
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                logger.log(Level.INFO, "TCP client connention!");
                tcpClientSession(engine, socket, commandLineArgs.charset);
            }
            logger.log(Level.INFO, "Closing, listening again (Ctrl-C to stop)");
        }
    }

    private static void tcpClientSession(Engine engine, Socket socket, String charset) throws IOException {
        engine.init();
        try (BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
                PrintStream toClient = new PrintStream(socket.getOutputStream(), true, charset)) {
            toClient.println(engine.getVersion());
            while (!engine.isQuitRequested()) {
                String line = fromClient.readLine();
                if (line == null) {
                    logger.log(Level.INFO, "Connection forcably closed by client");
                    break;
                }
                Collection<String> out = engine.exec(line);
                String resultString = packAnswer(out);
                toClient.println(resultString);
            }
        }
    }

    private static String packAnswer(Collection<String> list) {
        try {
            return list == null ? ERROR
                    : list.isEmpty() ? OK
                    : Utils.pack(list, Parameters.getInstance().getString("listSeparator"));
        } catch (NoSuchParameterException ex) {
            throw new RuntimeException();
        }
    }

    private JGirs() {}

    // NOTE: "verbosity" is a silly name for a boolean property, but let's
    // keep it for compatibility with hardhardware.
    //private boolean verbosity;

    private final static class CommandLineArgs {

        @Parameter(names = {"--appname"}, description = "Appname for finding resourses")
        private String appName = "JGirs";

        @Parameter(names = {"-c", "--config"}, description = "Pathname to config file")
        private String configFile = null;

        @Parameter(names = {"-C", "--capturedevice"}, description = "Device to use as default capturing device")
        private String captureDevice = null;

        @Parameter(names = {"--charset"}, description = "Charset for inputs")
        private String charset = "US-ASCII";

        @Parameter(names = {"-g", "--girr"}, variableArity = true, description = "Additional remotes in Girr files")
        private List<String> girr = new ArrayList<>(8);

        @Parameter(names = {"--help", "-?"}, description = "Display help message")
        private boolean helpRequested = false;

        @Parameter(names = {"--historyfile"}, description = "History file for readline")
        private String historyfile = null;//System.getProperty("user.home") + File.separator + ".jgirs.history";

//        @Parameter(names = {"--immediate-init"}, description = "Open hardware immediately at start.")
//        private boolean immediateInit = false;

        @Parameter(names = {"-i", "--irpprotocolsini"}, description = "IrpMaster protocol file")
        private String irpMasterIni = null;//"IrpProtocols.ini";

        @Parameter(names = {"--ip"}, description = "Local IP name/address to bind to (default 0.0.0.0)")
        private String ipName = "0.0.0.0";

        @Parameter(names = {"--initfile"}, description = "Initfile for readline")
        private String initfile = null;//System.getProperty("user.home") + File.separator + ".jgirs";

        @Parameter(names = {      "--prompt"}, description = "Interactive prompt for readline")
        private String prompt = "JGirs> ";

        @Parameter(names = { "-p", "--port"}, description = "Port for TCP")
        private int tcpPort = 33333;

        @Parameter(names = {"-r", "--readline"}, description = "Use the readline library, if available")
        private boolean readLine;

        @Parameter(names = {"-R", "--receivedevice"}, description = "Device to use as default receiving device")
        private String receiveDevice = null;

        @Parameter(names = {"-T", "--transmitdevice"}, description = "Device to use as default transmitting device")
        private String transmitDevice = null;

        @Parameter(names = {"-t", "--tcp"}, description = "Run as TCP server, using address from --ip and port from --port")
        private boolean tcp = false;

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

