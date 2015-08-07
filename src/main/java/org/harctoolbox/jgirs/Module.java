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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 */
public abstract class Module {

    private HashMap<String, Command> commands;
    private HashMap<String, Parameter> parameters;

    public String getName() {
        return getClass().getSimpleName();
    }

    public Collection<Command> getCommands() {
        return commands.values();
    }

    public ArrayList<String> getCommandNames(boolean sort) {
        ArrayList<String> al = new ArrayList<>(commands.keySet());
        if (sort)
            Collections.sort(al);
        return al;
    }

    protected void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    public HashMap<String, Parameter> getParameters() {
        return parameters;
    }

    protected void addParameter(Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }

    protected Module() {
        commands = new LinkedHashMap<>();
        parameters = new LinkedHashMap<>();
    }

    public static String join(Iterable<String> strings) {
        return join(strings, " ");
    }

    public static String join(Iterable<String> strings, String separator) {
        StringBuilder str = new StringBuilder();
        for (String name : strings)
            str.append(name).append(separator);

        return str.substring(0, str.length() - separator.length());
    }

    public static <T extends Object> String joinObjects(Iterable<T> objects) {
        return joinObjects(objects, " ");
    }

    public static <T extends Object> String joinObjects(Iterable<T> objects, String separator) {
        StringBuilder str = new StringBuilder();
        for (Object object : objects)
            str.append(object.toString()).append(separator);

        return str.substring(0, Math.max(str.length() - separator.length(), 0));
    }
}
