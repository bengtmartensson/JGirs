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

package org.example.jgirs;

import java.util.Date;
import java.util.List;
import org.harctoolbox.jgirs.CommandSyntaxException;
import org.harctoolbox.jgirs.ICommand;
import org.harctoolbox.jgirs.Module;
import org.harctoolbox.jgirs.Utils;

/**
 * A pretty useless module, to serve as an example.
 */
public class Dummy extends Module {

    private final String nonsense;

    public Dummy(String nonsense) {
        super();
        this.nonsense = nonsense;
        addCommand(new DateCommand());
        addCommand(new NonsenseCommand());
        addCommand(new WhyCommand());
    }

    public Dummy() {
        this(null);
    }

    private static class DateCommand implements ICommand {

        private static final String DATE = "date";

        @Override
        public String getName() {
            return DATE;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException {
            checkNoArgs(DATE, args.length, 0);
            return Utils.singletonArrayList(new Date().toString());
        }
    }

    private static class WhyCommand implements ICommand {

        private static final String WHY = "why";

        @Override
        public String getName() {
            return WHY;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException {
            checkNoArgs(WHY, args.length, 0);
            return Utils.singletonArrayList("Why? Becase this is not Matlab!");
        }
    }

    private class NonsenseCommand implements ICommand {

        private static final String NONSENSE = "nonsense";

        @Override
        public String getName() {
            return NONSENSE;
        }

        @Override
        public List<String> exec(String[] args) throws CommandSyntaxException {
            checkNoArgs(NONSENSE, args.length, 0);
            return Utils.singletonArrayList(nonsense);
        }
    }
}
