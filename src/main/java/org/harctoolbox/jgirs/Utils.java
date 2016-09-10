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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.validation.Schema;
import org.harctoolbox.IrpMaster.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


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

    public static String quoteStringsWithWhitespace(String string) {
        return string.matches(".*\\s+.*") ? ("\"" + string + "\"") : string;
    }

    public static String sortedString(Iterable<String> things, String separator) {
        List<String> list = new ArrayList<>(8);
        for (String string : things) {
            String s;
            if (string.contains("=")) {
                String[] chunks = string.split("=");
                s = chunks[0] + "=" + quoteStringsWithWhitespace(chunks[1]);
            } else
                s = string.matches(".*\\s+.*") ? ("\"" + string + "\"") : string;
            list.add(s);
        }

        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);

        return String.join(separator, list);
    }

    public static String sortedString(Iterable<String> things) throws NoSuchParameterException {
        return sortedString(things, ParameterModule.getInstance().getString(ParameterModule.LISTSEPARATOR));
    }

    public static String sortedString(String[] things) throws NoSuchParameterException {
        return sortedString(Arrays.asList(things));
    }

    public static String[] tokenizer(String s) {
        String[] toks = s.split("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < toks.length; i++)
            toks[i] = toks[i].replaceFirst("^\"", "").replaceFirst("\"$", "");
        return toks;
    }

    public static Document openXmlUrl(URL url, Schema schema, boolean isNamespaceAware, boolean isXIncludeAware) throws IOException, SAXException {
        InputStream inputStream = url.openStream();
        return XmlUtils.openXmlStream(inputStream, schema, isNamespaceAware, isXIncludeAware);
    }

    public static Document openXmlUrl(String url, Schema schema, boolean isNamespaceAware, boolean isXIncludeAware) throws SAXException, IOException {
        try {
            URL u = new URL(url);
            return openXmlUrl(u, schema, isNamespaceAware, isXIncludeAware);
        } catch (MalformedURLException ex) {
        }
        return XmlUtils.openXmlFile(new File(url), schema, isNamespaceAware, isXIncludeAware);
    }

    private Utils() {
    }
}
