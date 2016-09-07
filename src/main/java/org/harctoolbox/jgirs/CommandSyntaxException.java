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

/**
 * This is thrown when calling a command with erroneous syntax.
 */
public class CommandSyntaxException extends JGirsException {

    CommandSyntaxException() {
        super("Command syntax error");
    }

    CommandSyntaxException(String name){
        super("Command syntax error: " + name);
    }

    CommandSyntaxException(String name, int noArgs){
        super("The command \" + name + \" takes " + noArgs + " argument(s).");
    }

    CommandSyntaxException(String name, String reason){
        super("Command syntax error: " + name + " (" + reason + ")");
    }
}
