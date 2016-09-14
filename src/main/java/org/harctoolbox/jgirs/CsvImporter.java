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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.girr.Command;
import org.harctoolbox.girr.Remote;
import org.harctoolbox.girr.RemoteSet;

public class CsvImporter {

    private static final int INVALID = -1;
    private static final String DEFAULT_SEPARATOR = ",";
    private static final String UNNAMED = "unnamed";
    private static final String NOPROTOCOL = "no_protocol";
    private static final String D = "D";
    private static final String S = "S";
    private static final String F = "F";

    private static void getParam(Map<String, Long> parameters, String parameterName, int column, String[] chunks) {
        if (column <= 0)
            return;

        long value = Long.parseLong(chunks[column-1]);
        if (value >= 0)
            parameters.put(parameterName, value);
    }

    private static String dummyName(Map<String, Long> parameters) {
        return D + parameters.get(D)
                + S + parameters.get(S)
                + F + parameters.get(F);
    }

    // First column is called 1, not 0.
    private int commandNameColumn;
    private int protocolColumn;
    private int dColumn;
    private int sColumn;
    private int fColumn;
    private String separator;

    public CsvImporter(int commandNameColumn, int protocolColumn,
            int dColumn, int sColumn, int fColumn, String separator) {
        this.commandNameColumn = commandNameColumn;
        this.protocolColumn = protocolColumn;
        this.dColumn = dColumn;
        this.sColumn = sColumn;
        this.fColumn = fColumn;
        this.separator = separator;
    }

    public CsvImporter() {
        this(1, 2, 3, 4, 5, DEFAULT_SEPARATOR);
    }

    private Command parseLine(String line) throws IrpMasterException {
        String[] chunks = line.split(separator);
        String protocol = protocolColumn > 0 ? chunks[protocolColumn-1] : NOPROTOCOL;
        Map<String, Long> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        getParam(parameters, D, dColumn, chunks);
        getParam(parameters, S, sColumn, chunks);
        getParam(parameters, F, fColumn, chunks);
        String commandName = commandNameColumn > 0 ? chunks[commandNameColumn-1] : dummyName(parameters);
        return new Command(commandName, null, protocol, parameters);
    }

    public Remote parseFile(String remoteName, Reader reader) throws IOException  {
        Map<String, Command> commands = new HashMap<>(16);
        try (BufferedReader in = new BufferedReader(reader)) {

            while (true) {
                String line = in.readLine();
                if (line == null)
                    break;

                try {
                    Command command = parseLine(line);
                    commands.put(command.getName(), command);
                } catch (NumberFormatException | IrpMasterException ex) {
                }
            }
        }
        return new Remote(new Remote.MetaData(remoteName), null, null, commands, null);
    }

    public RemoteSet parseRemoteSet(String remoteName, String source, Reader reader) throws IOException, IrpMasterException {
        Remote remote = parseFile(remoteName, reader);
        return new RemoteSet(null, source, remote);
    }
}
