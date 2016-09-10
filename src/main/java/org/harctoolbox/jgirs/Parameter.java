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

import java.io.Serializable;
import java.util.Comparator;

public abstract class Parameter /*implements IParameter*/ {

    protected String name;
    protected String documentation;

    protected Parameter(String name, String documentation) {
        this.name = name;
        this.documentation = documentation;
    }

    public String getName() {
        return name;
    }

    public String getDocumentation() {
        return documentation;
    }

    @Override
    public String toString() {
        return name + "=" + get();
    }

    public abstract void set(String value);

    public abstract void set(Object value);

    public abstract String get();

    public static class ParameterNameComparator implements Comparator<Parameter>, Serializable {

        @Override
        public int compare(Parameter p1, Parameter p2) {
            return p1.name.compareToIgnoreCase(p2.name);
        }

    }

    public static class IncorrectParameterType extends RuntimeException {
        public IncorrectParameterType() {
            super();
        }

        public IncorrectParameterType(String expected) {
            super("Incorrect type, " + expected + " expected");
        }
    }
}
