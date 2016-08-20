/*
Copyright (C) 2015 Bengt Martensson.

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
 * The parameter settable by the Parameters class. Call it GirsParameter to avoid name collisions.
 */
public class GirsParameter {

    private Class<?> clazz;
    private String svalue;
    private int ivalue;
    private boolean bvalue;
    private Object object;

    public GirsParameter(String par) {
        String[] p = par.split(":");
        if (p.length == 1) {
            clazz = String.class;
            svalue = par;
        } else {
            switch (p[0]) {
                case "string":
                    clazz = String.class;
                    svalue = p[1];
                    object = svalue;
                    break;
                case "int":
                    clazz = int.class;
                    ivalue = Integer.parseInt(p[1]);
                    object = ivalue;
                    break;
                case "bool":
                case "boolean":
                    clazz = boolean.class;
                    bvalue = Boolean.parseBoolean(p[1]);
                    object = bvalue;
                    break;
                default:
                    throw new IllegalArgumentException("Type " + p[0] + " unknown");
            }
        }
    }

    /**
     * @return the clazz
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * @return the svalue
     */
    public String getSvalue() {
        return svalue;
    }

    /**
     * @return the ivalue
     */
    public int getIvalue() {
        return ivalue;
    }

    /**
     * @return the bvalue
     */
    public boolean isBvalue() {
        return bvalue;
    }

    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }
}
