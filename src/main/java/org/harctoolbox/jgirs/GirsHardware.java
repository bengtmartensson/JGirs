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
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
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

    private IHarcHardware hardware;
    private String url;
    private String description;
    private String name;
    private boolean immediateOpen;

    private GirsHardware() {
        this.description = null;
        this.url = null;
        this.name = null;
        this.hardware = null;
        this.immediateOpen = false;
    }

    public GirsHardware(IHarcHardware hardware) {
        this();
        this.hardware = hardware;
    }

    public GirsHardware(Element element) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        name = element.getAttribute("name");
        description = getChildContent(element, "description");
        url = getChildContent(element, "www");
        immediateOpen = element.getElementsByTagName("immediate-open").getLength() > 0;

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

    GirsHardware(List<String> params, String name) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException {
        this.name = name;
        description = null;
        url = null;
        hardware = newIrHardware(params);
    }

    GirsHardware(GirsHardware orig) {
        copyFrom(orig);
    }

    void copyFrom(GirsHardware orig) {
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

    /**
     * @return the immediateOpen
     */
    public boolean isImmediateOpen() {
        return immediateOpen;
    }

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

    public static class GirsHardwareNameComparator implements Comparator<GirsHardware>, Serializable {

        @Override
        public int compare(GirsHardware hw1, GirsHardware hw2) {
            return hw1.name.compareToIgnoreCase(hw2.name);
        }
    }
}
