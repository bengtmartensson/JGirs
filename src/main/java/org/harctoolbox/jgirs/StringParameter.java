/*
Copyright (C) 2014 Bengt Martensson.

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

/**
 *
 */
abstract class StringParameter implements Parameter {
    String value;

    @Override
    public void set(String str) {
        value = str.toLowerCase(Locale.US);
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public String toString() {
        return getName() + "=" + get();
    }

    private StringParameter() {}

    StringParameter(String initValue) {
        this.value = initValue;
    }
}
