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
import java.util.Collection;
import java.util.List;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.harchardware.HarcHardwareException;

/**
 * This class does something interesting and useful. Or not...
 */
public abstract class CommandWithSubcommands {

    public abstract String getName();

    private CommandExecuter commandExecuter;

    protected CommandWithSubcommands() {
        commandExecuter = new CommandExecuter();
    }

    protected void addCommand(Command command) {
        commandExecuter.addCommand(command);
    }

    public Collection<Command> getSubCommands() {
        return commandExecuter.getCommands();
    }

    public ArrayList<String> getSubCommandNames(boolean sort) {
        return commandExecuter.getCommandNames(sort);
    }

    public List<String> exec(String[] args) throws NoSuchCommandException, ExecutionException, CommandSyntaxException, NoSuchModuleException, AmbigousCommandException, IOException, HarcHardwareException, IrpMasterException, NoSuchRemoteException, NoSuchParameterException, NonExistingCommandException {
        if (args == null || args.length == 0)
            throw new CommandSyntaxException(getName(), "subcommand missing");
        String[] rest = new String[args.length - 1];
        System.arraycopy(args, 1, rest, 0, args.length - 1);
        return commandExecuter.exec(rest);
    }
}
