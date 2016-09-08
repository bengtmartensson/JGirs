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

/**
 * The Girs Base module that must be implemented.
 */
public class Base extends Module {

    public static final String goodbyeWord = "Bye!";
    private static volatile Base instance = null;

    static Base newBase() {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new Base();
        return instance;
    }

//    private final HashMap<String, Module> modules;
    //private final CommandExecuter commandExecuter;
//    private final IHarcHardware hardware;
    private boolean quitRequested = false;

    private Base() {
        super();
        addCommand(new VersionCommand());
        addCommand(new ModulesCommand());
        addCommand(new LicenseCommand());
        addCommand(new GirsCommandsCommand());
        addCommand(new QuitCommand());
//        this.engine = engine;
//        this.hardware = hardware;
//        this.modules = modules;
        //this.commandExecuter = commandExecuter;
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }

    private static class LicenseCommand implements ICommand {

        @Override
        public String getName() {
            return "license";
        }

        @Override
        public String exec(String[] args) {
            return Version.licenseString;
        }
    }

    private static class VersionCommand implements ICommand {

        @Override
        public String getName() {
            return "version";
        }

        @Override
        public String exec(String[] args) throws IOException {
            return Version.versionString;
        }
    }

    private static class ModulesCommand implements ICommand {

        @Override
        public String getName() {
            return "modules";
        }

        @Override
        public String exec(String[] args) {
            return Utils.sortedString(Engine.getInstance().getModuleNames());
        }
    }

    private static class GirsCommandsCommand implements ICommand {
        @Override
        public String getName() {
            return "girscommands";
        }

        @Override
        public String exec(String[] args) throws CommandSyntaxException {
            if (args.length > 1)
                throw new CommandSyntaxException(args[0], 0);
            return Utils.sortedString(CommandExecuter.getMainExecutor().getCommandNames());
        }
    }
    private class QuitCommand implements ICommand {

        @Override
        public String getName() {
            return "quit";
        }

        @Override
        public String exec(String[] args) {
            quitRequested = true;
            return goodbyeWord;
        }
    }
}
