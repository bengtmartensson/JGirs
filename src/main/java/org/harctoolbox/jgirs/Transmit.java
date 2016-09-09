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
import java.util.HashMap;
import org.harctoolbox.IrpMaster.DomainViolationException;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.InvalidRepeatException;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.ParseException;
import org.harctoolbox.IrpMaster.Pronto;
import org.harctoolbox.IrpMaster.Protocol;
import org.harctoolbox.IrpMaster.UnassignedException;
import org.harctoolbox.IrpMaster.UnknownProtocolException;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.harctoolbox.harchardware.ir.IIrSenderStop;
import org.harctoolbox.harchardware.ir.IRawIrSender;
import org.harctoolbox.harchardware.ir.ITransmitter;
import org.harctoolbox.harchardware.ir.NoSuchTransmitterException;
import org.harctoolbox.harchardware.ir.Transmitter;

/**
 * Transmitting commands.
 */
public class Transmit extends Module {

    private static volatile Transmit instance = null;

    static Module newTransmit(Renderer renderer, Irp irp, NamedRemotes namedRemotes) {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new Transmit(renderer, irp, namedRemotes);
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

    private static String transmit(IrSignal irSignal, int count, IRawIrSender hardware, Transmitter transmitter) throws HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException {
        boolean result = hardware.sendIr(irSignal, count, transmitter);
        return result ? "" : null;
    }

    private static String transmit(IrSignal irSignal, int count) throws HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException, IncompatibleHardwareException {
        IHarcHardware hardware = GirsHardware.getDefaultTransmittingHardware().getHardware();
        if (!(hardware instanceof IRawIrSender))
            throw new IncompatibleHardwareException("IRawIrSender");

        if (!hardware.isValid())
            hardware.open();

        IRawIrSender irsender = (IRawIrSender) hardware;
        Transmitter transmitter = irsender.getTransmitter();

        return transmit(irSignal, count, irsender, transmitter);
    }

    private final Renderer renderer;
    private final Irp irp;
    private final NamedRemotes namedRemotes;

    private Transmit(Renderer renderer, Irp irp, NamedRemotes namedRemotes) {
        super();
        this.renderer = renderer;
        this.irp = irp;
//        this.currentOutputHardware = currentOutputHardware;
        this.namedRemotes = namedRemotes;
//        if (currentOutputHardware.getHardware() iIRawIrSender.class.isInstance(hardware))
//            transmitter = ((IRawIrSender) hardware).getTransmitter();
//        else if (IRemoteCommandIrSender.class.isInstance(hardware))
//            transmitter = ((IRemoteCommandIrSender) hardware).getTransmitter();
//        else
//            throw new RuntimeException();

addCommand(new TransmitCommand());
        addCommand(new SendCommand());
//if (IIrSenderStop.class.isInstance(hardware))
        addCommand(new StopCommand());
//if (ITransmitter.class.isInstance(hardware))
        addCommand(new GetTransmittersCommand());
    }

    private static class StopCommand implements ICommand {

        @Override
        public String getName() {
            return "stop";
        }

        @Override
        public String exec(String[] args) throws ExecutionException, NoSuchTransmitterException, IOException, IncompatibleHardwareException {
            IHarcHardware hardware = GirsHardware.getDefaultTransmittingHardware().getHardware();
            if (!(hardware instanceof IIrSenderStop))
                throw new IncompatibleHardwareException("IIrSenderStop");
                        //ExecutionException("Current hardware does not  support stopping.");

            Transmitter transmitter = ((ITransmitter) hardware).getTransmitter();
            return ((IIrSenderStop) hardware).stopIr(transmitter) ? "" : null;
        }
    }

    private static class GetTransmittersCommand implements ICommand {

        @Override
        public String getName() {
            return "gettransmitters";
        }

        @Override
        public String exec(String[] args) throws NoSuchTransmitterException, ExecutionException, CommandSyntaxException {
            IHarcHardware hardware = GirsHardware.getDefaultTransmittingHardware().getHardware();
            if (!(hardware instanceof ITransmitter))
                throw new ExecutionException("Current hardware does not  support setting transmitters.");
            if (args.length > 1)
                throw new CommandSyntaxException("gettransmitters", 0);

            return Utils.sortedString(((ITransmitter) hardware).getTransmitterNames());
        }
    }

    private class TransmitCommand extends CommandWithSubcommands implements ICommand {

        TransmitCommand() {
            addCommand(new TransmitHexCommand());
            addCommand(new TransmitRawCommand());
            if (irp != null)
                addCommand(new TransmitIrpCommand());
            if (renderer != null)
                addCommand(new TransmitProtocolParameterCommand());
            if (namedRemotes != null)
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
            public String exec(String[] args) throws IrpMasterException, CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IOException, IncompatibleHardwareException {
                int count = intParse(args[1]);
                IrSignal irSignal = Pronto.ccfSignal(args, 2);
                return transmit(irSignal, count);
            }
        }

        private class TransmitRawCommand implements ICommand {

            @Override
            public String getName() {
                return "raw";
            }

            @Override
            public String exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException, IncompatibleHardwareException {
                int index = 1;
                int count = intParse(args[index++]); // throw NumberFormatException
                int frequency = intParse(args[index++]);
                int introLength= intParse(args[index++]);
                int repeatLength= intParse(args[index++]);
                int endingLength= intParse(args[index++]);
                if (introLength % 2 != 0 || repeatLength % 2 != 0 || endingLength % 2 != 0)
                    throw new CommandSyntaxException("Lengths must be even");
                int[] data = parseRaw(args, introLength+repeatLength+endingLength, index);
                IrSignal irSignal = new IrSignal(data, introLength/2, repeatLength/2, frequency);
                return transmit(irSignal, count);
            }
        }

        private class TransmitIrpCommand implements ICommand {

            @Override
            public String getName() {
                return "irp";
            }

            @Override
            public String exec(String[] args) throws CommandSyntaxException, UnassignedException, ParseException, IncompatibleArgumentException, DomainViolationException, InvalidRepeatException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException, IncompatibleHardwareException {
                int count = intParse(args[1]);
                String irpCode = args[2];
                HashMap<String, Long> parameters = Protocol.parseParams(args, 3);
                IrSignal irSignal = irp.render(irpCode, parameters);
                return transmit(irSignal, count);
            }
        }

        private class TransmitProtocolParameterCommand implements ICommand {

            @Override
            public String getName() {
                return "protocolparameter";
            }

            @Override
            public String exec(String[] args) throws CommandSyntaxException, UnassignedException, ParseException, IncompatibleArgumentException, UnknownProtocolException, DomainViolationException, InvalidRepeatException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException, IncompatibleHardwareException {
                int index = 1;
                int count = intParse(args[index++]);
                IrSignal irSignal = renderer.render(args, index);
                return transmit(irSignal, count);
            }
        }

        private class TransmitNameCommand implements ICommand {

            @Override
            public String getName() {
                return "name";
            }

            @Override
            public String exec(String[] args) throws ExecutionException, IrpMasterException, NoSuchRemoteException, NoSuchCommandException, HarcHardwareException, NoSuchTransmitterException, IOException, CommandSyntaxException, IncompatibleHardwareException {
                if (args.length != 3)
                    throw new CommandSyntaxException("name", 3);
                int index = 1;
                int count = intParse(args[index++]);
                String remote = args[index++];
                String command = args[index++];
                IrSignal irSignal = namedRemotes.render(remote, command);
                return transmit(irSignal, count);
            }
        }
    }

    private class SendCommand implements ICommand {

        @Override
        public String getName() {
            return "send";
        }

        @Override
        public String exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IncompatibleArgumentException, IOException, IncompatibleHardwareException {
            int index = 1;
            int count = intParse(args[index++]); // throw NumberFormatException
            int frequency = intParse(args[index++]);
            int introLength = intParse(args[index++]);
            int repeatLength = intParse(args[index++]);
            int endingLength = intParse(args[index++]);
            if (introLength % 2 != 0 || repeatLength % 2 != 0 || endingLength % 2 != 0)
                throw new CommandSyntaxException("Lengths must be even");
            int[] data = parseRaw(args, introLength + repeatLength + endingLength, index);
            IrSignal irSignal = new IrSignal(data, introLength / 2, repeatLength / 2, frequency);
            return transmit(irSignal, count);
        }

//        private String transmit(IrSignal irSignal, int count) throws IncompatibleArgumentException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException  {
//            IHarcHardware hardware = GirsHardware.getDefaultTransmittingHardware().getHardware();
//            if (!(hardware instanceof IRawIrSender))
//                throw new IncompatibleArgumentException("transmit");
//
//            if (!hardware.isValid())
//                hardware.open();
//
//            Transmitter transmitter = ((IRawIrSender) hardware).getTransmitter();
//
//            boolean result = ((IRawIrSender) hardware).sendIr(irSignal, count, transmitter);
//            return result ? "" : null;
//        }
    }
}
