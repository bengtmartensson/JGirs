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
import java.util.Map;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.ir.IRawIrSender;
import org.harctoolbox.harchardware.ir.ITransmitter;
import org.harctoolbox.harchardware.ir.NoSuchTransmitterException;
import org.harctoolbox.harchardware.ir.Transmitter;
import org.harctoolbox.ircore.InvalidArgumentException;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.Pronto;
import org.harctoolbox.irp.DomainViolationException;
import org.harctoolbox.irp.InvalidNameException;
import org.harctoolbox.irp.IrpException;
import org.harctoolbox.irp.IrpInvalidArgumentException;
import org.harctoolbox.irp.NameEngine;
import org.harctoolbox.irp.NameUnassignedException;
import org.harctoolbox.irp.UnsupportedRepeatException;

/**
 * Transmitting commands.
 */
public class Transmit extends Module {

    private static volatile Transmit instance = null;

    static Module newTransmit(Renderer renderer, Irp irp) {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new Transmit(renderer, irp);
        return instance;
    }

    private static int intParse(String str) throws CommandSyntaxException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("Unparseable as integer: " + str);
        }
    }

    private static int[] parseRaw(String[] args, int length, int skip) throws CommandSyntaxException {
        int[] result = new int[length];
        for (int i = 0; i < length; i++)
            result[i] = intParse(args[i + skip]);

        return result;
    }

    private static boolean transmit(int count, IrSignal irSignal, IRawIrSender hardware, Transmitter transmitter) throws HarcHardwareException, NoSuchTransmitterException, IOException, InvalidArgumentException {
        return hardware.sendIr(irSignal, count, transmitter);
    }

    private static Transmitter getTransmitter(GirsHardware hardware) {
        if (!(hardware.getHardware() instanceof ITransmitter))
            return null;

        if (Transmitters.getInstance() == null)
            return ((ITransmitter) hardware.getHardware()).getTransmitter(); // the default transmitter

        return Transmitters.getInstance().getTransmitter(hardware);
    }

    // This command is not meaningful as long as the program is single threaded.
//    public static boolean stop() throws NoSuchHardwareException, NoSuchParameterException, IncompatibleHardwareException, HarcHardwareException, IOException, AmbigousHardwareException {
//        GirsHardware hardware = Engine.getInstance().getTransmitHardware();
//        initializeHardware(hardware, IIrSenderStop.class);
//        Transmitter transmitter = getTransmitter(hardware);
//        return ((IIrSenderStop) hardware.getHardware()).stopIr(transmitter);
//    }

    private final Renderer renderer;
    private final Irp irp;
    //private final NamedRemotes namedRemotes;

    private Transmit(Renderer renderer, Irp irp) {
        super();
        this.renderer = renderer;
        this.irp = irp;
        //this.namedRemotes = namedRemotes;

        addCommand(new TransmitCommand());
        addCommand(new SendCommand());
//        addCommand(new StopCommand());
    }


    private boolean transmit(int count, IrSignal irSignal) throws HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, InvalidArgumentException {
        GirsHardware hardware = Engine.getInstance().getTransmitHardware();
        initializeHardware(hardware, IRawIrSender.class);
        IRawIrSender irsender = (IRawIrSender) hardware.getHardware();
        Transmitter transmitter = getTransmitter(hardware);
        return transmit(count, irSignal, irsender, transmitter);
    }

    private boolean transmitCcf(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, Pronto.NonProntoFormatException, InvalidArgumentException {
        int count = intParse(args[0]);
        int[] data = Pronto.parseAsInts(args, 1);
        return transmitCcf(count, data);
    }

    public boolean transmitCcf(int count, int[] data) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, InvalidArgumentException {
        IrSignal irSignal = Pronto.parse(data);
        return transmit(count, irSignal);
    }

    public boolean transmitNamedCommand(int count, String remote, String command) throws HarcHardwareException, NoSuchRemoteException, NoSuchCommandException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, NoSuchModuleException, AmbigousRemoteException, AmbigousCommandException, AmbigousHardwareException, IrCoreException, IrpException {
        if (NamedRemotes.getInstance() == null)
            throw new NoSuchModuleException("NamedRemotes");
        IrSignal irSignal = NamedRemotes.getInstance().render(remote, command);
        return transmit(count, irSignal);
    }

    public boolean transmitRaw(int count, int frequency, int introLength, int repeatLength, int endingLength, int[] data) throws HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchParameterException, NoSuchHardwareException, AmbigousHardwareException, InvalidArgumentException {
        IrSignal irSignal = new IrSignal(data, introLength, repeatLength, frequency);
        return transmit(count, irSignal);
    }

    public boolean transmitIrp(int count, String irpCode, Map<String, Long> parameters) throws HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, NoSuchModuleException, AmbigousHardwareException, UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException, DomainViolationException, InvalidArgumentException {
        if (irp == null)
            throw new NoSuchModuleException("Irp");
        IrSignal irSignal = Irp.render(irpCode, parameters);
        return transmit(count, irSignal);
    }

    public boolean transmitProtocolParameters(int count, String[] args, int skip) throws NoSuchModuleException, HarcHardwareException, IOException, NoSuchTransmitterException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, InvalidArgumentException, IrpException {
        if (renderer == null)
            throw new NoSuchModuleException("Renderer");
        IrSignal irSignal = renderer.render(args, skip);
        return transmit(count, irSignal);
    }


//    private static class StopCommand implements ICommand {
//
//        private static final String STOP = "stop";
//
//        @Override
//        public String getName() {
//            return STOP;
//        }
//
//        @Override
//        public List<String> exec(String[] args) throws ExecutionException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, HarcHardwareException, NoSuchHardwareException, NoSuchParameterException, CommandSyntaxException, AmbigousHardwareException {
//            checkNoArgs(STOP, args.length, 3, Integer.MAX_VALUE);
//            return stop() ? new ArrayList<>(0) : null;
//        }
//    }

    private class TransmitCommand extends CommandWithSubcommands implements ICommand {

        TransmitCommand() {
            addCommand(new TransmitHexCommand());
            addCommand(new TransmitCcfCommand()); // synonym
            addCommand(new TransmitRawCommand());
            if (irp != null)
                addCommand(new TransmitIrpCommand());
            if (renderer != null)
                addCommand(new TransmitProtocolParameterCommand());
            if (NamedRemotes.getInstance() != null)
                addCommand(new TransmitNameCommand());
        }

        @Override
        public String getName() {
            return "transmit";
        }

        private class TransmitHexCommand implements ICommand {

            @Override
            public String getName() {
                return "hex";
            }

            @Override
            public List<String> exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, Pronto.NonProntoFormatException, InvalidArgumentException {
                return transmitCcf(args) ? new ArrayList<>(0) : null;
            }
        }

        private class TransmitCcfCommand implements ICommand {

            @Override
            public String getName() {
                return "ccf";
            }

            @Override
            public List<String> exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, Pronto.NonProntoFormatException, InvalidArgumentException {
                return transmitCcf(args) ? new ArrayList<>(0) : null;
            }
        }

        private class TransmitRawCommand implements ICommand {

            private static final String RAW = "raw";

            @Override
            public String getName() {
                return RAW;
            }

            @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
            @Override
            public List<String> exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, InvalidArgumentException {
                checkNoArgs(RAW, args.length, 3, Integer.MAX_VALUE);
                int index = 0;
                int count = intParse(args[index++]); // throw NumberFormatException
                int frequency = intParse(args[index++]);
                int introLength= intParse(args[index++]);
                int repeatLength= intParse(args[index++]);
                int endingLength= intParse(args[index++]);
                if (introLength % 2 != 0 || repeatLength % 2 != 0 || endingLength % 2 != 0)
                    throw new CommandSyntaxException("Lengths must be even");
                int[] data = parseRaw(args, introLength+repeatLength+endingLength, index);
                return transmitRaw(count, frequency, introLength, repeatLength, endingLength, data) ? new ArrayList<>(0) : null;
            }
        }

        private class TransmitIrpCommand implements ICommand {

            private static final String IRP = "irp";

            @Override
            public String getName() {
                return IRP;
            }

            @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
            @Override
            public List<String> exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, NoSuchModuleException, AmbigousHardwareException, UnsupportedRepeatException, NameUnassignedException, InvalidNameException, IrpInvalidArgumentException, DomainViolationException, InvalidArgumentException {
                checkNoArgs(IRP, args.length, 3, Integer.MAX_VALUE);
                int index = 0;
                int count = intParse(args[index++]);
                String irpCode = args[index++];
                String[] defs = new String[args.length - index];
                System.arraycopy(args, index, defs, 0, args.length - index);
                NameEngine parameters = NameEngine.parse(args);
                return transmitIrp(count, irpCode, parameters.toMap()) ? new ArrayList<>(0) : null;
            }
        }

        private class TransmitProtocolParameterCommand implements ICommand {

            private static final String PROTOCOLPARAMETER = "protocolparameter";

            @Override
            public String getName() {
                return PROTOCOLPARAMETER;
            }

            @Override
            public List<String> exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, NoSuchModuleException, AmbigousHardwareException, InvalidArgumentException, IrpException {
                checkNoArgs(PROTOCOLPARAMETER, args.length, 3, Integer.MAX_VALUE);
                int index = 0;
                int count = intParse(args[index]);
                index++;
                return transmitProtocolParameters(count, args, index) ? new ArrayList<>(0) : null;
            }
        }

        private class TransmitNameCommand implements ICommand {

            private static final String NAME = "name";

            @Override
            public String getName() {
                return NAME;
            }

            @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
            @Override
            public List<String> exec(String[] args) throws ExecutionException, NoSuchRemoteException, NoSuchCommandException, HarcHardwareException, NoSuchTransmitterException, IOException, CommandSyntaxException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, NoSuchModuleException, AmbigousRemoteException, AmbigousCommandException, AmbigousHardwareException, IrCoreException, IrpException {
                checkNoArgs(NAME, args.length, 3);
                int index = 0;
                int count = intParse(args[index++]);
                String remote = args[index++];
                String command = args[index++];
                return transmitNamedCommand(count, remote, command) ? new ArrayList<>(0) : null;
            }
        }
    }

    private class SendCommand implements ICommand {

        private static final String SEND = "send";

        @Override
        public String getName() {
            return SEND;
        }

        @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException, NoSuchHardwareException, NoSuchParameterException, AmbigousHardwareException, InvalidArgumentException {
            checkNoArgs(SEND, args.length, 8, Integer.MAX_VALUE);
            int index = 0;
            int count = intParse(args[index++]); // throws NumberFormatException
            int frequency = intParse(args[index++]);
            int introLength = intParse(args[index++]);
            int repeatLength = intParse(args[index++]);
            int endingLength = intParse(args[index++]);
            if (introLength % 2 != 0 || repeatLength % 2 != 0 || endingLength % 2 != 0)
                throw new CommandSyntaxException("Lengths must be even");
            int[] data = parseRaw(args, introLength + repeatLength + endingLength, index);
            return transmitRaw(count, frequency, introLength, repeatLength, endingLength, data) ? new ArrayList<>(0) : null;
        }
    }
}
