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
import java.util.HashMap;
import java.util.List;
import org.harctoolbox.harchardware.IHarcHardware;

/**
 * The Girs Base module that must be implemented.
 */
public class Base extends Module {

    public static final String goodbyeWord = "Bye!";

    private final HashMap<String, Module> modules;
    private final CommandExecuter commandExecuter;
    private final IHarcHardware hardware;
    //private boolean quitRequested = false;

    public Base(HashMap<String, Module> modules, CommandExecuter commandExecuter, IHarcHardware hardware) {
        super();
        addCommand(new VersionCommand());
        addCommand(new ModulesCommand());
        addCommand(new LicenseCommand());
        addCommand(new GirsCommandsCommand());
        addCommand(new QuitCommand());
        this.hardware = hardware;
        this.modules = modules;
        this.commandExecuter = commandExecuter;
    }

//    public boolean isQuitRequested() {
//        return quitRequested;
//    }

    private static class LicenseCommand implements ICommand {

        @Override
        public String getName() {
            return "license";
        }

        @Override
        public List<String> exec(List<String> args) {
            ArrayList<String> result = new ArrayList<>(1);
            result.add(Version.licenseString);
            return result;
        }
    }

    private static class QuitCommand implements ICommand {

        @Override
        public String getName() {
            return "quit";
        }

        @Override
        public List<String> exec(List<String> args) {
            //quitRequested = true;
            ArrayList<String> result = new ArrayList<>(1);
            result.add(goodbyeWord);
            return result;
        }
    }

    private class VersionCommand implements ICommand {

        @Override
        public String getName() {
            return "version";
        }

        @Override
        public List<String> exec(List<String> args) throws IOException {
            ArrayList<String> result = new ArrayList<>(1);
            result.add(Version.versionString);
            result.add(hardware != null ? hardware.getVersion() : "Hardware not available");
            return result;
        }
    }

    private class ModulesCommand implements ICommand {

        @Override
        public String getName() {
            return "modules";
        }

        @Override
        public List<String> exec(List<String> args) {
            return new ArrayList<>(modules.keySet());
        }
    }

    private class GirsCommandsCommand implements ICommand {
        @Override
        public String getName() {
            return "girscommands";
        }

        @Override
        public List<String> exec(List<String> args) throws NoSuchModuleException {
            ArrayList<String> cmds;
            boolean sort = true;
            if (args.size() > 1) {
                Module module = modules.get(args.get(1));
                if (module == null)
                    throw new NoSuchModuleException(args.get(1));
                cmds = module.getCommandNames(sort);
            } else
                cmds = commandExecuter.getCommandNames(sort);
           return cmds;
        }
    }
}
