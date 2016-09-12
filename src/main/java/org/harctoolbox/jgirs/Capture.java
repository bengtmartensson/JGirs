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
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.ModulatedIrSequence;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.harctoolbox.harchardware.ir.ICapture;

/**
 * Module for capturing raw signals.
 */
public class Capture extends Module {

    private static final int defaultCaptureBeginTimeout  = 2000;
    private static final int defaultCaptureEndingTimeout = 30;
    private static final int defaultCaptureLength        = 400;

    public static final String RECEIVEBEGINTIMEOUT  = "captureBeginTimeout";
    public static final String RECEIVEENDINGTIMEOUT = "captureEndingTimeout";
    public static final String RECEIVELENGTH        = "captureLength";

    private static volatile Capture instance = null;

    public static Capture newCapture() {
        if (instance !=  null)
            throw new InvalidMultipleInstantiation();

        instance = new Capture();
        return instance;
    }

    public static String analyze() throws IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, HarcHardwareException, IOException, IrpMasterException {
        IHarcHardware hardware = Engine.getInstance().getCaptureHardware().getHardware();
        if (!(hardware instanceof ICapture))
            throw new IncompatibleHardwareException("ICapture");
        final ModulatedIrSequence irSequence = ((ICapture) hardware).capture();
        return irSequence.toPrintString(true, false);
    }

    private Capture() {
        super();
    }

    private static class AnalyzeCommand implements ICommand {

        private static final String ANALYZE = "analyze";

        @Override
        public String getName() {
            return ANALYZE;
        }

        @Override
        public List<String> exec(String[] args) throws HarcHardwareException, IOException, IrpMasterException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, CommandSyntaxException {
            //hardware.setTimeout(startTimeoutParameter.value, maxCaptureLengthParameter.value, endTimeoutParameter.value);
            checkNoArgs(ANALYZE, args.length, 0);
            return Utils.singletonArrayList(analyze());
        }
    }
}
