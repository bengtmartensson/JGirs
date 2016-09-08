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
import java.util.List;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is a package IHarcHardware, with some additional properties like name. url (for an embedded WWW server), description, etc.
 * @author bengt
 */

public final class GirsHardware {

    private static GirsHardware defaultTransmittingHardware = null;
    private static GirsHardware defaultReceivingHardware = null;
    private static GirsHardware defaultCapturingHardware = null;

//    public static List<GirsHardware> singleHardware(List<String> params) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
//        List<GirsHardware> list = new ArrayList<>(1);
//        GirsHardware irHardware = new GirsHardware(params, "default", true, true);
//        list.add(irHardware);
//        return list;
//    }

    private static IHarcHardware newIrHardware(String className, Class<?>[] classArray, Object[] objectArray)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        String fullHardwareClassName = (className.contains(".") ? "" : "org.harctoolbox.harchardware.ir.") + className;
        Class<?> clazz = Class.forName(fullHardwareClassName);

        Constructor<?> constructor = clazz.getConstructor(classArray);
        IHarcHardware hardware = (IHarcHardware) constructor.newInstance(objectArray);
        return hardware;
    }

    private static String getChildContent(Element e, String tagname) {
        NodeList nl = e.getElementsByTagName(tagname);
        return nl.getLength() > 0 ? nl.item(0).getTextContent() : null;
    }

    //    public IrHardware(String className, List<String> parameters)
//            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
//        Class<?>[] classArray;
//        Object[] objectArray;
//        if (parameters == null) {
//            classArray = null;
//            objectArray = null;
//        } else {
//            classArray = new Class<?>[parameters.size()];
//            objectArray = new Object[parameters.size()];
//            int index = 0;
//            for (String p : parameters) {
//                HardwareParameter parameter = new HardwareParameter(p);
//                classArray[index] = parameter.getClazz();
//                objectArray[index] = parameter.getObject();
//                index++;
//            }
//        }
//        hardware = new IrHardware(className, classArray, objectArray).hardware;
//    }

    public static IHarcHardware newIrHardware(List<String> list)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        Class<?>[] classArray = new Class<?>[list.size() - 1];
        Object[] objectArray = new Object[list.size() - 1];
        for (int index = 1; index < list.size(); index++) {
            HardwareParameter parameter = new HardwareParameter(list.get(index));
            classArray[index-1] = parameter.getClazz();
            objectArray[index-1] = parameter.getObject();
        }
        return newIrHardware(list.get(0), classArray, objectArray);
    }

    /**
     * @return the defaultSendingHardware
     */
    public static GirsHardware getDefaultTransmittingHardware() {
        return defaultTransmittingHardware;
    }

    /**
     * @param aDefaultSendingHardware the defaultSendingHardware to set
     */
    public static void setDefaultTransmittingHardware(GirsHardware aDefaultSendingHardware) {
        defaultTransmittingHardware = aDefaultSendingHardware;
    }

    /**
     * @return the defaultReceivingHardware
     */
    public static GirsHardware getDefaultReceivingHardware() {
        return defaultReceivingHardware;
    }

    /**
     * @param aDefaultReceivingHardware the defaultReceivingHardware to set
     */
    public static void setDefaultReceivingHardware(GirsHardware aDefaultReceivingHardware) {
        defaultReceivingHardware = aDefaultReceivingHardware;
    }

    /**
     * @return the defaultCapturingHardware
     */
    public static GirsHardware getDefaultCapturingHardware() {
        return defaultCapturingHardware;
    }

    /**
     * @param aDefaultCapturingHardware the defaultCapturingHardware to set
     */
    public static void setDefaultCapturingHardware(GirsHardware aDefaultCapturingHardware) {
        defaultCapturingHardware = aDefaultCapturingHardware;
    }

    private IHarcHardware hardware;
    private String url;
    private String description;
    private String name;
    private boolean outputDefault;
    private boolean capturingDefault;
    private boolean receivingDefault;

    private GirsHardware() {
        this.outputDefault = false;
        this.capturingDefault = false;
        this.receivingDefault = false;
        this.description = null;
        this.url = null;
        this.name = null;
        this.hardware = null;
    }

    public GirsHardware(IHarcHardware hardware) {
        this();
        this.hardware = hardware;
    }

    public GirsHardware(Element element) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        capturingDefault = Boolean.parseBoolean(element.getAttribute("capturingdefault"));
        receivingDefault = Boolean.parseBoolean(element.getAttribute("receivingdefault"));
        outputDefault = Boolean.parseBoolean(element.getAttribute("outputdefault"));
        name = element.getAttribute("name");
        description = getChildContent(element, "description");
        url = getChildContent(element, "www");

        NodeList args = element.getElementsByTagName("argument");
        Class<?>[] classArray = new Class<?>[args.getLength()];
        Object[] objectArray = new Object[args.getLength()];
        for (int i = 0; i < args.getLength(); i++) {
            Element e = (Element) args.item(i);
            String type = e.getAttribute("type");
            classArray[i] = Utils.name2class(type);
            objectArray[i] = Utils.parseObject(type, e.getTextContent());
        }
        hardware = newIrHardware(element.getAttribute("class"), classArray, objectArray);
    }

    GirsHardware(List<String> params, String name, boolean capturingDefault, boolean receivingDefault, boolean outputDefault) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        this.capturingDefault = capturingDefault;
        this.receivingDefault = receivingDefault;
        this.outputDefault = outputDefault;
        this.name = name;
        description = null;
        url = null;
        hardware = newIrHardware(params);
    }

    GirsHardware(GirsHardware orig) {
        copyFrom(orig);
    }

    void copyFrom(GirsHardware orig) {
        this.outputDefault = orig.outputDefault;
        this.capturingDefault = orig.capturingDefault;
        this.receivingDefault = orig.receivingDefault;
        this.description = orig.description;
        this.url = orig.url;
        this.name = orig.name;
        this.hardware = orig.hardware;
    }

    /**
     * @return the hardware
     */
    public IHarcHardware getHardware() {
        return hardware;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    boolean isOutputDefault() {
        return outputDefault;
    }

    boolean isCapturingDefault() {
        return capturingDefault;
    }

    boolean isReceivingDefault() {
        return receivingDefault;
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
        private final Class<?> clazz;
        private final Object object;

        HardwareParameter(String par) throws ClassNotFoundException {
            String[] p = par.split(":");
            if (p.length == 1) {
                clazz = java.lang.String.class;
                object = p[0];
            } else {
                clazz = Class.forName(p[0]);
                object = Utils.parseObject(p[0], p[1]);
            }
        }

        /**
         * @return the clazz
         */
        public Class<?> getClazz() {
            return clazz;
        }

        /**
         * @return the object
         */
        public Object getObject() {
            return object;
        }
    }
}
