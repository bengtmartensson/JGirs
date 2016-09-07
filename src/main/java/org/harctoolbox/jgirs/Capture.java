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
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.ModulatedIrSequence;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ir.ICapture;

/**
 * Class for capturing raw signals.
 */
public class Capture extends Module {

    private final ICapture hardware;
    private final StartTimeoutParameter startTimeoutParameter = null;
    private final MaxCaptureLengthParameter maxCaptureLengthParameter = null;
    private final EndTimeoutParameter endTimeoutParameter = null;
    private final UseCcfCaptureParameter useCcfCaptureParameter = null;
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

    public Capture(CommandExecuter commandExecuter, Parameters parameters) {
        super(commandExecuter, parameters);
        //this.engine = engine;
        this.hardware = null;//capture;
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

    private static class StartTimeoutParameter extends IntegerParameter {

        StartTimeoutParameter(int initValue) {
            super(initValue);
        }
        @Override
        public String getName() {
            return "starttimeout";
        }

        @Override
        public String getDocumentation() {
            return "Timeout in milliseconds while waiting for first pulse";
        }
    }

    private static class MaxCaptureLengthParameter extends IntegerParameter {

        MaxCaptureLengthParameter(int initValue) {
            super(initValue);
        }
        @Override
        public String getName() {
            return "maxcapturelength";
        }

        @Override
        public String getDocumentation() {
            return "Maximal capture length in milliseconds";
        }
    }

    private static class EndTimeoutParameter extends IntegerParameter {

        EndTimeoutParameter(int initValue) {
            super(initValue);
        }
        @Override
        public String getName() {
            return "endtimeout";
        }

        @Override
        public String getDocumentation() {
            return "Requred silece at end of capture";
        }
    }

    private static class UseCcfCaptureParameter extends BooleanParameter {

        UseCcfCaptureParameter(boolean initValue) {
            super(initValue);
        }

        @Override
        public String getName() {
            return "useccfcapture";
        }

        @Override
        public String getDocumentation() {
            return "If true, present captures in CCF form.";
        }
    }

    private class AnalyzeCommand implements ICommand {

        @Override
        public String getName() {
            return "analyze";
        }

        @Override
        public List<String> exec(List<String> args) throws HarcHardwareException, IOException, IrpMasterException {
            //hardware.setTimeout(startTimeoutParameter.value, maxCaptureLengthParameter.value, endTimeoutParameter.value);
            final ModulatedIrSequence irSequence = hardware.capture();
            return irSequence == null ? new ArrayList<>(8) : new ArrayList<String>(8) {{ add(useCcfCaptureParameter.getValue() ? irSequence.toIrSignal().ccfString() : irSequence.toPrintString(true, false)); }};
        }
    }
}
