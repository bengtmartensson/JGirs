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

import java.util.Arrays;
import java.util.List;
import org.harctoolbox.harchardware.ir.ITransmitter;

/**
 * This class models transmitters as in HarcHardware.
 */
public class Transmitters extends Module {
    private final ITransmitter transmitter;
    private Engine engine;

    public Transmitters(CommandExecuter commandExecuter, Parameters parameters, ITransmitter transmitter) {
        super(commandExecuter, parameters);
        this.transmitter = transmitter;
        addCommand(new TransmittersCommand());
    }

    public Transmitters(CommandExecuter commandExecuter, Parameters parameters) {
        this(commandExecuter, parameters, null);
    }

    private class TransmittersCommand implements ICommand {

        @Override
        public String getName() {
            return "transmitters";
        }

        @Override
        public List<String> exec(List<String> args) {
            return Arrays.asList(transmitter.getTransmitterNames());
        }
    }
}
