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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.harchardware.HarcHardwareException;

/**
 * A container class for ICommand.
 */
public class CommandExecuter {

    private static final CommandExecuter mainExecuter = new CommandExecuter();

    public static CommandExecuter getMainExecutor() {
        return mainExecuter;
    }

    private final HashMap<String, ICommand> commandMap;

    public CommandExecuter() {
        commandMap = new HashMap<>(8);
    }

    public Set<String> getCommandNames() {
        return commandMap.keySet();
    }

    public Set<String> getSubCommandNames(String command) {
        ICommand cmd = commandMap.get(command);
        if (!(cmd instanceof CommandWithSubcommands))
            return null;
        return ((CommandWithSubcommands) cmd).getSubCommandNames();
    }

    public Collection<ICommand> getCommands() {
        return commandMap.values();
    }

    public String exec(String[] args) throws CommandException, IOException, HarcHardwareException, IrpMasterException, AmbigousCommandException {
        String commandName = args[0];
        ICommand cmd = null;
        if (commandMap.containsKey(commandName))
            cmd = commandMap.get(commandName);
        else {
            for (Map.Entry<String, ICommand> kvp : commandMap.entrySet()) {
                if (kvp.getKey().startsWith(commandName)) {
                    if (cmd == null) {
                        cmd = kvp.getValue();
                        //break;
                    } else
                        throw new AmbigousCommandException(commandName);
                }
            }
        }
        if (cmd == null)
            throw new NoSuchCommandException(commandName);
        String[] rest = new String[args.length-1];
        System.arraycopy(args, 1, rest, 0, args.length - 1);
        return cmd.exec(rest);
    }

    public void add(ICommand command) {
        if (commandMap.containsKey(command.getName()))
            throw new RuntimeException("Multiply defined command: " + command.getName());

        commandMap.put(command.getName(), command);
    }
}
