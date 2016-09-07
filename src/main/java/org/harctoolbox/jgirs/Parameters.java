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

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Models parameters in the Girs server.
 */
public class Parameters extends Module {
    private final HashMap<String, IParameter> parameterMap;

    public Parameters(CommandExecuter commandExecutor) {
        super(commandExecutor, null);
        this.parameterMap = new LinkedHashMap<>(8);
        addCommand(new ParameterCommand());
        //addCommand(new SetParameterCommand());
        //addCommand(new GetParameterCommand());
    }

    public void add(IParameter parameter) {
        parameterMap.put(parameter.getName(), parameter);
    }

    public void addAll(HashMap<String, IParameter> newParameters) {
        parameterMap.putAll(newParameters);
    }

    public IParameter get(String name) {
        return parameterMap.get(name);
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
        public String[] exec(String[] args) {
            String[] result = new String[parameterMap.size()];
            int i = 0;
            for (IParameter param : parameterMap.values())
                result[i++] = param.toString();

            return result;
        }
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
}
