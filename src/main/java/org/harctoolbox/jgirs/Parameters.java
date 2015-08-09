/*
Copyright (C) 2015 Bengt Martensson.

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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Models parameters in the Girs server.
 */
public class Parameters extends Module {
    private HashMap<String, IParameter> allParameters;

    private class ParametersCommand implements ICommand {

        @Override
        public String getName() {
            return "parameters";
        }

        @Override
        public List<String> exec(String[] args) {
            ArrayList<String> result = new ArrayList<>();
            for (IParameter param : allParameters.values())
                result.add(param.toString());
            return result;
        }
    }

    private class SetParameterCommand implements ICommand {

        @Override
        public String getName() {
            return "setparameter";
        }

        @Override
        public List<String> exec(String[] args) throws NoSuchParameterException {
            int index = 1;
            String name = args[index++];
            if (!allParameters.containsKey(name))
                throw new NoSuchParameterException(name);
            IParameter parameter = allParameters.get(name);
            parameter.set(args[index]);
            return new ArrayList<>();
        }

    }

    private class GetParameterCommand implements ICommand {

        @Override
        public String getName() {
            return "getparameter";
        }

        @Override
        public List<String> exec(String[] args) throws NoSuchParameterException {
            int index = 1;
            String name = args[index];
            if (!allParameters.containsKey(name))
                throw new NoSuchParameterException(name);
            final IParameter parameter = allParameters.get(name);
            return new ArrayList<String>() {{ add(parameter.get()); }};
        }
    }

    public void addParameters(Module module) {
        allParameters.putAll(module.getParameters());
    }

    public Parameters() {
        this.allParameters = new LinkedHashMap<>();
        addCommand(new ParametersCommand());
        addCommand(new SetParameterCommand());
        addCommand(new GetParameterCommand());
    }
}
