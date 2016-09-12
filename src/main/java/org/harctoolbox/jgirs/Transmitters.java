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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ir.ITransmitter;
import org.harctoolbox.harchardware.ir.Transmitter;

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

    public static List<String> transmitters(GirsHardware hardware)
            throws NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException {
        initializeHardware(hardware, ITransmitter.class);
        ITransmitter irsender = (ITransmitter) hardware.getHardware();
        String[] transmitterNames = irsender.getTransmitterNames();
        List<String> list = Arrays.asList(transmitterNames);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public static List<String> transmitters(String hardwareName) throws NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException {
        GirsHardware hardware = Engine.getInstance().getHardware(hardwareName);
        return transmitters(hardware);
    }

    public static List<String> transmitters() throws NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException {
        GirsHardware hardware = Engine.getInstance().getTransmitHardware();
        return transmitters(hardware);
    }

    public static Transmitters getInstance() {
        return instance;
    }

    private final HashMap<String, Transmitter>transmitterMap;

    private Transmitters() {
        super();
        transmitterMap = new HashMap<>(4);
        addCommand(new GetTransmittersCommand());
        addCommand(new SetTransmittersCommand());
    }

    public void setTransmitter(GirsHardware hardware, Transmitter transmitter) {
        transmitterMap.put(hardware.getName(), transmitter);
    }

    public void setTransmitter(GirsHardware hardware, String transmitterName) throws IncompatibleHardwareException, HarcHardwareException, IOException, NoSuchParameterException {
        initializeHardware(hardware, ITransmitter.class);
        ITransmitter irsender = (ITransmitter) hardware.getHardware();
        Transmitter transmitter = irsender.getTransmitter(transmitterName);
        setTransmitter(hardware, transmitter);
    }

    public void setTransmitter(String hardwareName, String transmitterName)
            throws NoSuchHardwareException, IncompatibleHardwareException, HarcHardwareException, IOException, NoSuchParameterException {
        GirsHardware hardware = Engine.getInstance().getHardware(hardwareName);
        setTransmitter(hardware, transmitterName);
    }

    public void setTransmitter(String transmitterName)
            throws NoSuchHardwareException, IncompatibleHardwareException, HarcHardwareException, IOException, NoSuchParameterException {
        GirsHardware hardware = Engine.getInstance().getTransmitHardware();
        setTransmitter(hardware, transmitterName);
    }

    public Transmitter getTransmitter(GirsHardware hardware) {
        return transmitterMap.get(hardware.getName());
    }

    private static class GetTransmittersCommand implements ICommand {

        private static final String GETTRANSMITTERS = "gettransmitters";

        @Override
        public String getName() {
            return GETTRANSMITTERS;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException, NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException {
            checkNoArgs(GETTRANSMITTERS, args.length, 0, 1);
            return args.length == 0 ? transmitters() : transmitters(args[0]);
        }
    }

    private class SetTransmittersCommand implements ICommand {

        private static final String SETTRANSMITTER = "settransmitter";

        @Override
        public String getName() {
            return SETTRANSMITTER;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException, NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException {
            checkNoArgs(SETTRANSMITTER, args.length, 1, 2);
            if (args.length == 1)
                setTransmitter(args[0]);
            else
                setTransmitter(args[0], args[1]);
            return new ArrayList<>(0);
        }
    }
}
