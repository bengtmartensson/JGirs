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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.harctoolbox.IrpMaster.IrSignal;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.girr.Remote;
import org.harctoolbox.girr.RemoteSet;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of named remotes.
 */
public class NamedRemotes extends Module {

    private static boolean caseInsensitive = true;

    private static RemoteSet readRemoteSet(String file) throws ParserConfigurationException, SAXException, IOException, IrpMasterException, ParseException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setXIncludeAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(file));
        return new RemoteSet(doc);
    }

    private static List<RemoteSet> readRemoteSet(Iterable<String> files) throws ParserConfigurationException, SAXException, IOException, IrpMasterException, ParseException {
        ArrayList<RemoteSet> result = new ArrayList<>(16);
        for (String file : files)
            result.add(readRemoteSet(file));
        return result;
    }

    //private final RemoteSet remoteSet;
    private final RemoteCommandDataBase database;

    private NamedRemotes(Iterable<RemoteSet> remoteSets) throws IrpMasterException {
        this(new RemoteCommandDataBase(remoteSets, caseInsensitive));

    }

    private NamedRemotes(List<String> files) throws ParserConfigurationException, SAXException, IOException, IrpMasterException, ParseException {
        this(readRemoteSet(files));
    }

    public NamedRemotes(RemoteCommandDataBase remoteCommandsDataBase) {
        super();
        this.database = remoteCommandsDataBase;
        addCommand(new RemotesCommand());
        addCommand(new CommandsCommand());
    }

    public RemoteCommandDataBase.RemoteCommand getRemoteCommand(String protocol, Map<String, Long> parameters) {
        return database.getRemoteCommand(protocol, parameters);
    }

    public IrSignal render(String remoteName, String commandName) throws IrpMasterException, NoSuchRemoteException, NoSuchCommandException {
        Remote remote = database.getRemote(remoteName);
        if (remote == null)
            throw new NoSuchRemoteException(remoteName);
        org.harctoolbox.girr.Command command = remote.getCommand(commandName);
        if (command == null)
            throw new NoSuchCommandException(commandName);
        IrSignal irSignal = command.toIrSignal();
        return irSignal;
    }

    public boolean isEmpty() {
        return database.isEmpty();
    }

    private class RemotesCommand implements ICommand {

        @Override
        public String getName() {
            return "remotes";
        }

        @Override
        public String exec(String[] args) {
            Collection<Remote> collection = database.getRemotes();
            List<String> result = new ArrayList<>(collection.size());
            collection.stream().forEach((remote) -> {
                result.add(remote.getName());
            });

            return Utils.sortedString(result);
        }
    }

    private class CommandsCommand implements ICommand {

        @Override
        public String getName() {
            return "commands";
        }

        @Override
        public String exec(String[] args) throws CommandSyntaxException, NoSuchRemoteException, RemoteCommandDataBase.AmbigousRemoteException {
            if (args.length <= 1)
                throw new CommandSyntaxException("No remote given");
            String remoteNameFragment = args[1];
            Remote remote = database.findRemote(remoteNameFragment);
            if (remote == null)
                throw new NoSuchRemoteException(remoteNameFragment);
            return Utils.sortedString(remote.getCommands().keySet());
        }
    }
}
