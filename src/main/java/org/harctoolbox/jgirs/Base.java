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

package org.harctoolbox.jgirs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;

/**
 * The Girs Base module containing base commands that must be implemented.
 */
public class Base extends Module {

    public static final String GOODBYE = "Bye!";

    private static volatile Base instance = null;

    static Base newBase() {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new Base();
        return instance;
    }

    public static List<String> commands() {
        return CommandExecuter.getMainExecutor().getCommandNames();
    }

    public static String version() {
        return Version.versionString;
    }

    /**
     * @return the instance
     */
    public static Base getInstance() {
        return instance;
    }

    private boolean quitRequested = false;

    private Base() {
        super();
        addCommand(new VersionCommand());
        addCommand(new ModulesCommand());
        addCommand(new LicenseCommand());
        addCommand(new CommandsCommand());
        addCommand(new QuitCommand());
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }

    public void setQuitRequested(boolean quitRequested) {
        this.quitRequested = quitRequested;
    }

    public String version(String hardwareName) throws IncompatibleHardwareException, IOException, HarcHardwareException, NoSuchParameterException, NoSuchHardwareException, AmbigousHardwareException {
        GirsHardware hardware = Engine.getInstance().getHardware(hardwareName);
        initializeHardware(hardware, IHarcHardware.class);
        return hardware.getHardware().getVersion();
    }

    public String quit() {
        quitRequested = true;
        return GOODBYE;
    }

    private static class LicenseCommand implements ICommand {

        private static final String LICENSE = "license";

        @Override
        public String getName() {
            return LICENSE;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException {
            checkNoArgs(LICENSE, args.length, 0);
            return Utils.singletonArrayList(Version.licenseString);
        }
    }

    private static class ModulesCommand implements ICommand {

        private static final String MODULES = "modules";

        @Override
        public String getName() {
            return MODULES;
        }

        @Override
        public List<String> exec(String[] args) throws NoSuchParameterException, CommandSyntaxException {
            checkNoArgs(MODULES, args.length, 0);
            List<String> list = new ArrayList<>(Engine.getInstance().getModuleNames());
            return list;
        }
    }

    private static class CommandsCommand implements ICommand {

        private static final String COMMANDS = "commands";

        @Override
        public String getName() {
            return COMMANDS;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException, NoSuchParameterException {
            checkNoArgs(COMMANDS, args.length, 0);
            return commands();
        }
    }

    private class VersionCommand implements ICommand {
        private static final String VERSION = "version";

        @Override
        public String getName() {
            return VERSION;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException, NoSuchHardwareException, IOException, IncompatibleHardwareException, HarcHardwareException, NoSuchParameterException, AmbigousHardwareException {
            checkNoArgs(VERSION, args.length, 0, 1);

            return Utils.singletonArrayList(args.length == 0 ? version() : version(args[0]));
        }
    }

    private class QuitCommand implements ICommand {

        private final static String QUIT = "quit";

        @Override
        public String getName() {
            return QUIT;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException {
            checkNoArgs(QUIT, args.length, 0);
            return Utils.singletonArrayList(quit());
        }
    }
}
