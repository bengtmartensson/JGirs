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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.xml.validation.Schema;
import org.harctoolbox.IrpMaster.IrpMasterException;
import org.harctoolbox.IrpMaster.XmlUtils;
import org.harctoolbox.girr.RemoteSet;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigFile {

    private static RemoteSet parseRemoteSet(Element element) throws NoSuchRemoteTypeException, IOException, SAXException, ParseException {
        String type = element.getAttribute("type");
        if (type.equalsIgnoreCase("lircd"))
            return parseLirc(element);
        else if (type.equalsIgnoreCase("girr") || type.isEmpty())
            return parseGirr(element);
        else
            throw new NoSuchRemoteTypeException(type);
    }

    private static RemoteSet parseLirc(Element element) throws MalformedURLException, IOException {
        URL url = new URL(element.getAttribute("url"));
        InputStream inputStream = url.openStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        return org.harctoolbox.jirc.ConfigFile.parseConfig(reader, url.toString(), true, null, false);
    }

    private static RemoteSet parseGirr(Element element) throws IOException, SAXException, ParseException {
        URL url = new URL(element.getAttribute("url"));
        Document doc = XmlUtils.openXmlUrl(url, (Schema) null, true, true);
        RemoteSet remoteSet = new RemoteSet(doc);
        return remoteSet;
    }

    private final List<GirsHardware> irHardwareList;
    private RemoteCommandDataBase remoteCommandsDataBase;
    private final List<Module> moduleList;
    private final HashMap<String, Integer> integerOptions;
    private final HashMap<String, String> stringOptions;
    private final HashMap<String, Boolean> booleanOptions;

    public ConfigFile() {
        remoteCommandsDataBase = new RemoteCommandDataBase(true);
        irHardwareList = new ArrayList<>(8);
        moduleList = new ArrayList<>(4);
        integerOptions = new HashMap<>(4);
        booleanOptions = new HashMap<>(4);
        stringOptions = new HashMap<>(16);
    }

    public ConfigFile(Document doc) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, IOException, NoSuchRemoteTypeException, SAXException, ParseException, IrpMasterException {
        this();
        if (doc == null)
            return;

        NodeList nodeList = doc.getElementsByTagName("hardware-item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            GirsHardware hw = new GirsHardware((Element) nodeList.item(i));
            irHardwareList.add(hw);
        }

        nodeList = doc.getElementsByTagName("named-remote");
        ArrayList<RemoteSet> remoteSetList = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++)
            remoteSetList.add(parseRemoteSet((Element) nodeList.item(i)));
        remoteCommandsDataBase = new RemoteCommandDataBase(remoteSetList, true);

        nodeList = doc.getElementsByTagName("module");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Module module = Module.newModule((Element) nodeList.item(i));
            moduleList.add(module);
        }

        nodeList = doc.getElementsByTagName("option");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element e = (Element) nodeList.item(i);
            String name = e.getAttribute("name");
            String type = e.getAttribute("type");
            String value = e.getTextContent();
            switch (type) {
                case "int":
                    integerOptions.put(name, Integer.parseInt(value));
                    break;
                case "boolean":
                    booleanOptions.put(name, Boolean.parseBoolean(value));
                    break;
                default: // String
                    stringOptions.put(name, value);
                    break;
            }
        }
    }

    public ConfigFile(String url) throws SAXException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, HarcHardwareException, NoSuchRemoteTypeException, ParseException, IrpMasterException, IOException {
        this(url != null ? XmlUtils.openXmlUrl(url, (Schema) null, false, true) : null);
    }

    /**
     * @return the irHardwareList
     */
    public List<GirsHardware> getIrHardwareList() {
        return Collections.unmodifiableList(irHardwareList);
    }

    /**
     * @return the remoteCommandsDataBase
     */
    public RemoteCommandDataBase getRemoteCommandsDataBase() {
        return remoteCommandsDataBase;
    }

    /**
     * @return the moduleList
     */
    public List<Module> getModuleList() {
        return Collections.unmodifiableList(moduleList);
    }

    /**
     * @param name
     * @return the stringOptions
     */
    public String getStringOption(String name) {
        return stringOptions.get(name);
    }

    /**
     * @param name
     * @return the booleanOptions
     */
    public boolean getBooleanOption(String name) {
        return booleanOptions.get(name);
    }

    /**
     * @param name
     * @return the booleanOptions
     */
    public int getIntegerOption(String name) {
        return integerOptions.get(name);
    }

    void addHardware(GirsHardware irHardware) {
        irHardwareList.add(irHardware);
    }

    void setStringOption(String name, String value) {
        stringOptions.put(name, value);
    }

    void setIntegerOption(String name, int value) {
        integerOptions.put(name, value);
    }

    void setBooleanOption(String name, boolean value) {
        booleanOptions.put(name, value);
    }

    void addGirs(List<String> girr) throws ParseException, IOException, SAXException, IrpMasterException {
        remoteCommandsDataBase.add(girr);
    }

    public GirsHardware getDefaultOutputHardware() {
        for (GirsHardware hardware : irHardwareList)
            if (hardware.isOutputDefault())
                return hardware;

        return null;
    }

    public GirsHardware getDefaultInputHardware() {
        for (GirsHardware hardware : irHardwareList)
            if (hardware.isInputDefault())
                return hardware;

        return null;
    }

    public static class NoSuchRemoteTypeException extends Exception {

        public NoSuchRemoteTypeException(String type) {
            super("No such remote type: " + type);
        }
    }
}
