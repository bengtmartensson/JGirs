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
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.irp.IrpException;

/**
 * This is an abstract base class for commands containing subcommands.
 */
public abstract class CommandWithSubcommands {

    private final CommandExecuter commandExecuter;

    protected CommandWithSubcommands() {
        commandExecuter = new CommandExecuter();
    }

    public abstract String getName();

    protected final void addCommand(ICommand command) {
        commandExecuter.add(command);
    }

    public Collection<ICommand> getSubCommands() {
        return commandExecuter.getCommands();
    }

    public Collection<String> getSubCommandNames() {
        return commandExecuter.getCommandNames();
    }

    public Collection<String> exec(String[] args) throws IOException, IrCoreException, IrpException, CommandSyntaxException, AmbigousCommandException, CommandException, NoSuchCommandException, HarcHardwareException, AmbigousHardwareException {
        if (args == null || args.length == 0)
            throw new CommandSyntaxException(getName(), "subcommand missing");
        return commandExecuter.exec(args);
    }
}
