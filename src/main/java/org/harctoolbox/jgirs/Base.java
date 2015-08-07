/*
Copyright (C) 2014 Bengt Martensson.

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
 *
 */
public class Base extends Module {

    private HashMap<String, Module> modules;
    private CommandExecuter commandExecuter;
    private IHarcHardware hardware;
    private boolean quitRequested = false;

    private class VersionCommand implements Command {

        @Override
        public String getName() {
            return "version";
        }

        @Override
        public List<String> exec(String[] args) throws IOException {
            ArrayList<String> result = new ArrayList<>();
            result.add(Version.versionString);
            result.add(hardware.getVersion());
            return result;
        }
    }

    private static class LicenseCommand implements Command {

        @Override
        public String getName() {
            return "license";
        }

        @Override
        public List<String> exec(String[] args) {
            ArrayList<String> result = new ArrayList<>();
            result.add(Version.licenseString);
            return result;
        }
    }

    private class ModulesCommand implements Command {

        @Override
        public String getName() {
            return "modules";
        }

        @Override
        public List<String> exec(String[] args) {
            return new ArrayList<>(modules.keySet());
        }
    }

    private class QuitCommand implements Command {

        @Override
        public String getName() {
            return "quit";
        }

        @Override
        public List<String> exec(String[] args) {
            quitRequested = true;
            return new ArrayList<String>() {{ add("Bye!"); }};
        }
    }

    private class GirsCommandsCommand implements Command {
        @Override
        public String getName() {
            return "girscommands";
        }

        @Override
        public List<String> exec(String[] args) throws NoSuchModuleException {
            ArrayList<String> cmds;
            boolean sort = true;
            if (args.length > 1) {
                Module module = modules.get(args[1]);
                if (module == null)
                    throw new NoSuchModuleException(args[1]);
                cmds = module.getCommandNames(sort);
            } else
                cmds = commandExecuter.getCommandNames(sort);
           return cmds;
        }
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }

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
}
