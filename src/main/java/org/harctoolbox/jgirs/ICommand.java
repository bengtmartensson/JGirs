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
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.irp.IrpException;

/**
 * Defines the functions the Commands must implements.
 */
public interface ICommand {

    /**
     *
     * @return name of the command.
     */
    public String getName();

    /**
     * Executes the command
     * @param args argument list in the Unix sense, i.e., containing the command name as the first argument args[0].
     * @return Strings to be returned to the client, null for failure, non-null (possibly a list of length 0) indicates success.
     * @throws java.io.IOException
     * @throws org.harctoolbox.harchardware.HarcHardwareException
     * @throws org.harctoolbox.jgirs.CommandException
     * @throws org.harctoolbox.jgirs.AmbigousHardwareException
     * @throws org.harctoolbox.jgirs.AmbigousCommandException
     * @throws org.harctoolbox.ircore.InvalidArgumentException
     * @throws org.harctoolbox.irp.IrpException
     */
    public Collection<String> exec(String[] args) throws IOException, HarcHardwareException, CommandException, AmbigousHardwareException, AmbigousCommandException, InvalidArgumentException, IrCoreException, IrpException;
}
