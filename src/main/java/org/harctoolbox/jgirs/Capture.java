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

    //private final ICapture hardware;
//    private final StartTimeoutParameter startTimeoutParameter = null;
//    private final MaxCaptureLengthParameter maxCaptureLengthParameter = null;
//    private final EndTimeoutParameter endTimeoutParameter = null;
//    private final UseCcfCaptureParameter useCcfCaptureParameter = null;
    //private Engine engine;

//    public Capture(ICapture capture) {
//        this.hardware = capture;
//        addCommand(new AnalyzeCommand());
//        startTimeoutParameter = new StartTimeoutParameter(2000);
//        addParameter(startTimeoutParameter);
//        maxCaptureLengthParameter = new MaxCaptureLengthParameter(2000);
//        addParameter(maxCaptureLengthParameter);
//        endTimeoutParameter = new EndTimeoutParameter(200);
//        addParameter(endTimeoutParameter);
//        useCcfCaptureParameter = new UseCcfCaptureParameter(false);
//        addParameter(useCcfCaptureParameter);
//    }

    private Capture() {
        super();
        //this.engine = engine;
        //this.hardware = null;//capture;
//        addCommand(new AnalyzeCommand());
//        startTimeoutParameter = new StartTimeoutParameter(2000);
//        addParameter(startTimeoutParameter);
//        maxCaptureLengthParameter = new MaxCaptureLengthParameter(2000);
//        addParameter(maxCaptureLengthParameter);
//        endTimeoutParameter = new EndTimeoutParameter(200);
//        addParameter(endTimeoutParameter);
//        useCcfCaptureParameter = new UseCcfCaptureParameter(false);
//        addParameter(useCcfCaptureParameter);
    }

//    private static class StartTimeoutParameter extends IntegerParameter {
//
//        StartTimeoutParameter(int initValue) {
//            super(initValue);
//        }
//        @Override
//        public String getName() {
//            return "starttimeout";
//        }
//
//        @Override
//        public String getDocumentation() {
//            return "Timeout in milliseconds while waiting for first pulse";
//        }
//    }
//
//    private static class MaxCaptureLengthParameter extends IntegerParameter {
//
//        MaxCaptureLengthParameter(int initValue) {
//            super(initValue);
//        }
//        @Override
//        public String getName() {
//            return "maxcapturelength";
//        }
//
//        @Override
//        public String getDocumentation() {
//            return "Maximal capture length in milliseconds";
//        }
//    }
//
//    private static class EndTimeoutParameter extends IntegerParameter {
//
//        EndTimeoutParameter(int initValue) {
//            super(initValue);
//        }
//        @Override
//        public String getName() {
//            return "endtimeout";
//        }
//
//        @Override
//        public String getDocumentation() {
//            return "Requred silece at end of capture";
//        }
//    }
//
//    private static class UseCcfCaptureParameter extends BooleanParameter {
//
//        UseCcfCaptureParameter(boolean initValue) {
//            super(initValue);
//        }
//
//        @Override
//        public String getName() {
//            return "useccfcapture";
//        }
//
//        @Override
//        public String getDocumentation() {
//            return "If true, present captures in CCF form.";
//        }
//    }
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
            IHarcHardware hardware = Engine.getInstance().getCaptureHardware().getHardware();
            if (!(hardware instanceof ICapture))
                throw new IncompatibleHardwareException("ICapture");
            final ModulatedIrSequence irSequence = ((ICapture) hardware).capture();
            return Utils.singletonArrayList(irSequence.toPrintString(true, false));
        }
    }
}
