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
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.harchardware.HarcHardwareException;

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
     * @return String to be returned to the client
     * @throws java.io.IOException
     * @throws org.harctoolbox.harchardware.HarcHardwareException
     * @throws org.harctoolbox.jgirs.CommandException
     * @throws org.harctoolbox.jgirs.AmbigousCommandException
     * @throws org.harctoolbox.IrpMaster.IrpMasterException
     */
    public String exec(String[] args) throws IOException, HarcHardwareException, CommandException, AmbigousCommandException, IrpMasterException;
}
