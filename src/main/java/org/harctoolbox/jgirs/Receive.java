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
import org.harctoolbox.IrpMaster.DecodeIR;
import org.harctoolbox.IrpMaster.IrSequence;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.IrpUtils;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ir.IReceive;

/**
 * This class implements the receiving commands.
 */
public class Receive extends Module {
    private static final int defaultReceiveBeginTimeout = 2000;
    private static final int defaultReceiveEndingTimeout = 30;
    private static final int defaultReceiveLength = 400;
    private static final ReceiveFormat defaultReceiveFormat = ReceiveFormat.raw;

    public static final String RECEIVEBEGINTIMEOUT = "receivebegintimeout";
    public static final String RECEIVEENDTIMEOUT = "receiveendtimeout";
    public static final String RECEIVELENGTH = "receivelength";
    public static final String FALLBACKFREQUENCY = "fallbackfrequency";
    public static final String RECEIVEFORMAT = "receiveformat";
    private static volatile Receive instance;

    static Module newReceive(NamedRemotes namedRemotes) {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new Receive(namedRemotes);
        return instance;
    }

    //private IReceive hardware;
//    private IntegerParameter beginTimeout;
//    private IntegerParameter receiveLength;
//    private IntegerParameter endingTimeout;
//    private IntegerParameter fallbackFrequency;
//    private ReceiveFormatParameter receiveFormat;

    private final NamedRemotes namedRemotes;
    //private ReceiveFormat receiveFormat;
    //private final GirsHardware girsHardware;

//    public Receive(IReceive receiver, NamedRemotes namedRemotes) {
//        this.namedRemotes = namedRemotes;
//        this.hardware = receiver;
//        addCommand(new ReceiveCommand());
//        timeoutParameter = new TimeoutParameter(2000);
//        addParameter(timeoutParameter);
//        this.fallbackFrequencyParameter = new FallbackFrequencyParameter((int) IrpUtils.defaultFrequency);
//        addParameter(this.fallbackFrequencyParameter);
//        this.receiveFormatParameter = new ReceiveFormatParameter(defaultReceiveFormat);
//        addParameter(this.receiveFormatParameter);
//
//    }

//    public static Receive newReceive() {
//        if (instance != null)
//            throw new InvalidMultipleInstantiation();
//
//        instance = new Receive();
//        return instance;
//    }

    public Receive(NamedRemotes namedRemotes) {
        super();
        this.namedRemotes = namedRemotes;
        //this.girsHardware = girsHardware;
        addCommand(new ReceiveCommand());

        addParameter(new IntegerParameter(RECEIVEBEGINTIMEOUT, defaultReceiveBeginTimeout, "begin timeout for receive"));
        addParameter(new IntegerParameter(RECEIVELENGTH, defaultReceiveLength, "max number of durations in receive"));
        addParameter(new IntegerParameter(RECEIVEENDTIMEOUT, defaultReceiveEndingTimeout, "ending timeout for receive"));
        addParameter(new IntegerParameter(FALLBACKFREQUENCY, (int) IrpUtils.defaultFrequency,
                "Fallback frequency (in Hz) to be used in absence of a measurement."));
        addParameter(new ReceiveFormatParameter("receiveformat", defaultReceiveFormat,
                "Format of received codes (to the extent possible). Possible values are: raw, ccf, protocolparameter, namedcommand"));
    }

    public static enum ReceiveFormat {
        raw,
        ccf,
        protocolparameter,
        namedcommand;
    };

//    private static class TimeoutParameter extends IntegerParameter {
//
//        TimeoutParameter(int initValue) {
//            super(initValue);
//        }
//        @Override
//        public String getName() {
//            return "receivetimeout";
//        }
//
//        @Override
//        public String getDocumentation() {
//            return "Timeout in milliseconds";
//        }
//    }

//    private static class FallbackFrequencyParameter extends IntegerParameter {
//
//        FallbackFrequencyParameter(int initValue) {
//            super(initValue);
//        }
//
//        @Override
//        public String getName() {
//            return "defaultfrequency";
//        }
//
//        @Override
//        public String getDocumentation() {
//            return "Fallback frequency to be used in absence of a measurement.";
//        }
//    }

    private static class ReceiveFormatParameter extends Parameter {

        ReceiveFormat value;

        ReceiveFormatParameter(String name, ReceiveFormat initValue, String documentation) {
            super(name, documentation);
            value = initValue;
        }

        @Override
        public void set(String str) {
            set(ReceiveFormat.valueOf(str));
        }

        public void set(ReceiveFormat receiveFormat) {
            value = receiveFormat;
        }

        @Override
        public void set(Object value) {
            this.value = (ReceiveFormat) value;
        }

        @Override
        public String get() {
            return value.toString();
        }

        public ReceiveFormat getValue() {
            return value;
        }
    }

    private class ReceiveCommand implements ICommand {

        @Override
        public String getName() {
            return "receive";
        }

        @Override
        public String exec(String[] args) throws HarcHardwareException, IOException, IrpMasterException, IncompatibleHardwareException {
            if (!(GirsHardware.getDefaultReceivingHardware().getHardware() instanceof IReceive))
                throw new IncompatibleHardwareException("IReceive");
            IReceive hardware = (IReceive) GirsHardware.getDefaultReceivingHardware().getHardware();
            hardware.setVerbosity(ParameterModule.getInstance().getBoolean("verbosity"));
            if (!hardware.isValid())
                hardware.open();

            hardware.setBeginTimeout(ParameterModule.getInstance().getInteger(RECEIVEBEGINTIMEOUT));
            final IrSequence irSequence = hardware.receive();

            final IrSignal irSignal = new IrSignal(ParameterModule.getInstance().getInteger(FALLBACKFREQUENCY), IrpUtils.invalid, irSequence, null, null);
            ReceiveFormat receiveFormat = ((ReceiveFormatParameter) ParameterModule.getInstance().get(RECEIVEFORMAT)).value;
            switch (receiveFormat) {
                case ccf:
                    return irSignal.ccfString();
                case protocolparameter:
                {
                    boolean success = DecodeIR.loadLibrary();
                    if (!success)
                        return null;
                    DecodeIR.DecodedSignal[] decodes = DecodeIR.decode(irSequence, ParameterModule.getInstance().getInteger(FALLBACKFREQUENCY));
                    List<String> result = new ArrayList<>(decodes.length);
                    for (DecodeIR.DecodedSignal decode : decodes)
                        result.add(decode.toString());

                    return String.join(";", result);
                }
                case namedcommand:
                {
                    boolean success = DecodeIR.loadLibrary();
                    if (!success)
                        return null;
                    DecodeIR.DecodedSignal[] decodes = DecodeIR.decode(irSequence, ParameterModule.getInstance().getInteger(FALLBACKFREQUENCY));
                    if (decodes.length == 0)
                        return null;
                    final RemoteCommandDataBase.RemoteCommand cmd = namedRemotes.getRemoteCommand(decodes[0].getProtocol(), decodes[0].getParameters());
                    return cmd != null ? cmd.toString() : null;
                }
                case raw:
                    return irSequence.toPrintString(true, false);
                default:
                    throw new RuntimeException("Programming error");
            }
        }
    }
}
