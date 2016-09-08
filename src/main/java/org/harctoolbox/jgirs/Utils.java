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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Utils {

    public static Object parseObject(String type, String value) {
        return
                type.equals("int") ? Integer.parseInt(value)
                : type.equals("boolean") ? Boolean.parseBoolean(value)
                : value;
    }

    public static Class<?> name2class(String type) throws ClassNotFoundException {
        return type.equals("int") ? int.class
                : type.equals("boolean") ? boolean.class
                : type.equalsIgnoreCase("string") || type.isEmpty() ? String.class
                : Class.forName(type);
    }

    public static String sortedString(Iterable<String> things) {
        List<String> list = new ArrayList<>(8);
        for (String string : things) {
            String s = string.matches(".*\\s+.*") ? ("\"" + string + "\"") : string;
            list.add(s);
        }

        Collections.sort(list);

        return String.join(ParameterModule.getInstance().getString(ParameterModule.LISTSEPARATOR), list);
    }

    public static String sortedString(String[] things) {
        return sortedString(Arrays.asList(things));
    }

    public static String[] tokenizer(String s) {
        String[] toks = s.split("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < toks.length; i++)
            toks[i] = toks[i].replaceFirst("^\"", "").replaceFirst("\"$", "");
        return toks;
    }

    private Utils() {
    }
}
