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

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


public class ProtocolParameter implements Comparable<ProtocolParameter>, Cloneable {

    private final TreeMap<String, Long> parameters; // Case preserving
    private final String protocol; // folded to lowercase

    public ProtocolParameter(String protocol, Map<String, Long> parameters) {
        // TODO: check for defaults: eliminate all parameters equals to their defaults.
        this.protocol = protocol.toLowerCase(Locale.US);
        this.parameters = new TreeMap<>(parameters);
    }

    public ProtocolParameter(String protocol, long F) {
        this(protocol, Utils.toParameters(F));
    }

    public ProtocolParameter(String protocol, long D, long F) {
        this(protocol, Utils.toParameters(D, F));
    }

    public ProtocolParameter(String protocol, long D, long S, long F) {
        this(protocol, Utils.toParameters(D, S, F));
    }

    public ProtocolParameter(String protocol, long D, long S, long F, long T) {
        this(protocol, Utils.toParameters(D, S, F, T));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ProtocolParameter other = (ProtocolParameter) obj;
        return protocol.equals(other.protocol) && parameters.equals(other.parameters);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.parameters);
        hash = 97 * hash + Objects.hashCode(this.protocol);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(protocol);
        parameters.keySet().stream().forEach((param) -> {
            str.append(" ").append(param).append("=").append(parameters.get(param));
        });
        return str.toString();
    }

    @Override
    public int compareTo(ProtocolParameter other) {
        int c1 = protocol.compareTo(other.protocol);
        if (c1 != 0)
            return c1;

        for (String parameterName : parameters.keySet()) {
            if (!other.parameters.containsKey(parameterName))
                return -1;
        }

        if (other.parameters.size() > parameters.size())
            return -1;

        for (Map.Entry<String, Long> entry : parameters.entrySet()) {
            String parameterName = entry.getKey();
            Long parameterValue = entry.getValue();
            Long otherValue = other.parameters.get(parameterName);
            int c2 = Long.compare(parameterValue, otherValue);
            if (c2 != 0)
                return c2;
        }

        return 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ProtocolParameter dolly = (ProtocolParameter) super.clone();
        //ProtocolParameter dolly = new ProtocolParameter(protocol, parameters);
        return dolly;
    }

    /**
     * @param parameterName
     * @return the parameters
     */
    public Long getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }
}
