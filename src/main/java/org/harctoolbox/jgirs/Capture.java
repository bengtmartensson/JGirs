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
import java.util.List;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ir.ICapture;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.ModulatedIrSequence;
import static org.harctoolbox.jgirs.Engine.TIMEOUT;

/**
 * Module for capturing raw signals.
 */
public class Capture extends Module {

    private static final int defaultCaptureBeginTimeout  = 5000;
    private static final int defaultCaptureEndingTimeout = 200;
    private static final int defaultCaptureLength        = 400;

    public static final String CAPTUREBEGINTIMEOUT  = "captureBeginTimeout";
    public static final String CAPTUREENDINGTIMEOUT = "captureEndingTimeout";
    public static final String CAPTURELENGTH        = "captureLength";

    private static final boolean alternatingSigns = true;
    private static final boolean noSigns = false;

    private static volatile Capture instance = null;

    public static Capture newCapture() {
        if (instance !=  null)
            throw new InvalidMultipleInstantiation();

        instance = new Capture();
        return instance;
    }

    public static ModulatedIrSequence capture() throws NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException, AmbigousHardwareException, InvalidArgumentException {
        GirsHardware hardware = Engine.getInstance().getCaptureHardware();
        return capture(hardware);
    }

    public static ModulatedIrSequence capture(GirsHardware hardware) throws IncompatibleHardwareException, HarcHardwareException, IOException, NoSuchParameterException, InvalidArgumentException {
        initializeHardware(hardware, ICapture.class);
        ICapture capturer = (ICapture) hardware.getHardware();
        capturer.setBeginTimeout(Parameters.getInstance().getInteger(CAPTUREBEGINTIMEOUT));
        capturer.setCaptureMaxSize(Parameters.getInstance().getInteger(CAPTURELENGTH));
        capturer.setEndingTimeout(Parameters.getInstance().getInteger(CAPTUREENDINGTIMEOUT));
        ModulatedIrSequence irSequence = ((ICapture) hardware.getHardware()).capture();
        return irSequence;
    }

    private Capture() {
        super();
        addCommand(new CaptureCommand());
        addParameter(new IntegerParameter(CAPTUREBEGINTIMEOUT, defaultCaptureBeginTimeout, null));
        addParameter(new IntegerParameter(CAPTUREENDINGTIMEOUT, defaultCaptureEndingTimeout, null));
        addParameter(new IntegerParameter(CAPTURELENGTH, defaultCaptureLength, null));
    }

    private static class CaptureCommand implements ICommand {

        private static final String ANALYZE = "analyze";
        private static final String SEPARATOR = " ";

        @Override
        public String getName() {
            return ANALYZE;
        }

        @Override
        public List<String> exec(String[] args) throws HarcHardwareException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, CommandSyntaxException, AmbigousHardwareException, InvalidArgumentException {
            //hardware.setTimeout(startTimeoutParameter.value, maxCaptureLengthParameter.value, endTimeoutParameter.value);
            checkNoArgs(ANALYZE, args.length, 0, 1);
            ModulatedIrSequence irSequence = args.length == 0 ? capture() : capture(Engine.getInstance().getHardware(args[0]));
            String string = irSequence == null ? TIMEOUT
                    : irSequence.toString(alternatingSigns, SEPARATOR);
            return Utils.singletonArrayList(string);
        }
    }
}
