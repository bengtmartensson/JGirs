/*
Copyright (C) 2014 Bengt Martensson.

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
import org.harctoolbox.IrpMaster.DomainViolationException;
import org.harctoolbox.IrpMaster.IncompatibleArgumentException;
import org.harctoolbox.IrpMaster.InvalidRepeatException;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.ParseException;
import org.harctoolbox.IrpMaster.UnassignedException;
import org.harctoolbox.IrpMaster.UnknownProtocolException;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.harctoolbox.harchardware.ir.IIrSenderStop;
import org.harctoolbox.harchardware.ir.IRawIrSender;
import org.harctoolbox.harchardware.ir.IRemoteCommandIrSender;
import org.harctoolbox.harchardware.ir.ITransmitter;
import org.harctoolbox.harchardware.ir.NoSuchTransmitterException;
import org.harctoolbox.harchardware.ir.Transmitter;

/**
 *
 */
public class Transmit extends Module {
    private Renderer renderer;
    private Irp irp;
    private IHarcHardware hardware;
    private Transmitter transmitter;
    private final NamedRemotes namedCommand;

    private int intParse(String str) throws CommandSyntaxException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("Unparseable as integer: " + str);
        }
    }

    private class TransmitCommand extends CommandWithSubcommands implements Command {

        private ArrayList<String> transmit(IrSignal irSignal, int count) throws HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException {
            boolean result = ((IRawIrSender) hardware).sendIr(irSignal, count, transmitter);
            return result ? new ArrayList<String>() : null;
        }

        TransmitCommand() {
            if (IRawIrSender.class.isInstance(hardware)) {
                addCommand(new TransmitCcfCommand());
                addCommand(new TransmitRawCommand());
                if (irp != null)
                    addCommand(new TransmitIrpCommand());
                if (renderer != null)
                    addCommand(new TransmitProtocolParameterCommand());
            }
            if (IIrSenderStop.class.isInstance(hardware))
                addCommand(new TransmitNameCommand());
        }

        @Override
        public String getName() {
            return "transmit";
        }

        private class TransmitCcfCommand implements Command {

            @Override
            public String getName() {
                return "ccf";
            }

            @Override
            public List<String> exec(String[] args) throws CommandSyntaxException, IrpMasterException, HarcHardwareException, NoSuchTransmitterException, IOException {
                int count = intParse(args[0]);
                IrSignal irSignal = new IrSignal(args, 1);
                return transmit(irSignal, count);
            }
        }

        private class TransmitRawCommand implements Command {

            @Override
            public String getName() {
                return "raw";
            }

            @Override
            public List<String> exec(String[] args) throws CommandSyntaxException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException {
                int index = 0;
                int count = intParse(args[index++]); // throw NumberFormatException
                int frequency = intParse(args[index++]);
                int introLength= intParse(args[index++]);
                int repeatLength= intParse(args[index++]);
                int endingLength= intParse(args[index++]);
                if (introLength % 2 != 0 || repeatLength % 2 != 0 || endingLength % 2 != 0)
                    throw new CommandSyntaxException("Lengths must be even");
                int[] data = parseRaw(args, 2*(introLength+repeatLength+endingLength), index);
                IrSignal irSignal = new IrSignal(data, introLength/2, repeatLength/2, frequency);
                return transmit(irSignal, count);
            }

            private int[] parseRaw(String[] args, int length, int skip) throws CommandSyntaxException {
                int[] result = new int[length];
                for (int i = skip; i < length+skip; i++)
                    result[i] = intParse(args[i]);

                return result;
            }
        }

        private class TransmitIrpCommand implements Command {

            @Override
            public String getName() {
                return "irp";
            }

            @Override
            public List<String> exec(String[] args) throws UnassignedException, ParseException, IncompatibleArgumentException, DomainViolationException, InvalidRepeatException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException, CommandSyntaxException {
                int count = intParse(args[0]);
                IrSignal irSignal = irp.render(args, count);
                return transmit(irSignal, count);
            }
        }

        private class TransmitProtocolParameterCommand implements Command {

            @Override
            public String getName() {
                return "protocolparameter";
            }

            @Override
            public List<String> exec(String[] args) throws ExecutionException, CommandSyntaxException, UnassignedException, ParseException, UnknownProtocolException, DomainViolationException, IncompatibleArgumentException, InvalidRepeatException, HarcHardwareException, NoSuchTransmitterException, IrpMasterException, IOException {
                int index = 1;
                int count = intParse(args[index++]);
                IrSignal irSignal = renderer.render(args, index);
                return transmit(irSignal, count);
            }
        }

        private class TransmitNameCommand implements Command {

            @Override
            public String getName() {
                return "name";
            }

            @Override
            public List<String> exec(String[] args) throws ExecutionException, IrpMasterException, NoSuchRemoteException, NoSuchCommandException, HarcHardwareException, NoSuchTransmitterException, IOException, CommandSyntaxException {
                if (args.length < 3)
                    throw new CommandSyntaxException("Too few arguments");
                int index = 1;
                int count = intParse(args[index++]);
                String remote = args[index++];
                String command = args[index++];
                IrSignal irSignal = namedCommand.render(remote, command);
                return transmit(irSignal, count);
            }
        }
    }

    private class StopCommand implements Command {

        @Override
        public String getName() {
            return "stop";
        }

        @Override
        public List<String> exec(String[] args) throws NoSuchTransmitterException, IOException {
            return ((IIrSenderStop) hardware).stopIr(transmitter) ? new ArrayList<String>() : null;
        }
    }

    private class SetTransmitterCommand implements Command {

        @Override
        public String getName() {
            return "settransmitter";
        }

        @Override
        public List<String> exec(String[] args) throws NoSuchTransmitterException {
            transmitter = ((ITransmitter) hardware).getTransmitter(args[1]);
            return new ArrayList<>();
        }
    }

    protected Transmit(IHarcHardware hardware, Renderer renderer, Irp irp, NamedRemotes namedCommand) {
        super();
        this.renderer = renderer;
        this.irp = irp;
        this.hardware = hardware;
        this.namedCommand = namedCommand;
        if (IRawIrSender.class.isInstance(hardware))
            transmitter = ((IRawIrSender) hardware).getTransmitter();
        else if (IRemoteCommandIrSender.class.isInstance(hardware))
            transmitter = ((IRemoteCommandIrSender) hardware).getTransmitter();
        else
            throw new RuntimeException();

        addCommand(new TransmitCommand());
        if (IIrSenderStop.class.isInstance(hardware))
            addCommand(new StopCommand());
        if (ITransmitter.class.isInstance(hardware))
            addCommand(new SetTransmitterCommand());
    }

    public static Transmit newTransmit(IHarcHardware hardware, Renderer renderer, Irp irp, NamedRemotes namedCommand) {
        if (!(IRawIrSender.class.isInstance(hardware) || IRemoteCommandIrSender.class.isInstance(hardware)))
            return null;
        return new Transmit(hardware, renderer, irp, namedCommand);
    }
}
