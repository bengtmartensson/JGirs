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

package org.harctoolbox.jgirs.hardware;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.comm.LocalSerialPortBuffered;
import org.harctoolbox.harchardware.ir.GirsClient;


public class SerialGirsClient extends GirsClient<LocalSerialPortBuffered>{

    public SerialGirsClient(LocalSerialPortBuffered hardware) throws HarcHardwareException, IOException {
        super(hardware);
    }

    public SerialGirsClient(String portName, int baudRate) throws HarcHardwareException, IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
            this(new LocalSerialPortBuffered(portName, baudRate));
    }

    public SerialGirsClient(String portName) throws HarcHardwareException, IOException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
            this(new LocalSerialPortBuffered(portName));
    }
}
