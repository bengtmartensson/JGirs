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

import java.util.List;

/**
 * This class models transmitters as in HarcHardware.
 */
public class Transmitters extends Module {

    private static volatile Transmitters instance = null;

    public static Transmitters newTransmittersModule() {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new Transmitters();
        return instance;
    }
    //private final ITransmitter transmitter;

    private Transmitters() {
        super();
        addCommand(new TransmittersCommand());
    }

    private static class TransmittersCommand implements ICommand {

        @Override
        public String getName() {
            return "transmitters";
        }

        @Override
        public List<String> exec(String[] args) {
            return null; // TODO Utils.sortedString(transmitter.getTransmitterNames());
        }
    }
}
