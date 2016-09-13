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
import java.util.List;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.harctoolbox.harchardware.ir.NoSuchTransmitterException;

/**
 * Force open and close of a piece of hardware. This module is essentially for
 * debugging purposes, since the normal functions open and close as needed.
 */
public class OpenClose extends Module {

    public static void open(String name) throws NoSuchHardwareException, HarcHardwareException, IncompatibleHardwareException, IOException, NoSuchParameterException, AmbigousHardwareException {
        GirsHardware ghw = Engine.getInstance().getHardware(name);
        initializeHardware(ghw, IHarcHardware.class); // calls open
    }

    public static void close(String name) throws NoSuchHardwareException, HarcHardwareException, IncompatibleHardwareException, IOException, NoSuchParameterException, AmbigousHardwareException {
        IHarcHardware hardware = Engine.getInstance().getHardware(name).getHardware();
        if (hardware.isValid())
            hardware.close();
    }

    public static List<String> status(String name) throws NoSuchHardwareException, HarcHardwareException, IncompatibleHardwareException, IOException, NoSuchParameterException, AmbigousHardwareException {
        GirsHardware ghw = Engine.getInstance().getHardware(name);
        //initializeHardware(ghw, IHarcHardware.class);
        IHarcHardware hardware = ghw.getHardware();
        List<String> result = new ArrayList<>(2);
        result.add(hardware.isValid() ? "valid" : "not valid");
        result.add(hardware.getClass().getName());
        return result;
    }

    public OpenClose() {

        addCommand(new OpenCommand());
        addCommand(new CloseCommand());
        addCommand(new StatusCommand());
    }

    private static class OpenCommand implements ICommand {

        private static final String OPEN = "open";

        @Override
        public String getName() {
            return OPEN;
        }

        @Override
        public List<String> exec(String[] args) throws ExecutionException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, HarcHardwareException, NoSuchHardwareException, NoSuchParameterException, CommandSyntaxException, AmbigousHardwareException {
            checkNoArgs(OPEN, args.length, 1);
            open(args[0]);
            return new ArrayList<>(0);
        }
    }

    private static class CloseCommand implements ICommand {

        private static final String CLOSE = "close";

        @Override
        public String getName() {
            return CLOSE;
        }

        @Override
        public List<String> exec(String[] args) throws ExecutionException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, HarcHardwareException, NoSuchHardwareException, NoSuchParameterException, CommandSyntaxException, AmbigousHardwareException {
            checkNoArgs(CLOSE, args.length, 1);
            close(args[0]);
            return new ArrayList<>(0);
        }
    }

    private static class StatusCommand implements ICommand {

        private static final String STATUS = "status";

        @Override
        public String getName() {
            return STATUS;
        }

        @Override
        public List<String> exec(String[] args) throws ExecutionException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, HarcHardwareException, NoSuchHardwareException, NoSuchParameterException, CommandSyntaxException, AmbigousHardwareException {
            checkNoArgs(STATUS, args.length, 1);
            return status(args[0]);
        }
    }
}
