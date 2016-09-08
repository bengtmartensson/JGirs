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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Models parameters in the Girs server.
 */
public class ParameterModule extends Module {

    public static final String IRPPROTOCOLSINI = "irpProtocolsIni";
    public static final String VERBOSITY = "verbosity"; // Do not change to the more grammatically correct "verbose"
    public static final String LISTSEPARATOR = "listseparator";

    private static volatile ParameterModule instance = null;

    public static ParameterModule getInstance() {
        return instance;
    }

    public static ParameterModule newParameterModule() {
        if (instance != null)
            throw new InvalidMultipleInstantiation();

        instance = new ParameterModule();
        return instance;
    }

    private final HashMap<String, Parameter> parameterMap;

    private ParameterModule() {
        super();
        this.parameterMap = new HashMap<>(8);
        addCommand(new ParameterCommand());

        // In this constructor, use add instead of addParameter.
        add(new StringParameter(LISTSEPARATOR, " ", "String to be used between entries in list"));
        add(new BooleanParameter(VERBOSITY, false, "Execute commands verbosely"));
        //addCommand(new SetParameterCommand());
        //addCommand(new GetParameterCommand());
    }

    public final void add(Parameter parameter) {
        parameterMap.put(parameter.getName(), parameter);
    }

    public void addAll(HashMap<String, Parameter> newParameters) {
        parameterMap.putAll(newParameters);
    }

    public void addAll(List<Parameter> list) {
        list.stream().forEach((parameter) -> {
            add(parameter);
        });
    }

    public Parameter get(String name) {
        return parameterMap.get(name);
    }

    public Parameter find(String str) throws AmbigousParameterException {
        if (parameterMap.containsKey(str))
            return get(str);

        Parameter candidate = null;
        for (Map.Entry<String, Parameter> kvp : parameterMap.entrySet()) {
            if (kvp.getKey().toLowerCase(Locale.US).startsWith(str.toLowerCase(Locale.US))) {
                if (candidate != null)
                    throw new AmbigousParameterException(str);
                candidate = kvp.getValue();
            }
        }
        return candidate; // possibly null
    }

    public int getInteger(String name) {
        Parameter param = parameterMap.get(name);
        return ((IntegerParameter) param).getValue();
    }

    public boolean getBoolean(String name) {
        Parameter param = parameterMap.get(name);
        return ((BooleanParameter) param).getValue();
    }

    public String getString(String name) {
        Parameter param = parameterMap.get(name);
        return ((StringParameter) param).getValue();
    }

    public void setInteger(String name, int value) throws Parameter.IncorrectParameterType {
        Parameter param = parameterMap.get(name);
        if (!(param instanceof IntegerParameter))
            throw new Parameter.IncorrectParameterType("int");
        ((IntegerParameter) param).set(value);
    }

    public void setBoolean(String name, boolean value) throws Parameter.IncorrectParameterType {
        Parameter param = parameterMap.get(name);
        if (!(param instanceof BooleanParameter))
            throw new Parameter.IncorrectParameterType("boolean");
        ((BooleanParameter) param).set(value);
    }

    public void setString(String name, String value) throws Parameter.IncorrectParameterType {
        Parameter param = parameterMap.get(name);
        if (!(param instanceof StringParameter))
            throw new Parameter.IncorrectParameterType("String");
        ((StringParameter) param).set(value);
    }

//    private class SetParameterCommand implements ICommand {
//
//        @Override
//        public String getName() {
//            return "setparameter";
//        }
//
//        @Override
//        public String[] exec(String[] args) throws NoSuchParameterException {
//            //int index = 1;
//            String name = args.get(1);//[index++];
//            if (!parameterMap.containsKey(name))
//                throw new NoSuchParameterException(name);
//            IParameter parameter = parameterMap.get(name);
//            parameter.set(args.get(2));
//            return new ArrayList<>(0);
//        }
//
//    }

    public static class AmbigousParameterException extends CommandException {
        public AmbigousParameterException(String name) {
            super("Ambigous parameter name: " + name);
        }
    }

    //    private static class GetParameterCommand implements ICommand {
//
//        @Override
//        public String getName() {
//            return "getparameter";
//        }
//
//        @Override
//        public String[] exec(String[] args) throws NoSuchParameterException {
////            int index = 1;
////            String name = args[index];
////            if (!allParameters.containsKey(name))
////                throw new NoSuchParameterException(name);
////            final IParameter parameter = allParameters.get(name);
////            return new ArrayString[]() {{ add(parameter.get()); }};
//            return new ArrayList<>(8);
//        }
//    }
//
////    public void addParameters(Module module) {
////        allParameters.putAll(module.getParameters());
////    }

    private class ParameterCommand implements ICommand {

        @Override
        public String getName() {
            return "parameter";
        }

        @Override
        public String exec(String[] args) throws CommandException {
            List<String> result = new ArrayList<>(parameterMap.size());
            switch (args.length) {
                case 1:
                    parameterMap.values().stream().forEach((param) -> {
                        result.add(param.toString());
                    });
                    break;
                case 2: {
                    String fragment = args[1];
                    parameterMap.values().stream().forEach((param) -> {
                        if (param.getName().toLowerCase(Locale.US).startsWith(fragment.toLowerCase(Locale.US)))
                            result.add(param.toString());
                    });
                }
                break;
                case 3: {
                    String fragment = args[1];
                    String newValue = args[2];

                    Parameter parameter = find(fragment);
                    parameter.set(newValue);
                    result.add(parameter.toString());
                }
                break;
                default:
                    throw new CommandSyntaxException("parameter", 0, 2);
            }

            return Utils.sortedString(result);
        }
    }
}
