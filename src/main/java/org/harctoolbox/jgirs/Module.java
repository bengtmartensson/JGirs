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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.harctoolbox.harchardware.HarcHardwareException;
import org.harctoolbox.harchardware.IHarcHardware;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for the Girs modules.
 */
public abstract class Module {

    public static final String OK = Engine.OK;
    public static final String ERROR = Engine.ERROR;

    protected static void checkNoArgs(String name, int length, int min, int max) throws CommandSyntaxException {
        if (length < min  || length > max)
            throw new CommandSyntaxException(name, min, max);
    }

//    public static Module newModule(String className, Class<?>[] classArray, Object[] objectArray)
//            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        String fullHardwareClassName = (className.contains(".") ? "" : "org.harctoolbox.jgirs.") + className;
//        Class<?> clazz = Class.forName(fullHardwareClassName);
//        Constructor<?> constructor = clazz.getConstructor(classArray);
//        return (Module) constructor.newInstance(objectArray);
//    }

//    public static Module newModule(Element element) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        NodeList nodeList = element.getElementsByTagName("argument");
//        Class<?>[] classArray = new Class<?>[nodeList.getLength()];
//        Object[] objectArray = new Object[nodeList.getLength()];
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            Element e = (Element) nodeList.item(i);
//            String type = e.getAttribute("type");
//            classArray[i] = Utils.name2class(type);
//            objectArray[i] = parseObject(type, e.getTextContent());
//        }
//        return newModule(element.getAttribute("class"), classArray, objectArray);
//    }

    //private final HashMap<String, ICommand> commands;
    //protected final CommandExecuter commandExecuter;
    //protected final ParameterModule parameters;
    //private final HashMap<String, IParameter> parameterMap;

    protected Module() {
//        if (instance != null)
//            throw new InvalidMultipleInstantiation();
        //this.commandExecuter = commandExecuter;
        //this.parameters = parameters;
        //parameters = new LinkedHashMap<>(4);
    }

    public String getName() {
        return getClass().getSimpleName();
    }

//    public Collection<ICommand> getCommands() {
//        return commands.values();
//    }

//    public ArrayList<String> getCommandNames(boolean sort) {
//        ArrayList<String> al = new ArrayList<>(commands.keySet());
//        if (sort)
//            Collections.sort(al);
//        return al;
//    }

    protected final void addCommand(ICommand command) {
        //commands.put(command.getName(), command);
        CommandExecuter.getMainExecutor().add(command);
    }

//    public HashMap<String, IParameter> getParameters() {
//        return parameters;
//    }

    protected final void addParameter(Parameter parameter) {
        ParameterModule.getInstance().add(parameter);
    }

    List<String> getCommandNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void initializeHardware(GirsHardware girsHardware, Class<?> requiredClass) throws IncompatibleHardwareException, HarcHardwareException, IOException, NoSuchParameterException {
        IHarcHardware hardware = girsHardware.getHardware();
        if (!(requiredClass.isInstance(hardware)))
            throw new IncompatibleHardwareException(requiredClass.getSimpleName());
        hardware.setVerbosity(Engine.getInstance().isVerbosity());
        if (!hardware.isValid())
            hardware.open();
    }

//    void addParametersTo(Parameters parameters) {
//        parameters.addAll(this.parameters);
//    }

    public static class ModulePars {

        private final String className;
        private final Class<?>[] classArray;
        private final Object[] objectArray;

        public ModulePars(Element element) throws ClassNotFoundException {
            className = element.getAttribute("class");
            NodeList nodeList = element.getElementsByTagName("argument");
            classArray = new Class<?>[nodeList.getLength()];
            objectArray = new Object[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element e = (Element) nodeList.item(i);
                String type = e.getAttribute("type");
                classArray[i] = Utils.name2class(type);
                objectArray[i] = Utils.parseObject(type, e.getTextContent());
            }
        }

//        public ModulePars(String className, Class<?>[] classArray, Object[] objectArray) {
//            this.className = className;
//            this.classArray = classArray;
//            this.objectArray = objectArray;
//        }

        public Module instantiate() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            String fullHardwareClassName = (className.contains(".") ? "" : "org.harctoolbox.jgirs.") + className;
            Class<?> clazz = Class.forName(fullHardwareClassName);
            Constructor<?> constructor = clazz.getConstructor(classArray);
            return (Module) constructor.newInstance(objectArray);
        }
    }
}
