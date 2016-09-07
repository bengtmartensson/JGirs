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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.harctoolbox.IrpMaster.DomainViolationException;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.InvalidRepeatException;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.IrpMaster;
import org.harctoolbox.IrpMaster.ParseException;
import org.harctoolbox.IrpMaster.Protocol;
import org.harctoolbox.IrpMaster.UnassignedException;
import org.harctoolbox.IrpMaster.UnknownProtocolException;

/**
 * Implements rendering commands.
 */
public class Renderer extends Module {
    private IrpMaster irpMaster;

    public Renderer(CommandExecuter commandExecuter, Parameters parameters, String irpMasterIniName) throws FileNotFoundException, IncompatibleArgumentException {
        super(commandExecuter, parameters);
        if (irpMasterIniName == null)
            throw new NullPointerException();
        irpMaster = new IrpMaster(irpMasterIniName);
        org.harctoolbox.girr.Command.setIrpMaster(irpMaster);
        addCommand(new ProtocolsCommand());
    }

    public IrSignal render(String[] args, int skip) throws UnassignedException, ParseException, IncompatibleArgumentException, UnknownProtocolException, DomainViolationException, InvalidRepeatException {
        int index = skip;
        String protocol = args[index++];
        HashMap<String, Long> params = Protocol.parseParams(args, index);
        IrSignal irSignal = irpMaster.newProtocol(protocol).renderIrSignal(params);
        return irSignal;
    }

    private class ProtocolsCommand implements ICommand {

        @Override
        public String getName() {
            return "protocols";
        }

        @Override
        public List<String> exec(List<String> args) {
            return new ArrayList<>(irpMaster.getNames());
        }
    }
}
