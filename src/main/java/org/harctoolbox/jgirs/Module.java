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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Abstract base class for the Girs modules.
 */
public abstract class Module {

    public static String join(Iterable<String> strings) {
        return join(strings, " ");
    }

    public static String join(Iterable<String> strings, String separator) {
        StringBuilder str = new StringBuilder(32);
        for (String name : strings)
            str.append(name).append(separator);

        return str.substring(0, str.length() - separator.length());
    }

    public static <T extends Object> String joinObjects(Iterable<T> objects) {
        return joinObjects(objects, " ");
    }

    public static <T extends Object> String joinObjects(Iterable<T> objects, String separator) {
        StringBuilder str = new StringBuilder(32);
        for (Object object : objects)
            str.append(object.toString()).append(separator);

        return str.substring(0, Math.max(str.length() - separator.length(), 0));
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
