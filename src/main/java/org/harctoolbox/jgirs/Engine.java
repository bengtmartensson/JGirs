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
import org.harctoolbox.harchardware.ICommandLineDevice;
import static org.harctoolbox.jgirs.Parameters.VERBOSITY;

public final class Engine implements ICommandLineDevice, Closeable {
    // NOTE: "verbosity" is a silly name for a boolean property, but let's
    // keep it for compatibility with HardHardware.
    //private boolean verbosity;

    private static volatile Engine instance = null;

    private static final Logger logger = Logger.getLogger(Engine.class.getName());

    public  static final String OK = "OK";
    public  static final String ERROR = "ERROR";

    public static Engine getInstance() {
        return instance;
    }

    public static Engine newEngine(ConfigFile config) throws FileNotFoundException, IncompatibleArgumentException {
        if (instance != null)
            throw new InvalidMultipleInstantiation();
        instance = new Engine(config);
        return instance;
    }

    private final TreeMap<String, GirsHardware> irHardware;
    private final TreeMap<String, Module> modules;

    private Renderer renderer = null;
    private Irp irp = null;

    private final List<String> outBuffer;

    public Engine(ConfigFile config) throws FileNotFoundException, IncompatibleArgumentException {
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

    public void greet() {
        Base.getInstance().version();//outBuffer.add(Version.versionString);
    }

    public GirsHardware getHardware(String name) throws NoSuchHardwareException, AmbigousHardwareException {
        List<String> candidates = Utils.findStringsWithPrefix(irHardware.keySet(), name);
        if (candidates.isEmpty())
            throw new NoSuchHardwareException(name);
        if (candidates.size() > 1)
            throw new AmbigousHardwareException(name);

        return irHardware.get(candidates.get(0));
    }

    public GirsHardware getTransmitHardware() throws NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException {
        return getHardware(Parameters.getInstance().getString(Parameters.TRANSMITDEVICE));
    }

    public GirsHardware getCaptureHardware() throws NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException {
        return getHardware(Parameters.getInstance().getString(Parameters.CAPTUREDEVICE));
    }

    public GirsHardware getReceiveHardware() throws NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException {
        return getHardware(Parameters.getInstance().getString(Parameters.RECEIVEDEVICE));
    }

    @Override
    public void flushInput() throws IOException {
    }

    private void registerModule(Module module) {
        if (module == null)
            return;
        modules.put(module.getName(), module);
    }

    public Set<String> getModuleNames() {
        return modules.keySet();
    }

    @Override
    public synchronized void sendString(String cmd) throws IOException {
        List<String> result = eval(cmd);
        outBuffer.addAll(result);
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
    public boolean ready() {
        return outBuffer.size() > 0;
    }

    @Override
    public String getVersion() {
        return Version.versionString;
    }

    @Override
    public void setVerbosity(boolean verbosity) {
        Parameters.getInstance().setBoolean(VERBOSITY, verbosity);
    }

    // NOTE: This software does not support "debug" as in harchardware.
    // Logging (or the interactive debugger) is to be used instead.
    // Or the debugger...
    @Override
    public void setDebug(int debug) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        // TODO
    }

    @Override
    public boolean isValid() {
        return true; //???
    }

    public boolean isQuitRequested() {
        return Base.getInstance().isQuitRequested();
    }

    @Override
    public void open() throws HarcHardwareException, IOException {
    }

    void openAll() {
        irHardware.values().stream().forEach((hardware) -> {
            try {
                hardware.getHardware().setVerbosity(isVerbosity());
                hardware.getHardware().open();
            } catch (IOException | HarcHardwareException ex) {
                logger.log(Level.WARNING, "Failed to open " + hardware.getName(), ex);
            }
        });
    }

    @Override
    public void close() {
        irHardware.values().stream().forEach((hardware) -> {
            try {
                hardware.getHardware().close();
            } catch (IOException ex) {
                logger.log(Level.WARNING, null, ex);
            }
        });
    }

    public Set<String> getCommandNames() {
        return CommandExecuter.getMainExecutor().getCommandNames();
    }

    public List<String> getCommandNames(String moduleName) throws NoSuchModuleException {
        Module module = modules.get(moduleName);
        if (module == null)
            throw new NoSuchModuleException(moduleName);

        return module.getCommandNames();
    }

    public Collection<String> getSubCommandNames(String command) {
        return CommandExecuter.getMainExecutor().getSubCommandNames(command);
    }

    List<String> eval(String command) {
        if (command.trim().isEmpty())
            return Utils.singletonArrayList(OK);

        try {
            String[] tokens = Utils.tokenizer(command.trim());
            List<String> out = new ArrayList<>(8);
            int beg = 0;
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equals(";")) {
                    String[] args = new String[i - beg];
                    System.arraycopy(tokens, beg, args, 0, i - beg);
                    Collection<String> list = CommandExecuter.getMainExecutor().exec(args);
                    if (list == null)
                        return null;
                    out.addAll(list);
                    beg = i + 1;
                }
            }
            if (beg < tokens.length) {
                String[] args = new String[tokens.length - beg];
                System.arraycopy(tokens, beg, args, 0, tokens.length - beg);
                Collection<String> list = CommandExecuter.getMainExecutor().exec(args);
                if (list == null)
                        return null;
                out.addAll(list);
            }
            return out;
        } catch (JGirsException | IOException | HarcHardwareException | IrpMasterException | RuntimeException ex) {
            return Utils.singletonArrayList(ERROR + ": " + ex.toString());
        }
    }

    public boolean isVerbosity() {
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
}
