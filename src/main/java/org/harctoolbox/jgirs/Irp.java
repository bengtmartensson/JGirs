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

import java.util.HashMap;
import org.harctoolbox.IrpMaster.DomainViolationException;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.InvalidRepeatException;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.ParseException;
import org.harctoolbox.IrpMaster.Protocol;
import org.harctoolbox.IrpMaster.UnassignedException;

/**
 *
 */
public class Irp extends Module {

    public IrSignal render(String[] args, int skip) throws UnassignedException, ParseException, IncompatibleArgumentException, DomainViolationException, InvalidRepeatException {
        int index = skip;
        String irp = args[index++];
        Protocol protocol = new Protocol("dummy", irp, null);
        HashMap<String, Long> params = Protocol.parseParams(args, index);
        IrSignal irSignal = protocol.renderIrSignal(params);
        return irSignal;
    }

    public Irp() {
        super();
    }
}
