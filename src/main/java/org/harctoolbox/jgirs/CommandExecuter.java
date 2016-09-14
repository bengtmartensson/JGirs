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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.harchardware.HarcHardwareException;

/**
 *
 */
public class CommandExecuter {

    private static final CommandExecuter mainExecuter = new CommandExecuter();

    public static CommandExecuter getMainExecutor() {
        return mainExecuter;
    }

    private final Map<String, ICommand> commandMap;

    public CommandExecuter() {
        commandMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public Set<String> getCommandNames() {
        return commandMap.keySet();
    }

    public Collection<String> getSubCommandNames(String command) {
        ICommand cmd = commandMap.get(command);
        if (!(cmd instanceof CommandWithSubcommands))
            return null;
        return ((CommandWithSubcommands) cmd).getSubCommandNames();
    }

    public Collection<ICommand> getCommands() {
        return commandMap.values();
    }

    public Collection<String> exec(String[] args) throws CommandException, IOException, HarcHardwareException, IrpMasterException, AmbigousCommandException, AmbigousHardwareException {
        String commandName = args[0];
        List<String> candidates = Utils.findStringsWithPrefix(commandMap.keySet(), commandName);
        if (candidates.isEmpty())
            throw new NoSuchCommandException(commandName);
        if (candidates.size() > 1)
            throw new AmbigousCommandException(commandName);
        ICommand cmd = commandMap.get(candidates.get(0));
        String[] rest = new String[args.length-1];
        System.arraycopy(args, 1, rest, 0, args.length - 1);
        return cmd.exec(rest);
    }

    public void add(ICommand command) {
        if (commandMap.containsKey(command.getName()))
            throw new RuntimeException("Multiply defined command: " + command.getName()); // Programmer is to blame

        commandMap.put(command.getName(), command);
    }
}
