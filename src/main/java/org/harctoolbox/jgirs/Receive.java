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
    private final static String defaultReceiveFormat = "named-command";

    private final IReceive hardware;
    private final TimeoutParameter timeoutParameter = null;
    private final FallbackFrequencyParameter fallbackFrequencyParameter = null;
    private final ReceiveFormatParameter receiveFormatParameter = null;
    private final NamedRemotes namedRemotes;

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

    Receive(CommandExecuter commandExecuter, Parameters parameters, NamedRemotes namedRemotes) {
        super(commandExecuter, parameters);
        this.namedRemotes = namedRemotes;
        hardware = null; // FIXME
    }

    private static class TimeoutParameter extends IntegerParameter {

        TimeoutParameter(int initValue) {
            super(initValue);
        }
        @Override
        public String getName() {
            return "receivetimeout";
        }

        @Override
        public String getDocumentation() {
            return "Timeout in milliseconds";
        }
    }

    private static class FallbackFrequencyParameter extends IntegerParameter {

        FallbackFrequencyParameter(int initValue) {
            super(initValue);
        }

        @Override
        public String getName() {
            return "defaultfrequency";
        }

        @Override
        public String getDocumentation() {
            return "Fallback frequency to be used in absence of a measurement.";
        }
    }

    private static class ReceiveFormatParameter extends StringParameter {

        ReceiveFormatParameter(String initValue) {
            super(initValue);
        }

        @Override
        public String getName() {
            return "receiveformat";
        }

        @Override
        public String getDocumentation() {
            return "Format of received codes (as long as possible). Possible values are: "
                    + "raw, ccf, protocol-parameter, named-command";
        }
    }

    private class ReceiveCommand implements ICommand {

        @Override
        public String getName() {
            return "receive";
        }

        @Override
        public String[] exec(String[] args) throws HarcHardwareException, IOException, IrpMasterException {
            hardware.setTimeout(timeoutParameter.getValue());
            final IrSequence irSequence = hardware.receive();

            final IrSignal irSignal = new IrSignal(fallbackFrequencyParameter.getValue(), IrpUtils.invalid, irSequence, null, null);
            switch (receiveFormatParameter.getValue()) {
                case "ccf":
                    return new String[] { irSignal.ccfString() };
                case "protocol-parameter":
                {
                    boolean success = DecodeIR.loadLibrary();
                    if (!success)
                        return null;
                    DecodeIR.DecodedSignal[] decodes = DecodeIR.decode(irSequence, fallbackFrequencyParameter.getValue());
                    String[] result = new String[decodes.length];
                    for (int i = 0; i < decodes.length; i++)
                        result[i] = decodes[i].toString();

                    return result;
                }
                case "named-command":
                {
                    boolean success = DecodeIR.loadLibrary();
                    if (!success)
                        return null;
                    DecodeIR.DecodedSignal[] decodes = DecodeIR.decode(irSequence, fallbackFrequencyParameter.getValue());
                    if (decodes.length == 0)
                        return null;
                    final RemoteCommandDataBase.RemoteCommand cmd = namedRemotes.getRemoteCommand(decodes[0].getProtocol(), decodes[0].getParameters());
                    return cmd != null ? new String[] { cmd.toString() } : null;
                }
                default:
                    return new String[] { irSequence.toPrintString(true, false) };
            }
        }
    }
}
