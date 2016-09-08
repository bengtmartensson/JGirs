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

import java.util.HashMap;
import org.harctoolbox.IrpMaster.DomainViolationException;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.InvalidRepeatException;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.ParseException;
import org.harctoolbox.IrpMaster.Protocol;
import org.harctoolbox.IrpMaster.UnassignedException;

/**
 * This class defines commands for rendering IR signals from their Protocol and parameters.
 */
public class Irp extends Module {

//    public static Irp getInstance() {
//        return (Irp) instance;
//    }
//
//    public static Irp newIrp() {
//        Irp irp = new Irp();
//        instance = irp;
//        return irp;
//    }

    public Irp() {
        super();
    }

    public IrSignal render(String protocolName, HashMap<String, Long>parameters) throws UnassignedException, ParseException, IncompatibleArgumentException, DomainViolationException, InvalidRepeatException {
        Protocol protocol = new Protocol("dummy", protocolName, null);
        IrSignal irSignal = protocol.renderIrSignal(parameters);
        return irSignal;
    }
}
