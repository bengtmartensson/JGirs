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

import java.util.Map;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.irp.DomainViolationException;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrpInvalidArgumentException;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.irp.Protocol;
import org.harctoolbox.irp.UnsupportedRepeatException;

/**
 *
 */
public class Irp extends Module {

    public static IrSignal render(String protocolName, Map<String, Long>parameters) throws UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException, DomainViolationException {
        Protocol protocol = new Protocol(protocolName);
        IrSignal irSignal = protocol.toIrSignal(parameters);
        return irSignal;
    }

    public Irp() {
        super();
    }
}
