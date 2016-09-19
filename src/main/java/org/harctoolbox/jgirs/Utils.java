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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import javax.xml.validation.Schema;
import org.harctoolbox.IrpMaster.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class Utils {

    public static final String INT              = "int";
    public static final String BOOLEAN          = "boolean";
    public static final String STRING           = "String";

    public static Object parseObject(String type, String value) {
        return
                type.equals(INT) ? Integer.parseInt(value)
                : type.equals(BOOLEAN) ? Boolean.parseBoolean(value)
                : value;
    }

    public static Class<?> name2class(String type) throws ClassNotFoundException {
        return type.equals(INT) ? int.class
                : type.equals(BOOLEAN) ? boolean.class
                : type.equalsIgnoreCase(STRING) || type.isEmpty() ? String.class
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
        return sortedString(things, Parameters.getInstance().getString(Parameters.LISTSEPARATOR));
    }

    public static String sortedString(String[] things) throws NoSuchParameterException {
        return sortedString(Arrays.asList(things));
    }

    public static String[] tokenizer(String s, String separator) {
        String[] toks = s.split(separator + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < toks.length; i++)
            toks[i] = toks[i].replaceFirst("\"", "").replaceFirst("\"$", "").trim();
        return toks;
    }

    public static String[] tokenizer(String s) {
        return tokenizer(s, "\\s+");
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

    public static List<String> singletonArrayList(String string) {
        ArrayList<String> list = new ArrayList<>(1);
        list.add(string);
        return list;
    }

    public static List<String> toSortedList(Collection<String> collection) {
        ArrayList<String> list = new ArrayList<>(collection);
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    public static List<String> findStringsWithPrefix(Collection<String> collection, String prefix) {
        List<String> result = new ArrayList<>(8);
        if (collection.contains(prefix))
            result.add(prefix);
        else
            collection.stream().filter((candidate) -> (candidate.toLowerCase(Locale.US).startsWith(prefix.toLowerCase(Locale.US)))).forEach((candidate) -> {
                result.add(candidate);
            });
        return result;
    }

    public static Map<String, Long> toParameters(long F) {
        Map<String, Long> parameters = new HashMap<>(1);
        parameters.put("F", F);
        return parameters;
    }

    public static Map<String, Long> toParameters(long D, long F) {
        Map<String, Long> parameters = new HashMap<>(2);
        parameters.put("D", D);
        parameters.put("F", F);
        return parameters;
    }

    public static Map<String, Long> toParameters(long D, long S, long F) {
        Map<String, Long> parameters = new HashMap<>(3);
        parameters.put("D", D);
        parameters.put("S", S);
        parameters.put("F", F);
        return parameters;
    }

    public static Map<String, Long> toParameters(long D, long S, long F, long T) {
        Map<String, Long> parameters = new HashMap<>(4);
        parameters.put("D", D);
        parameters.put("S", S);
        parameters.put("F", F);
        parameters.put("T", T);
        return parameters;
    }

    static String pack(Iterable<String> result, String separator) {
        StringJoiner stringJoiner = new StringJoiner(separator);

        for (String s : result)
            stringJoiner.add(s.matches("\\s+") ? ("\"" + s + "\"") : s);
        return stringJoiner.toString();
    }

    private Utils() {
    }
}
