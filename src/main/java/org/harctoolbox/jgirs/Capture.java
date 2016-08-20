/*
Copyright (C) 2015 Bengt Martensson.

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
 * Class for caputurinig raw signals.
 */
public class Capture extends Module {

    private ICapture hardware;
    private StartTimeoutParameter startTimeoutParameter;
    private MaxCaptureLengthParameter maxCaptureLengthParameter;
    private EndTimeoutParameter endTimeoutParameter;
    private UseCcfCaptureParameter useCcfCaptureParameter;

    private class AnalyzeCommand implements ICommand {

        @Override
        public String getName() {
            return "analyze";
        }

        @Override
        public List<String> exec(String[] args) throws HarcHardwareException, IOException, IrpMasterException {
            hardware.setTimeout(startTimeoutParameter.value, maxCaptureLengthParameter.value, endTimeoutParameter.value);
            final ModulatedIrSequence irSequence = hardware.capture();
            return irSequence == null ? new ArrayList<String>() : new ArrayList<String>() {{ add(useCcfCaptureParameter.value ? irSequence.toIrSignal().ccfString() : irSequence.toPrintString(true, false)); }};
        }
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

    public Capture(ICapture capture) {
        this.hardware = capture;
        addCommand(new AnalyzeCommand());
        startTimeoutParameter = new StartTimeoutParameter(2000);
        addParameter(startTimeoutParameter);
        maxCaptureLengthParameter = new MaxCaptureLengthParameter(2000);
        addParameter(maxCaptureLengthParameter);
        endTimeoutParameter = new EndTimeoutParameter(200);
        addParameter(endTimeoutParameter);
        useCcfCaptureParameter = new UseCcfCaptureParameter(false);
        addParameter(useCcfCaptureParameter);
    }
}
