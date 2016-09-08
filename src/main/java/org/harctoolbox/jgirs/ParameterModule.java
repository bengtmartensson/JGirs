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
import java.util.Map;

/**
 * Models parameters in the Girs server.
 */
public class ParameterModule extends Module {

    private final HashMap<String, Parameter> parameterMap;

    public ParameterModule(CommandExecuter commandExecutor) {
        super(commandExecutor, null);
        this.parameterMap = new HashMap<>(8);
        addCommand(new ParameterCommand());
        //addCommand(new SetParameterCommand());
        //addCommand(new GetParameterCommand());
    }

    public void add(Parameter parameter) {
        parameterMap.put(parameter.getName(), parameter);
    }

    public void addAll(HashMap<String, Parameter> newParameters) {
        parameterMap.putAll(newParameters);
    }

    public Parameter get(String name) {
        return parameterMap.get(name);
    }

    public Parameter find(String str) throws AmbigousParameterException {
        if (parameterMap.containsKey(str))
            return get(str);

        Parameter candidate = null;
        for (Map.Entry<String, Parameter> kvp : parameterMap.entrySet()) {
            if (kvp.getKey().startsWith(str)) {
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

    public boolean getBooleanr(String name) {
        Parameter param = parameterMap.get(name);
        return ((BooleanParameter) param).getValue();
    }

    public String getString(String name) {
        Parameter param = parameterMap.get(name);
        return ((StringParameter) param).getValue();
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

    public static class AmbigousParameterException extends Exception {
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
        public String exec(String[] args) {
            List<String> result = new ArrayList<>(parameterMap.size());
            int i = 0;
            parameterMap.values().stream().forEach((param) -> {
                result.add(param.toString());
            });

            return Utils.sortedString(result);
        }
    }
}
