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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ir.IReceive;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.ircore.OddSequenceLengthException;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.Decoder;
import org.harctoolbox.irp.IrpDatabase;
import org.harctoolbox.irp.IrpParseException;
import static org.harctoolbox.jgirs.Engine.TIMEOUT;

/**
 * This class implements the receiving commands.
 */
public class Receive extends Module {
    private static final int defaultReceiveBeginTimeout     = 2000;
    private static final int defaultReceiveEndingTimeout    = 30;
    private static final int defaultReceiveLength           = 400;
    private static final ReceiveFormat defaultReceiveFormat = ReceiveFormat.raw;

    public static final String RECEIVEBEGINTIMEOUT  = "receiveBeginTimeout";
    public static final String RECEIVEENDINGTIMEOUT = "receiveEndingTimeout";
    public static final String RECEIVELENGTH        = "receiveLength";
    public static final String FALLBACKFREQUENCY    = "fallbackFrequency";
    public static final String RECEIVEFORMAT        = "receiveFormat";

    private static volatile Receive instance;

    public static Module newReceive() {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new Receive();
        return instance;
    }

    public static IrSequence receive(GirsHardware hardware) throws IncompatibleHardwareException, HarcHardwareException, IOException, NoSuchParameterException, OddSequenceLengthException {
        initializeHardware(hardware, IReceive.class);
        IReceive receiver = (IReceive) hardware.getHardware();
        receiver.setBeginTimeout(Parameters.getInstance().getInteger(RECEIVEBEGINTIMEOUT));
        receiver.setEndingTimeout(Parameters.getInstance().getInteger(RECEIVEBEGINTIMEOUT));
        receiver.setBeginTimeout(Parameters.getInstance().getInteger(RECEIVEBEGINTIMEOUT));
        IrSequence irSequence = receiver.receive();
        return irSequence;
    }

    public static IrSequence receive() throws NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException, AmbigousHardwareException, OddSequenceLengthException {
        return receive(Engine.getInstance().getReceiveHardware());
    }

    public static IrSequence receive(String hardwareName) throws IncompatibleHardwareException, HarcHardwareException, IOException, NoSuchParameterException, NoSuchHardwareException, AmbigousHardwareException, OddSequenceLengthException {
        return receive(Engine.getInstance().getHardware(hardwareName));
    }

    public static String formatAsRaw(IrSequence irSequence) {
        return irSequence.toString(true);
    }

    public static String formatAsCcf(IrSequence irSequence) throws NoSuchParameterException {
        IrSignal irSignal = new IrSignal(irSequence, null, null, (double) Parameters.getInstance().getInteger(FALLBACKFREQUENCY), null);
        return Pronto.toString(irSignal);
    }

    public static List<String> formatAsDecode(IrSequence irSequence) throws NoSuchParameterException {
//        boolean success = DecodeIR.loadLibrary();
//        if (!success)
//            return null;
        ModulatedIrSequence modulatedIrSequence = new ModulatedIrSequence(irSequence, Double.valueOf(Parameters.getInstance().getInteger(FALLBACKFREQUENCY)));
        Decoder.DecodeTree decodes = instance.decoder.decode(modulatedIrSequence, instance.decoderParams);
        List<String> result = new ArrayList<>(10);
        Iterator<Decoder.TrunkDecodeTree> iterator = decodes.iterator();
        while (iterator.hasNext()) {
            Decoder.Decode decode = iterator.next().getTrunk();
            result.add(decode.toString());
        }

        return result;
    }

    public static List<String> formatAsNamedCommands(IrSequence irSequence) throws NoSuchParameterException {
//        boolean success = DecodeIR.loadLibrary();
//        if (!success)
//            return null;
        ModulatedIrSequence modulatedIrSequence = new ModulatedIrSequence(irSequence, Double.valueOf(Parameters.getInstance().getInteger(FALLBACKFREQUENCY)));
        Decoder.DecodeTree decodes = instance.decoder.decode(modulatedIrSequence, instance.decoderParams);
        Iterator<Decoder.TrunkDecodeTree> iterator = decodes.iterator();
        if (!iterator.hasNext())
            return null;

        Decoder.Decode decode = iterator.next().getTrunk();
        ProtocolParameter protocolParameters = new ProtocolParameter(decode.getName(), decode.getMap());
        RemoteCommandDataBase.RemoteCommand cmd = NamedRemotes.getInstance().getRemoteCommand(protocolParameters);
        List<String> list = new ArrayList<>(1);
        if (cmd != null)
            list.add(cmd.toString());
        return list;
    }

    public static List<String> format(IrSequence irSequence, ReceiveFormat receiveFormat)
            throws NoSuchParameterException {
        return
                irSequence == null ? Utils.singletonArrayList(TIMEOUT)
                : receiveFormat == ReceiveFormat.raw ? Utils.singletonArrayList(formatAsRaw(irSequence))
                : receiveFormat == ReceiveFormat.ccf ? Utils.singletonArrayList(formatAsCcf(irSequence))
                : receiveFormat == ReceiveFormat.protocolparameter ? formatAsDecode(irSequence)
                : formatAsNamedCommands(irSequence);
    }

    private final Decoder decoder;
    private final Decoder.DecoderParameters decoderParams;

    public Receive() {
        super();
        try {
            IrpDatabase irpDatabase = new IrpDatabase((String) null);
            decoder = new Decoder(irpDatabase);
        } catch (IOException | IrpParseException ex) {
            // If I get here, it is an internal problem; don't expect a user to be able to do anything.
            Logger.getLogger(Receive.class.getName()).log(Level.SEVERE, null, ex);
            throw new ThisCannotHappenException();
        }

        decoderParams = new Decoder.DecoderParameters();

        addCommand(new ReceiveCommand());

        addParameter(new IntegerParameter(RECEIVEBEGINTIMEOUT, defaultReceiveBeginTimeout, "begin timeout for receive"));
        addParameter(new IntegerParameter(RECEIVELENGTH, defaultReceiveLength, "max number of durations in receive"));
        addParameter(new IntegerParameter(RECEIVEENDINGTIMEOUT, defaultReceiveEndingTimeout, "ending timeout for receive"));
        addParameter(new IntegerParameter(FALLBACKFREQUENCY, (int) ModulatedIrSequence.DEFAULT_FREQUENCY,
                "Fallback frequency (in Hz) to be used in absence of a measurement."));
        addParameter(new ReceiveFormatParameter(RECEIVEFORMAT, defaultReceiveFormat,
                "Format of received codes (to the extent possible). Possible values are: raw, ccf, protocolparameter, namedcommand"));
    }

    public static enum ReceiveFormat {
        raw,
        ccf,
        protocolparameter,
        namedcommand;
    };

    private static class ReceiveFormatParameter extends Parameter {

        ReceiveFormat value;

        ReceiveFormatParameter(String name, ReceiveFormat initValue, String documentation) {
            super(name, documentation);
            value = initValue;
        }

        @Override
        public void set(String str) {
            for (ReceiveFormat format : ReceiveFormat.values())
                if (format.toString().startsWith(str)) {
                    value = format;
                    return;
                }
            throw new IllegalArgumentException();
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

    private static class ReceiveCommand implements ICommand {
        private static final String RECEIVE = "receive";

        @Override
        public String getName() {
            return RECEIVE;
        }

        @Override
        public List<String> exec(String[] args) throws HarcHardwareException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, CommandSyntaxException, AmbigousHardwareException, OddSequenceLengthException {
            checkNoArgs(RECEIVE, args.length, 0, 1);
            IrSequence irSequence = args.length == 0 ? receive() : receive(args[0]);
            return format(irSequence, ((ReceiveFormatParameter) Parameters.getInstance().get(RECEIVEFORMAT)).getValue());
        }
    }
}
