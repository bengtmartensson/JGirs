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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.XmlUtils;
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
        TreeMap<ParameterSet, String> db = new TreeMap<>();
        Map<String, Long> params = new HashMap<>(16);
        params.put("C", 3L);
        params.put("A", 1L);
        ParameterSet ba21 = new ParameterSet("proto", params);
        db.put(ba21, "ba21");
        ba21 = new ParameterSet("proto", params);
        db.put(ba21, "ba21xxx");
        params.put("B", 2L);
        ba21 = new ParameterSet("aproto", params);
        db.put(ba21, "ba21xx");
        Map<String, Long> ps = new HashMap<>(16);
        ps.put("A", 1L);
        ps.put("B", 2L);
        ps.put("C", 3L);
        ParameterSet pa = new ParameterSet("proto", ps);
        db.put(pa, "xxx");
        ParameterSet pb = null;
        try {
            pb = (ParameterSet) pa.clone();
        } catch (CloneNotSupportedException ex) {
        }
        db.put(pb, "godzilla");

        db.entrySet().stream().forEach((kvp) -> {
            System.out.println(kvp.getKey() + "\t" + kvp.getValue());
        });
    }

//    private static RemoteSet parseGirr(Element element) throws IOException, SAXException, ParseException {
//        return parseGirr(new URL(element.getAttribute("url")));
//    }

    private static RemoteSet parseGirr(URL url) throws java.text.ParseException, IOException, SAXException {
        Document doc = XmlUtils.openXmlUrl(url, null, true, true);
        RemoteSet remoteSet = new RemoteSet(doc);
        return remoteSet;
    }

    private Map<String, Remote> remotes;
    private TreeMap<ParameterSet, RemoteCommand> data;

    public RemoteCommandDataBase(boolean caseInsensitive) {
        this.data = new TreeMap<>();
        this.remotes = caseInsensitive ? new TreeMap<>(String.CASE_INSENSITIVE_ORDER) : new TreeMap<>();
    }

    public RemoteCommandDataBase(Iterable<RemoteSet> remoteSets, boolean caseInsensitive) throws IrpMasterException {
        this(caseInsensitive);
        for (RemoteSet remoteSet : remoteSets)
            load (remoteSet);
    }

    private void load(RemoteSet remoteSet) throws IrpMasterException {
        for (Remote remote : remoteSet.getRemotes()) {
            remotes.put(remote.getName(), remote);
            for (org.harctoolbox.girr.Command command : remote.getCommands().values()) {
                RemoteCommand rc = new RemoteCommand(remote, command);
                String protocol = command.getProtocolName();
                Map<String, Long> parameters = command.getParameters();
                ParameterSet params = new ParameterSet(protocol, parameters);
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

    public Remote getRemote(String remoteName) {
        return remotes.get(remoteName);
    }

    public Collection<Remote> getRemotes() {
        return remotes.values();
    }

    public org.harctoolbox.girr.Command getCommand(String remoteName, String commandName) {
        Remote remote = remotes.get(remoteName);
        return remote.getCommand(commandName);
    }

    public RemoteCommand getRemoteCommand(String protocol, Map<String, Long>parameters) {
        ParameterSet params = new ParameterSet(protocol, parameters);
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

    public static class ParameterSet implements Comparable<ParameterSet>, Cloneable {
        private final Map<String, Long> parameters;
        private final String protocol;

        @SuppressWarnings("unchecked")
        public ParameterSet(String protocol, Map<String, Long> parameters) {
            this.protocol = protocol.toLowerCase(Locale.US);
            this.parameters = new HashMap<>(parameters.size());
            parameters.entrySet().stream().forEach((kvp) -> {
                this.parameters.put(kvp.getKey(), kvp.getValue());
            });
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final ParameterSet other = (ParameterSet) obj;
            if (!this.protocol.equals(other.protocol))
                return false;
            if (this.parameters.size() != other.parameters.size())
                return false;
            for (Map.Entry<String, Long> kvp : this.parameters.entrySet()) {
                String name = kvp.getKey();
                if (!Objects.equals(kvp.getValue(), other.parameters.get(name)))
                    return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.parameters);
            hash = 97 * hash + Objects.hashCode(this.protocol);
            return hash;
        }

        private ArrayList<String> parameterNamesSorted() {
            ArrayList<String> list = new ArrayList<>(parameters.keySet());
            Collections.sort(list);
            return list;
        }

        @Override
        public String toString() {
            ArrayList<String> list = parameterNamesSorted();
            StringBuilder str = new StringBuilder(protocol);
            list.stream().forEach((param) -> {
                str.append(" ").append(param).append("=").append(parameters.get(param));
            });
            return str.toString();
        }

        /*@Override
        public int compare(ParameterSet p1, ParameterSet p2) {
        int c1 = p1.protocol.compareToIgnoreCase(p2.protocol);
        if (c1 != 0)
        return c1;
        ArrayList<String> params1 = p1.parameterNamesSorted();
        ArrayList<String> params2 = p2.parameterNamesSorted();
        for (int i = 0; i < params1.size(); i++) {
        String param1 = params1.get(i);
        String param2 = params1.get(i);
        int c2 = param1.compareTo(param2);
        if (c2 != 0)
        return c2;
        int c3 = Long.compare(p1.parameters.get(param1), p2.parameters.get(param2));
        if (c3 != 0)
        return c3;
        }
        return params1.size() == params2.size() ? 0 : 1;
        }*/

        @Override
        public int compareTo(ParameterSet p2) {
            int c1 = this.protocol.compareToIgnoreCase(p2.protocol);
            if (c1 != 0)
                return c1;
            ArrayList<String> params1 = this.parameterNamesSorted();
            ArrayList<String> params2 = p2.parameterNamesSorted();
            for (int i = 0; i < params1.size(); i++) {
                String param1 = params1.get(i);
                String param2 = params2.get(i);
                int c2 = param1.compareTo(param2);
                if (c2 != 0)
                    return c2;
                int c3 = Long.compare(this.parameters.get(param1), p2.parameters.get(param2));
                if (c3 != 0)
                    return c3;
            }
            return params1.size() == params2.size() ? 0 : 1;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            super.clone();
            ParameterSet dolly = new ParameterSet(protocol, parameters);
            return dolly;
        }
    }
}
