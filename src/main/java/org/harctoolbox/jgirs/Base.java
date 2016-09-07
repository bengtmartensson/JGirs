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
import java.util.Set;

/**
 * The Girs Base module that must be implemented.
 */
public class Base extends Module {

    static final String goodbyeWord = "Bye!";

//    private final HashMap<String, Module> modules;
    //private final CommandExecuter commandExecuter;
//    private final IHarcHardware hardware;
    private boolean quitRequested = false;

    private final Engine engine;

    public Base(CommandExecuter commandExecuter, Parameters parameters, Engine engine) {
        super(commandExecuter, parameters);
        addCommand(new VersionCommand());
        addCommand(new ModulesCommand());
        addCommand(new LicenseCommand());
        addCommand(new GirsCommandsCommand());
        addCommand(new QuitCommand());
        this.engine = engine;
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
        public String[] exec(String[] args) {
            return new String[] { Version.licenseString };
        }
    }

    private static class QuitCommand implements ICommand {

        @Override
        public String getName() {
            return "quit";
        }

        @Override
        public String[] exec(String[] args) {
            return new String[] { goodbyeWord };
        }
    }

    private static class VersionCommand implements ICommand {

        @Override
        public String getName() {
            return "version";
        }

        @Override
        public String[] exec(String[] args) throws IOException {
            return new String[] { Version.versionString };
        }
    }

    private class ModulesCommand implements ICommand {

        @Override
        public String getName() {
            return "modules";
        }

        @Override
        public String[] exec(String[] args) {
            Set<String> list = engine.getModuleNames();
            return list.toArray(new String[list.size()]);
        }
    }

    private class GirsCommandsCommand implements ICommand {
        @Override
        public String getName() {
            return "girscommands";
        }

        @Override
        public String[] exec(String[] args) throws CommandSyntaxException {
            if (args.length > 1)
                throw new CommandSyntaxException(args[0], 0);

            Set<String> result = commandExecuter.getCommandNames();
            return result.toArray(new String[result.size()]);
        }
    }
}
