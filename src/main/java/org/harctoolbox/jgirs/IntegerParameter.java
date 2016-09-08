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
 *
 */
final class IntegerParameter extends Parameter {

    private int value;
    //private IntegerParameter() {}

    IntegerParameter(String name, int initValue, String documentation) {
        super(name, documentation);
        this.value = initValue;
    }


    public void set(int val) {
        value = val;
    }

    @Override
    public void set(Object object) {
        value = (int) object;
    }

    @Override
    public String get() {
        return Integer.toString(value);
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    @Override
    public void set(String str) {
        value = Integer.parseInt(str);
    }

}
