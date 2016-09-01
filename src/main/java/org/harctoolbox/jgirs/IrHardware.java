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

/**
 *
 */

package org.harctoolbox.jgirs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.w3c.dom.Document;

public final class IrHardware {
    public static List<IrHardware> parseConfig(Document doc) {
        // TODO
        return new ArrayList<>(0);
    }

    public static List<IrHardware> singleHardware(String className, List<String> parameters) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        List<IrHardware> list = new ArrayList<>(1);
        IrHardware irHardware = new IrHardware(className, parameters);
        list.add(irHardware);
        return list;
    }

    private IHarcHardware hardware;

    public IrHardware(String className, List<String> parameters)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        Class<?>[] classArray;
        Object[] objectArray;
        if (parameters == null) {
            classArray = null;
            objectArray = null;
        } else {
            classArray = new Class<?>[parameters.size()];
            objectArray = new Object[parameters.size()];
            int index = 0;
            for (String p : parameters) {
                HardwareParameter parameter = new HardwareParameter(p);
                classArray[index] = parameter.getClazz();
                objectArray[index] = parameter.getObject();
                index++;
            }
        }
        hardware = new IrHardware(className, classArray, objectArray).hardware;
    }

    private IrHardware(String className, Class<?>[] classArray, Object[] objectArray)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        String fullHardwareClassName = (className.contains(".") ? "" : "org.harctoolbox.harchardware.ir.") + className;
        Class<?> clazz = Class.forName(fullHardwareClassName);

        //@SuppressWarnings("unchecked")
        Constructor<?> constructor = clazz.getConstructor(classArray);
        hardware = (IHarcHardware) constructor.newInstance(objectArray);
//        System.err.print("Trying to open hardware...");
//        hardware.open();
//        System.err.println("done");
//        if (openDelay > 0) {
//            try {
//                Thread.sleep(openDelay);
//            } catch (InterruptedException ex) {
//            }
//        }
//        hardware.setVerbosity(verbose);
//        hardware.setDebug(debug);
    }


    /**
     * @return the hardware
     */
    public IHarcHardware getHardware() {
        return hardware;
    }

//    private Object[] toObjects(List<String> parameters) {
//        Object[] objectArray = new Object[parameters.size()];
//        int index = 0;
//        for (String p : parameters) {
//            HardwareParameter parameter = new HardwareParameter(p);
//            objectArray[index++] = parameter.getObject();
//        }
//    }

    private static class HardwareParameter {
        private Class<?> clazz;
        private String svalue;
        private int ivalue;
        private boolean bvalue;
        private Object object;

        HardwareParameter(String par) {
            String[] p = par.split(":");
            if (p.length == 1) {
                clazz = String.class;
                svalue = par;
                object = svalue;
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
}
