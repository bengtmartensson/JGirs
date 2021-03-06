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
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.harctoolbox.girr.GirrException;
import org.harctoolbox.girr.Remote;
import org.harctoolbox.girr.RemoteSet;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.irp.IrpException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of named remotes.
 */
public class NamedRemotes extends Module {

    private static boolean caseInsensitive = true;

    private static volatile NamedRemotes instance = null;

    private static RemoteSet readRemoteSet(String file) throws ParserConfigurationException, SAXException, IOException, ParseException, GirrException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setXIncludeAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(file));
        return new RemoteSet(doc);
    }

    private static List<RemoteSet> readRemoteSet(Iterable<String> files) throws ParserConfigurationException, SAXException, IOException, ParseException, GirrException {
        ArrayList<RemoteSet> result = new ArrayList<>(16);
        for (String file : files)
            result.add(readRemoteSet(file));
        return result;
    }

    public static NamedRemotes getInstance() {
        return instance;
    }

    public static NamedRemotes newNamedRemotes(RemoteCommandDataBase remoteCommandsDataBase) {
        if (instance != null)
            throw new InvalidMultipleInstantiation("NamedRemotes");
        instance = new NamedRemotes(remoteCommandsDataBase);
        return instance;
    }

    private final RemoteCommandDataBase database;

    private NamedRemotes(Iterable<RemoteSet> remoteSets) throws IrCoreException, IrpException {
        this(new RemoteCommandDataBase(remoteSets, caseInsensitive));

    }

    private NamedRemotes(List<String> files) throws ParserConfigurationException, SAXException, IOException, ParseException, IrCoreException, IrpException, GirrException {
        this(readRemoteSet(files));
    }

    private NamedRemotes(RemoteCommandDataBase remoteCommandsDataBase) {
        super();
        this.database = remoteCommandsDataBase;
        addCommand(new RemotesCommand());
        //addCommand(new CommandsCommand());
    }

    public RemoteCommandDataBase.RemoteCommand getRemoteCommand(ProtocolParameter protocolParameters) {
        return database.getRemoteCommand(protocolParameters);
    }

    public IrSignal render(String remotePrefix, String commandPrefix) throws NoSuchRemoteException, NoSuchCommandException, AmbigousRemoteException, AmbigousCommandException, IrCoreException, IrpException {
        Remote remote = database.getRemote(remotePrefix);
        List<String> commandCandidates = Utils.findStringsWithPrefix(remote.getCommands().keySet(), commandPrefix);
        if (commandCandidates.isEmpty())
            throw new NoSuchCommandException(commandPrefix);
        if (commandCandidates.size() > 1)
            throw new AmbigousCommandException(commandPrefix);

        org.harctoolbox.girr.Command command = remote.getCommand(commandCandidates.get(0));
        IrSignal irSignal = command.toIrSignal();
        return irSignal;
    }

    public boolean isEmpty() {
        return database.isEmpty();
    }

    public List<String> remotes(String remoteNameFragment) throws AmbigousRemoteException, NoSuchRemoteException {
        Remote remote = database.getRemote(remoteNameFragment);
        if (remote == null)
            throw new NoSuchRemoteException(remoteNameFragment);
        return Utils.toSortedList(remote.getCommands().keySet());
    }

    public List<String> remotes() {
        Collection<Remote> collection = database.getRemotes();
        List<String> result = new ArrayList<>(collection.size());
        collection.stream().forEach((remote) -> {
            result.add(remote.getName());
        });

        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    private class RemotesCommand implements ICommand {

        private static final String REMOTES = "remotes";

        @Override
        public String getName() {
            return REMOTES;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException, AmbigousRemoteException, NoSuchRemoteException {
            checkNoArgs(REMOTES, args.length, 0, 1);
            return args.length == 0 ? remotes() : remotes(args[0]);
        }
    }
}
