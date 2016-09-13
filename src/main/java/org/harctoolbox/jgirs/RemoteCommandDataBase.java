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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.girr.Remote;
import org.harctoolbox.girr.RemoteSet;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class implements a data base for Remotes and Commands. It is not claimed to be very efficient,
 * and may be replaced by a more efficient implementation, should the need arise.
 *
 * TODO: take left-out default parameters into account.
 */
public class RemoteCommandDataBase {

    public static void main(String[] args) {
        TreeMap<ProtocolParameter, String> db = new TreeMap<>();
        Map<String, Long> params = new HashMap<>(16);
        params.put("C", 3L);
        params.put("A", 1L);
        ProtocolParameter ba21 = new ProtocolParameter("proto", params);
        db.put(ba21, "ba21");
        ba21 = new ProtocolParameter("proto", params);
        db.put(ba21, "ba21xxx");
        params.put("B", 2L);
        ba21 = new ProtocolParameter("aproto", params);
        db.put(ba21, "ba21xx");
        Map<String, Long> ps = new HashMap<>(16);
        ps.put("A", 1L);
        ps.put("B", 2L);
        ps.put("C", 3L);
        ProtocolParameter pa = new ProtocolParameter("proto", ps);
        db.put(pa, "xxx");
        ProtocolParameter pb = null;
        try {
            pb = (ProtocolParameter) pa.clone();
        } catch (CloneNotSupportedException ex) {
        }
        db.put(pb, "godzilla");

        db.entrySet().stream().forEach((java.util.Map.Entry<org.harctoolbox.jgirs.ProtocolParameter, java.lang.String> kvp) -> {
            System.out.println(kvp.getKey() + "\t" + kvp.getValue());
        });
    }

//    private static RemoteSet parseGirr(Element element) throws IOException, SAXException, ParseException {
//        return parseGirr(new URL(element.getAttribute("url")));
//    }

    private static RemoteSet parseGirr(URL url) throws java.text.ParseException, IOException, SAXException {
        Document doc = Utils.openXmlUrl(url, null, true, true);
        RemoteSet remoteSet = new RemoteSet(doc);
        return remoteSet;
    }

    private Map<String, Remote> remotes;
    private TreeMap<ProtocolParameter, RemoteCommand> data;

    public RemoteCommandDataBase(boolean caseInsensitive) {
        this.data = new TreeMap<>();
        this.remotes = caseInsensitive ? new TreeMap<>(String.CASE_INSENSITIVE_ORDER) : new TreeMap<>();
    }

    public RemoteCommandDataBase(Iterable<RemoteSet> remoteSets, boolean caseInsensitive) throws IrpMasterException {
        this(caseInsensitive);
        for (RemoteSet remoteSet : remoteSets)
            load(remoteSet);
    }

    private void load(RemoteSet remoteSet) throws IrpMasterException {
        for (Remote remote : remoteSet.getRemotes()) {
            remotes.put(remote.getName(), remote);
            for (org.harctoolbox.girr.Command command : remote.getCommands().values()) {
                RemoteCommand rc = new RemoteCommand(remote, command);
                String protocol = command.getProtocolName();
                Map<String, Long> parameters = command.getParameters();
                ProtocolParameter params = new ProtocolParameter(protocol, parameters);
                this.data.put(params, rc);
            }
        }
    }

    public void add(Iterable<String> urls) throws ParseException, IOException, SAXException, IrpMasterException {
        for (String url : urls) {
            RemoteSet remoteSet = parseGirr(new URL(url));
            load(remoteSet);
        }
    }

    public Remote getRemote(String remoteName) throws NoSuchRemoteException, AmbigousRemoteException {
        List<String> list = Utils.findStringsWithPrefix(remotes.keySet(), remoteName);
        if (list.isEmpty())
            throw new NoSuchRemoteException(remoteName);
        if (list.size() > 1)
            throw new AmbigousRemoteException(remoteName);

        return remotes.get(list.get(0));
    }

    public Remote findRemote(String remoteNameFragment) throws AmbigousRemoteException {
        if (remotes.containsKey(remoteNameFragment))
            return remotes.get(remoteNameFragment);

        Remote candidate = null;
        for (Remote remote : remotes.values())
            if (remote.getName().toLowerCase(Locale.US).startsWith(remoteNameFragment.toLowerCase(Locale.US))) {
                if (candidate != null)
                    throw new AmbigousRemoteException(remoteNameFragment);
                candidate = remote;
            }

        return candidate;
    }

    public Collection<Remote> getRemotes() {
        return remotes.values();
    }

    public org.harctoolbox.girr.Command getCommand(String remoteName, String commandName) {
        Remote remote = remotes.get(remoteName);
        return remote.getCommand(commandName);
    }

    public RemoteCommand getRemoteCommand(String protocol, Map<String, Long>parameters) {
        ProtocolParameter params = new ProtocolParameter(protocol, parameters);
        return this.data.get(params);
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
