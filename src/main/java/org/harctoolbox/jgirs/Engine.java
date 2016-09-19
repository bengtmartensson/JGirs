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

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ICommandExecutor;
import static org.harctoolbox.jgirs.Parameters.VERBOSITY;
import org.xml.sax.SAXException;

public final class Engine implements ICommandExecutor, Closeable {

    private static volatile Engine instance = null;

    private static final Logger logger = Logger.getLogger(Engine.class.getName());

    public static final String OK      = "OK";
    public static final String ERROR   = "ERROR";
    public static final String TIMEOUT = ".";

    public static Engine getInstance() {
        return instance;
    }

    public static Engine newEngine(ConfigFile config) throws FileNotFoundException, IncompatibleArgumentException {
        if (instance != null)
            throw new InvalidMultipleInstantiation();
        instance = new Engine(config);
        return instance;
    }

    public static Engine newEngine(String url) throws FileNotFoundException, IncompatibleArgumentException, SAXException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, ConfigFile.NoSuchRemoteTypeException, ParseException, IrpMasterException, IOException, ConfigFile.NonUniqueHardwareName {
        return newEngine(new ConfigFile(url));
    }

    private final TreeMap<String, GirsHardware> irHardware;
    private final TreeMap<String, Module> modules;

    private Renderer renderer = null;
    private Irp irp = null;

    private final List<String> outBuffer;

    private Engine(ConfigFile config) throws FileNotFoundException, IncompatibleArgumentException {
        outBuffer = new ArrayList<>(8);
        irHardware = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        config.getIrHardware().stream().forEach((GirsHardware hw) -> {
            irHardware.put(hw.getName(), hw);
        });

        modules = new TreeMap<>();

        // Set up the special modules
        registerModule(Parameters.newParameterModule());

        // May override values in the code
        Parameters.getInstance().addAll(config.getOptions());

        Parameters.getInstance().add(new StringParameter("hardwareList",
                Utils.sortedString(irHardware.keySet(), " "), "List of available hardware"));

        registerModule(Base.newBase());

        try {
            renderer = new Renderer(Parameters.getInstance().getString(Parameters.IRPPROTOCOLSINI));
            registerModule(renderer);
        } catch (NoSuchParameterException ex) {
            logger.log(Level.WARNING, Parameters.IRPPROTOCOLSINI + " not defined; rendering will not be available");
        }

        if (config.getRemoteCommandsDataBase() != null)
            registerModule(NamedRemotes.newNamedRemotes(config.getRemoteCommandsDataBase()));

        irp = new Irp();
        registerModule(irp);

        registerModule(Transmit.newTransmit(renderer, irp));
        registerModule(Transmitters.newTransmittersModule());
        registerModule(Capture.newCapture());
        registerModule(Receive.newReceive());

        config.getModuleList().stream().forEach((module) -> {
            try {
                registerModule(module.instantiate());
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });

        irHardware.values().forEach((GirsHardware ghw) -> {
            if (ghw.isImmediateOpen()) {
                ghw.getHardware().setVerbosity(isVerbosity());
                try {
                    ghw.getHardware().open();
                } catch (HarcHardwareException | IOException ex) {
                    logger.log(Level.WARNING, null, ex);
                }
            }
        });
    }

    public String greet() {
        return getVersion();
    }

    public synchronized GirsHardware getHardware(String name) throws NoSuchHardwareException, AmbigousHardwareException {
        List<String> candidates = Utils.findStringsWithPrefix(irHardware.keySet(), name);
        if (candidates.isEmpty())
            throw new NoSuchHardwareException(name);
        if (candidates.size() > 1)
            throw new AmbigousHardwareException(name);

        return irHardware.get(candidates.get(0));
    }

    public synchronized GirsHardware getTransmitHardware() throws NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException {
        return getHardware(Parameters.getInstance().getString(Parameters.TRANSMITDEVICE));
    }

    public synchronized GirsHardware getCaptureHardware() throws NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException {
        return getHardware(Parameters.getInstance().getString(Parameters.CAPTUREDEVICE));
    }

    public synchronized GirsHardware getReceiveHardware() throws NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException {
        return getHardware(Parameters.getInstance().getString(Parameters.RECEIVEDEVICE));
    }

    private void registerModule(Module module) {
        if (module == null)
            return;
        modules.put(module.getName(), module);
    }

    public synchronized Set<String> getModuleNames() {
        return modules.keySet();
    }

    @Override
    public synchronized String getVersion() {
        return Version.versionString;
    }

    // NOTE: "verbosity" is a silly name for a boolean property, but let's
    // keep it for compatibility with HardHardware.
    @Override
    public synchronized void setVerbosity(boolean verbosity) {
        Parameters.getInstance().setBoolean(VERBOSITY, verbosity);
    }

    public synchronized boolean isVerbosity() {
        try {
            return Parameters.getInstance().getBoolean(VERBOSITY);
        } catch (NoSuchParameterException ex) {
            // Not really a disaster
            logger.warning(VERBOSITY + " is not defined");
            return false;
        }
    }

    public synchronized void init() {
        Base.getInstance().setQuitRequested(false);
    }

    // NOTE: This class does not support "debug" as in harchardware.
    // Logging (or the interactive debugger) is to be used instead.
    // Or the debugger...
    @Override
    public void setDebug(int debug) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void setTimeout(int timeout) throws IOException {
        // TODO
    }

    @Override
    public synchronized boolean isValid() {
        return true; //???
    }

    public synchronized boolean isQuitRequested() {
        return Base.getInstance().isQuitRequested();
    }

    @Override
    public synchronized void open() throws HarcHardwareException, IOException {
    }

//    synchronized void openAll() {
//        irHardware.values().stream().forEach((hardware) -> {
//            try {
//                hardware.getHardware().setVerbosity(isVerbosity());
//                hardware.getHardware().open();
//            } catch (IOException | HarcHardwareException ex) {
//                logger.log(Level.WARNING, "Failed to open " + hardware.getName(), ex);
//            }
//        });
//    }

    @Override
    public synchronized void close() {
        irHardware.values().stream().forEach((hardware) -> {
            try {
                hardware.getHardware().close();
            } catch (IOException ex) {
                logger.log(Level.WARNING, null, ex);
            }
        });
    }

    public synchronized Set<String> getCommandNames() {
        return CommandExecuter.getMainExecutor().getCommandNames();
    }

//    public List<String> getCommandNames(String moduleName) throws NoSuchModuleException {
//        Module module = modules.get(moduleName);
//        if (module == null)
//            throw new NoSuchModuleException(moduleName);
//
//        return module.getCommandNames();
//    }

    public synchronized Collection<String> getSubCommandNames(String command) {
        return CommandExecuter.getMainExecutor().getSubCommandNames(command);
    }

    @Override
    public synchronized List<String> exec(String command) {
        String[] semicolonSplit = Utils.tokenizer(command, ";");
        List<String> result = new ArrayList<>(2*semicolonSplit.length);

        for (String cmd : semicolonSplit) {
            Collection<String> out = execAtomic(cmd);
            result.addAll(out);
        }
        return result;
    }

    private synchronized Collection<String> execAtomic(String command) {
        if (command.isEmpty())
            return Utils.singletonArrayList(OK);

        if (command.trim().isEmpty())
            return Utils.singletonArrayList(OK);

        try {
            String[] tokens = Utils.tokenizer(command.trim());
            Collection<String> list = CommandExecuter.getMainExecutor().exec(tokens);
            return list;

        } catch (JGirsException | IOException | HarcHardwareException | IrpMasterException | RuntimeException ex) {
            return Utils.singletonArrayList(ERROR + ": " + ex.toString());
        }
    }
}
