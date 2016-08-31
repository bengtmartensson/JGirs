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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.harchardware.HarcHardwareException;

/**
 * A container class for ICommand.
 */
public class CommandExecuter {

    private final HashMap<String, ICommand> commands;
    public CommandExecuter() {
        commands = new LinkedHashMap<>(8);
    }

    public ArrayList<String> getCommandNames(boolean sort) {
        ArrayList<String> commandNames = new ArrayList<>(commands.keySet());
        if (sort)
            Collections.sort(commandNames);

        return commandNames;
    }

    public ArrayList<String> getSubCommandNames(String command, boolean sort) {
        if (!CommandWithSubcommands.class.isInstance(commands.get(command)))
            return null;
        return ((CommandWithSubcommands) commands.get(command)).getSubCommandNames(sort);
    }

    public Collection<ICommand> getCommands() {
        return commands.values();
    }

    public List<String> exec(List<String> args) throws JGirsException, IOException, HarcHardwareException, IrpMasterException {
        String commandName = args.get(0);
        ICommand cmd = null;
        if (commands.containsKey(commandName))
            cmd = commands.get(commandName);
        else {
            for (Map.Entry<String, ICommand> kvp : commands.entrySet()) {
                if (kvp.getKey().startsWith(commandName)) {
                    if (cmd == null)
                        cmd = kvp.getValue();
                    else
                        throw new AmbigousCommandException("Command " + commandName + " is ambigous.");
                }
            }
        }
        if (cmd == null)
            throw new NoSuchCommandException("Command " + commandName + " not found.");
        return cmd.exec(args);
    }

    public void addCommand(ICommand command) {
        if (commands.containsKey(command.getName()))
            throw new RuntimeException("Multiply defined command: " + command.getName());

        commands.put(command.getName(), command);
    }
}
