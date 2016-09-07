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
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.IrpMasterException;
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
    public static Transmit newTransmit(CommandExecuter commandExecuter, Parameters parameters, GirsHardware currentOutputHardware, Renderer renderer, Irp irp, NamedRemotes namedCommand) {
//        if (!(IRawIrSender.class.isInstance(hardware) || IRemoteCommandIrSender.class.isInstance(hardware)))
//            return null;
        return new Transmit(commandExecuter, parameters, currentOutputHardware, renderer, irp, namedCommand);
    }

    private final Renderer renderer;
    private final Irp irp;
    private final GirsHardware currentOutputHardware; // NOTE: its field hardware may change outside of this class
    //private Transmitter transmitter;
    private final NamedRemotes namedCommand;

    protected Transmit(CommandExecuter commandExecuter, Parameters parameters, GirsHardware currentOutputHardware, Renderer renderer, Irp irp, NamedRemotes namedCommand) {
        super(commandExecuter, parameters);
        this.renderer = renderer;
        this.irp = irp;
        this.currentOutputHardware = currentOutputHardware;
        this.namedCommand = namedCommand;
//        if (currentOutputHardware.getHardware() iIRawIrSender.class.isInstance(hardware))
//            transmitter = ((IRawIrSender) hardware).getTransmitter();
//        else if (IRemoteCommandIrSender.class.isInstance(hardware))
//            transmitter = ((IRemoteCommandIrSender) hardware).getTransmitter();
//        else
//            throw new RuntimeException();

        //addCommand(new TransmitCommand());
        addCommand(new SendCommand());
        //if (IIrSenderStop.class.isInstance(hardware))
        addCommand(new StopCommand());
        //if (ITransmitter.class.isInstance(hardware))
        addCommand(new GetTransmittersCommand());
    }

    private int intParse(String str) throws CommandSyntaxException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("Unparseable as integer: " + str);
        }
    }

//    private class TransmitCommand extends CommandWithSubcommands implements ICommand {
//
//        TransmitCommand() {
//            if (IRawIrSender.class.isInstance(hardware)) {
//                addCommand(new TransmitCcfCommand());
//                addCommand(new TransmitRawCommand());
//                if (irp != null)
//                    addCommand(new TransmitIrpCommand());
//                if (renderer != null)
//                    addCommand(new TransmitProtocolParameterCommand());
//            }
//            if (IIrSenderStop.class.isInstance(hardware))
//                addCommand(new TransmitNameCommand());
//        }
//
//        private ArrayString[] transmit(IrSignal irSignal, int count) throws HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException {
//            boolean result = ((IRawIrSender) hardware).sendIr(irSignal, count, transmitter);
//            return result ? new ArrayList<>(0) : null;
//        }
//
//        @Override
//        public String getName() {
//            return "transmit";
//        }
//
//        private class TransmitCcfCommand implements ICommand {
//
//            @Override
//            public String getName() {
//                return "ccf";
//            }
//
//            @Override
//            public String[] exec(String[] args) throws IrpMasterException, HarcHardwareException, IOException, JGirsException {
//                int count = intParse(args.get(0));
//                String[] array = args.toArray(new String[args.size()]);
//                IrSignal irSignal = new IrSignal(array, 1);
//                return transmit(irSignal, count);
//            }
//        }
//
//        private class TransmitRawCommand implements ICommand {
//
//            @Override
//            public String getName() {
//                return "raw";
//            }
//
//            @Override
//            public String[] exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException {
//                int index = 0;
//                int count = intParse(args[index++]); // throw NumberFormatException
//                int frequency = intParse(args[index++]);
//                int introLength= intParse(args[index++]);
//                int repeatLength= intParse(args[index++]);
//                int endingLength= intParse(args[index++]);
//                if (introLength % 2 != 0 || repeatLength % 2 != 0 || endingLength % 2 != 0)
//                    throw new CommandSyntaxException("Lengths must be even");
//                int[] data = parseRaw(args, 2*(introLength+repeatLength+endingLength), index);
//                IrSignal irSignal = new IrSignal(data, introLength/2, repeatLength/2, frequency);
//                return transmit(irSignal, count);
//            }
//
//            private int[] parseRaw(String[] args, int length, int skip) throws CommandSyntaxException {
//                int[] result = new int[length];
//                for (int i = skip; i < length+skip; i++)
//                    result[i] = intParse(args.get(i));
//
//                return result;
//            }
//        }
//
//        private class TransmitIrpCommand implements ICommand {
//
//            @Override
//            public String getName() {
//                return "irp";
//            }
//
//            @Override
//            public String[] exec(String[] args) throws UnassignedException, ParseException, IncompatibleArgumentException, DomainViolationException, InvalidRepeatException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException, CommandSyntaxException {
//                int count = intParse(args.get(0));
//                String protocolName = args.get(1);
//                HashMap<String, Long> parameters = new HashMap<>(args.size() - 2);
//                // TODO parse parameters
//                IrSignal irSignal = irp.render(protocolName, parameters);
//                return transmit(irSignal, count);
//            }
//        }
//
//        private class TransmitProtocolParameterCommand implements ICommand {
//
//            @Override
//            public String getName() {
//                return "protocolparameter";
//            }
//
//            @Override
//            public String[] exec(String[] args) throws ExecutionException, CommandSyntaxException, UnassignedException, ParseException, UnknownProtocolException, DomainViolationException, IncompatibleArgumentException, InvalidRepeatException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException {
//                int index = 1;
//                int count = intParse(args[index++]);
//                IrSignal irSignal = renderer.render(args.toArray(new String[args.size()]), index);
//                return transmit(irSignal, count);
//            }
//        }
//
//        private class TransmitNameCommand implements ICommand {
//
//            @Override
//            public String getName() {
//                return "name";
//            }
//
//            @Override
//            public String[] exec(String[] args) throws ExecutionException, IrpMasterException, NoSuchRemoteException, NoSuchCommandException, HarcHardwareException, NoSuchTransmitterException, IOException, CommandSyntaxException {
//                if (args.size() < 3)
//                    throw new CommandSyntaxException("Too few arguments");
//                int index = 1;
//                int count = intParse(args[index++]);
//                String remote = args[index++];
//                String command = args[index++];
//                IrSignal irSignal = namedCommand.render(remote, command);
//                return transmit(irSignal, count);
//            }
//        }
//    }

    private class SendCommand implements ICommand {

        @Override
        public String getName() {
            return "send";
        }

        @Override
        public String[] exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IncompatibleArgumentException, IOException {
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

        private int[] parseRaw(String[] args, int length, int skip) throws CommandSyntaxException {
                int[] result = new int[length];
                for (int i = 0; i < length; i++)
                    result[i] = intParse(args[i+skip]);

                return result;
            }

        private String[] transmit(IrSignal irSignal, int count) throws IncompatibleArgumentException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException  {
            IHarcHardware hardware = currentOutputHardware.getHardware();
            if (!(hardware instanceof IRawIrSender))
                throw new IncompatibleArgumentException("transmit");

            if (!hardware.isValid())
                hardware.open();

            Transmitter transmitter = ((IRawIrSender) hardware).getTransmitter();

            boolean result = ((IRawIrSender) hardware).sendIr(irSignal, count, transmitter);
            return result ? new String[0] : null;
        }
    }

    private class StopCommand implements ICommand {

        @Override
        public String getName() {
            return "stop";
        }

        @Override
        public String[] exec(String[] args) throws ExecutionException, NoSuchTransmitterException, IOException {
            IHarcHardware hardware = currentOutputHardware.getHardware();
            if (!(hardware instanceof IIrSenderStop))
                throw new ExecutionException("Current hardware does not  support stopping.");

            Transmitter transmitter = ((ITransmitter) hardware).getTransmitter();
            return ((IIrSenderStop) hardware).stopIr(transmitter) ? new String[0] : null;
        }
    }

    private class GetTransmittersCommand implements ICommand {

        @Override
        public String getName() {
            return "gettransmitters";
        }

        @Override
        public String[] exec(String[] args) throws NoSuchTransmitterException, ExecutionException, CommandSyntaxException {
            IHarcHardware hardware = currentOutputHardware.getHardware();
            if (!(hardware instanceof ITransmitter))
                throw new ExecutionException("Current hardware does not  support setting transmitters.");
            if (args.length > 1)
                throw new CommandSyntaxException("gettransmitters", 0);

            return ((ITransmitter) hardware).getTransmitterNames();
        }
    }
}
