/*
Copyright (C) 2016  Bengt Martensson.

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
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.harctoolbox.girr.GirrException;
import org.harctoolbox.girr.Remote;
import org.harctoolbox.girr.RemoteSet;
import org.harctoolbox.ircore.IrCoreException;
import org.harctoolbox.irp.IrpException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class implements a data base for Remotes and Commands. It is not claimed to be very efficient,
 * and may be replaced by a more efficient implementation, should the need arise.
 *
 * TODO: take default parameters into account: remove them if they have the default values.
 */
public class RemoteCommandDataBase {

    private static RemoteSet parseGirr(URL url) throws java.text.ParseException, IOException, SAXException, GirrException {
        Document doc = Utils.openXmlUrl(url, null, true, true);
        RemoteSet remoteSet = new RemoteSet(doc);
        return remoteSet;
    }

    private TreeMap<String, Remote> remotes;
    private TreeMap<ProtocolParameter, RemoteCommand> data;

    public RemoteCommandDataBase() {
        this(true);
    }

    public RemoteCommandDataBase(boolean caseInsensitive) {
        this.data = new TreeMap<>();
        this.remotes = caseInsensitive ? new TreeMap<>(String.CASE_INSENSITIVE_ORDER) : new TreeMap<>();
    }

    public RemoteCommandDataBase(Iterable<RemoteSet> remoteSets, boolean caseInsensitive) throws IrpException, IrCoreException {
        this(caseInsensitive);
        for (RemoteSet remoteSet : remoteSets)
            add(remoteSet);
    }

    public RemoteCommandDataBase(Iterable<RemoteSet> remoteSets) throws IrpException, IrCoreException {
        this(true);
        for (RemoteSet remoteSet : remoteSets)
            add(remoteSet);
    }

    private void add(RemoteSet remoteSet) throws IrpException, IrCoreException {
        for (Remote remote : remoteSet.getRemotes()) {
            add(remote);
        }
    }

    private void add(Remote remote) throws IrpException, IrCoreException {
        remotes.put(remote.getName(), remote);
        for (org.harctoolbox.girr.Command command : remote.getCommands().values()) {
            RemoteCommand remoteCommand = new RemoteCommand(remote, command);
            String protocol = command.getProtocolName();
            Map<String, Long> parameters = command.getParameters();
            ProtocolParameter params = new ProtocolParameter(protocol, parameters);
            data.put(params, remoteCommand);
        }
    }

    public void add(Iterable<String> urls) throws ParseException, IOException, SAXException, IrpException, IrCoreException, GirrException {
        for (String url : urls) {
            RemoteSet remoteSet = parseGirr(new URL(url));
            add(remoteSet);
        }
    }

    public Remote getRemote(String remoteNamePrefix) throws NoSuchRemoteException, AmbigousRemoteException {
        List<String> list = Utils.findStringsWithPrefix(remotes.keySet(), remoteNamePrefix);
        if (list.isEmpty())
            throw new NoSuchRemoteException(remoteNamePrefix);
        if (list.size() > 1)
            throw new AmbigousRemoteException(remoteNamePrefix);

        return remotes.get(list.get(0));
    }

    public Collection<Remote> getRemotes() {
        return remotes.values();
    }

    public org.harctoolbox.girr.Command getCommand(String remoteName, String commandName) {
        Remote remote = remotes.get(remoteName);
        return remote.getCommand(commandName);
    }

//    public RemoteCommand getRemoteCommand(String protocol, Map<String, Long>parameters) {
//        ProtocolParameter params = new ProtocolParameter(protocol, parameters);
//        return data.get(params);
//    }

    public RemoteCommand getRemoteCommand(ProtocolParameter params) {
        return data.get(params);
    }

    public boolean isEmpty() {
        return remotes.isEmpty();
    }

    public static class RemoteCommand {

        private final Remote remote;
        private final org.harctoolbox.girr.Command command;

        public RemoteCommand(Remote remote, org.harctoolbox.girr.Command command) {
            this.remote = remote;
            this.command = command;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final RemoteCommand other = (RemoteCommand) obj;
            return this.command.getName().equals(other.command.getName())
                    && this.remote.getName().equals(other.remote.getName());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 19 * hash + this.remote.getName().hashCode();
            hash = 19 * hash + this.command.getName().hashCode();
            return hash;
        }

        /**
         * @return the remote
         */
        public Remote getRemote() {
            return remote;
        }

        /**
         * @return the command
         */
        public org.harctoolbox.girr.Command getCommand() {
            return command;
        }

        @Override
        public String toString() {
            return remote.getName() + "/" + command.getName();
        }
    }
}
