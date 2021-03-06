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

/**
 * Class for boolean parameters.
 */
final class BooleanParameter extends Parameter {

    private boolean value;

    //private BooleanParameter() {}

    BooleanParameter(String name, boolean initValue, String documentation) {
        super(name, documentation);
        this.value = initValue;
    }

    @Override
    public void set(String str) {
        value = Boolean.parseBoolean(str);
    }

    public void set(boolean val) {
        value = val;
    }

    @Override
    public void set(Object val) {
        value = (boolean) val;
    }

    @Override
    public String get() {
        return Boolean.toString(value);
    }

    /**
     * @return the value
     */
    public boolean getValue() {
        return value;
    }
}
