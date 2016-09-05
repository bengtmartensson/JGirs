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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for the Girs modules.
 */
public abstract class Module {

    public static Module newModule(String className, Class<?>[] classArray, Object[] objectArray)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String fullHardwareClassName = (className.contains(".") ? "" : "org.harctoolbox.jgirs.") + className;
        Class<?> clazz = Class.forName(fullHardwareClassName);
        Constructor<?> constructor = clazz.getConstructor(classArray);
        return (Module) constructor.newInstance(objectArray);
    }

    public static Module newModule(Element element) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        NodeList nodeList = element.getElementsByTagName("argument");
        Class<?>[] classArray = new Class<?>[nodeList.getLength()];
        Object[] objectArray = new Object[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);
            String type = e.getAttribute("type");
            classArray[i] = Utils.name2class(type);
            objectArray[i] = parseObject(type, e.getTextContent());
        }
        return newModule(element.getAttribute("class"), classArray, objectArray);
    }

    public static Object parseObject(String type, String value) {
        return
                type.equals("int") ? Integer.parseInt(value)
                : type.equals("boolean") ? Boolean.parseBoolean(value)
                : value;
    }

    private final HashMap<String, ICommand> commands;
    private final HashMap<String, IParameter> parameters;

    protected Module() {
        commands = new LinkedHashMap<>(8);
        parameters = new LinkedHashMap<>(8);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public Collection<ICommand> getCommands() {
        return commands.values();
    }

    public ArrayList<String> getCommandNames(boolean sort) {
        ArrayList<String> al = new ArrayList<>(commands.keySet());
        if (sort)
            Collections.sort(al);
        return al;
    }

    protected final void addCommand(ICommand command) {
        commands.put(command.getName(), command);
    }

//    public HashMap<String, IParameter> getParameters() {
//        return parameters;
//    }

    protected final void addParameter(IParameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    void addParametersTo(Parameters parameters) {
        parameters.addAll(this.parameters);
    }
}
