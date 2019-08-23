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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpException;
import org.harctoolbox.irp.IrpParseException;
import org.harctoolbox.irp.NameEngine;

/**
 * Implements rendering commands.
 */
public class Renderer extends Module {

    private IrpDatabase irpDatabase;

    public Renderer(String irpProtocolsName) throws FileNotFoundException, IOException, IrpParseException {
        super();
        if (irpProtocolsName == null)
            throw new NullPointerException();
        irpDatabase = new IrpDatabase(irpProtocolsName);
        org.harctoolbox.girr.Command.setIrpMaster(irpDatabase);
        addCommand(new ProtocolsCommand());
    }

    public IrSignal render(String[] args, int skip) throws IrpException {
        int index = skip;
        String protocol = args[index++];
        String[] defs = new String[args.length - index];
        System.arraycopy(args, index, defs, 0, args.length - index);
        NameEngine params = NameEngine.parse(args);
        return render(protocol, params.toMap());
    }

    public IrSignal render(String protocol, Map<String, Long> params) throws IrpException {
        return irpDatabase.render(protocol, params);
        //Master.newProtocol(protocol).renderIrSignal(params);
    }

    public List<String> protocols() {
        return irpDatabase.getNames();
        //return Utils.toSortedList(irpMaster.getNames());
    }

    private class ProtocolsCommand implements ICommand {

        private static final String PROTOCOLS = "protocols";

        @Override
        public String getName() {
            return PROTOCOLS;
        }

        @Override
        public List<String> exec(String[] args) throws NoSuchParameterException, CommandSyntaxException {
            checkNoArgs(PROTOCOLS, args.length, 0);
            return protocols();
        }
    }
}
