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

import java.util.Date;

/**
 * A pretty useless module, more as an example.
 */
public class DummyModule extends Module {

    private final String nonsense;

    public DummyModule(CommandExecuter commandExecutor, ParameterModule parameters, String nonsense) {
        super(commandExecutor, parameters);
        this.nonsense = nonsense;
        addCommand(new DateCommand());
        addCommand(new NonsenseCommand());
    }

    public DummyModule(CommandExecuter commandExecutor, ParameterModule parameters) {
        this(commandExecutor, parameters, null);
    }

    private static class DateCommand implements ICommand {

        @Override
        public String getName() {
            return "date";
        }

        @Override
        public String exec(String[] args) {
            return (new Date()).toString();
        }
    }

    private class NonsenseCommand implements ICommand {

        @Override
        public String getName() {
            return "nonsense";
        }

        @Override
        public String exec(String[] args) {
            return nonsense;
        }
    }
}
