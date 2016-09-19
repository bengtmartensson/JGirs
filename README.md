# JGirs
An IR server, compatible with [Girs](http://www.harctoolbox.org/Girs.html).
It can be considered as "IrScrutinizer as server", as it builds on the same
components as IrScrutinizer: the renderer IrpMaster,
the hardware structures of HarcHardware, the Lirc subset Jirc, the Girr support.

JGirs implements the "server" in my [architecture](http://harctoolbox.org/architecture.html).
(With an presently not yet implemented option, it may be possible for an instance
instead to fulfil the "listener" role.

## Demarcation
The program  should be kept fairly "dumb", basically executing simple commands.
It should not be extended with, e.g. embedded script language, macros or the such.
The usage of IrScrutinizer and JGirs should not overlap.

## The configuration file
At start, the program reads a configuration file. This is an XML file consisting
of declarations of variables, modules, remotes data bases, and deployed hardware.
An XML scheme is planned, but at the time of this writing not yet implemented.

### Varibles
A variable in JGirs has as `type` either `string` (default), `int`, or `boolean`. It is
declared either in  the Java code or in the configuration file. The value can be changed interactively in the server.
Some of the functions use the values of these variables.

## Loading of jni native libraries
It is possible from the configuration file to load jni libraries, that the program or the dynamically
loaded modules or hardware drivers may need. This is done with the `jni-lib` element, allowed as immediate child
of the root element, and as immediate child of the `module` and `hardware-item` elements. The `jni-lib` must have
either the `libpath` or the `library` attribute set: the first one contains the complete path to the
jni lib (including both absolute directory path and file extension)
(to be loaded with [load](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#load-java.lang.String-),
while the second one contains the bare library
name (without directory and extension), to be loaded with
[loadLibrary](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#loadLibrary-java.lang.String-).

## Dynamic loading
Modules are "dynamically loaded" as per above.
not belonging to the program proper. The `class` attribute denotes the full Java class name to the class that is to be loaded.
Of course, this must be loadable in the Java `classpath`.
As arguments, within the XML element `argument`, the arguments (type and actual value), of the constructor used, are given.

### Hardware
A number of different hardware can be deployed by the server. The hardware devices are identified
by a unique `name`, subsequently used by the server to  identify the device. The program will maintain
three variables `transmitDevice`, `captureDevice`, and `receiveDevice`,
which should contain the name of the default devices used for transmitting, capturing, and receiving.

If an empty element `<immediate-open/>` is contained in the `<hardware-item>` element,
the hardware will be initialized immediately at start; otherwise it will be initialized when needed.

### Modules
The functionality of the program is implemented in [modules](http://harctoolbox.org/Girs.html#Modules).
Modules, other than the standard ones, are declared in the configuration file. The full Java class name is given,
as well as the arguments to the constructor, as described above. Version 0.1.0 comes with two "example" extra modules,
`OpenClose` (class name `org.harctoolbox.jgirs.OpenClose`), allowing for opening and closing of hardware as desired
(otherwise it is done implicitly, when the need arise), and `Dummy` (classname `org.example.jgirs.Dummy`)
adding some truly remarkable functionality :-).

### "Named remotes": databases of remote commands
The program will read "configuration files" defining IR commands with names. Typically, these corresponds
to a consumer electronics device, and its known commands, with a name in plain English, like `play`.
Files in [Girr](http://www.harctoolbox.org/Girr.html) format, [Lircd](http://lirc.org/html/lircd.conf.html),
and CSV files, e.g. from [IRDB](https://github.com/probonopd/irdb). These are accessed through URLs,
so not only local files can be used, but also, e.g., Internet http resources. See the examples in
the supplied configuration file.

A _remote_ is here nothing but a set of commands. It is identified by its name.
A _command_ is an IR signal identified by its a name.

## Usage
As of version 0.1.0, the program is strictly single threaded. Many of the classes are implemented as
singleton classes (i.e. can be instantiated only once).

There are three ways of running the program, described next. In the future,
more program modes will likely be implemented, as well as multi-threaded operation.

### One-shot command mode
A command, including its parameters, can be given at the command line.
It will then be executed, and the result printed on standard output.
It is possible to give several commands by separating them with a semicolon (";").
Note that the latter must likely be escaped from the shell.

### Interactive (Readline) mode
If the program is started without arguments, it goes into interactive mode, reading one line of command, evalualing it,
and printing the result to standard out ("read-eval-print"). This continues until the user issues the `quit` command,
or enters the end-of-line (Normally Ctrl-D on Unix/Linux, Ctrl-Z on Msdos/Windows).
If [Java Readline](https://github.com/bengtmartensson/java-readline)
and [Readline Commander](https://github.com/bengtmartensson/ReadlineCommander)
are installed, advanced command line editing will be available.

### TCP server mode
If given the `--tcp` option, the program will start as TCP server, listening for connections on a given TCP port (default 33333).
(To try it out, just use the `telnet` program.)  This will accept a  client session on the port,
accepting a new one as soon as one has been closed.
This is compatible both with the Girs client of IrScrutinizer, and with Lirc, using the experimental version of the Girs driver.


## Commands
All commands consists of text only. The command name, and in most cases its arguments, can be abbreviated as long as the
abbreviation (as prefix) matches exactly one command. All matching of command and arguments are being done case insensitively.
Almost all commands return exactly one line. If the command succeeded, but without delivering any
useful information, the response is "`OK`". If the command fails, the response is "`ERROR`", possibly followed by some more
precise error information. If timeout is received, the response is "`.`" (a period).
Some command, like `remote`, deliver a list as answer; these are given as one line, with the list elements separated
by white spaces (), except for when they contain white space themselves. In that case, the elements will be
enclosed by double quotes (").

Commands can be abbreviated as long as the entered fragments is the prefix of exactly one command.

Next, the currently implemented commands in the standard modules are described:

### analyze
Without arguments, captures am IR signal using the current default device contained
in the variable `captureDevice`. With an argument, tries to use that as the
name of the capturing hardware.


### commands
Prints a list of the available commands.

### gettransmitters
Without argument, prints the list of transmitters available on the current default
transmitting device. With argument, returns the transmitters on that device instead,
if possible.

### license
Returns the license

### modules
Returns a list of the loaded modules

### parameter
Without arguments, all parameters and their values are listed. With one argument, the value of
that parameter is listed. With two arguments, the first one is used as the parameter name,
which is assigned the value of the second parameter. Parameter names and values can abbreviated
as long as sensible.

### protocols
Lists all the protocols known to the renderer.

### quit
Ends the session.

### receive
Without arguments, captures am IR signal using the current default device contained
in the variable `captureDevice`. With an argument, tries to use that as the
name of the capturing hardware. The variabke

###  remotes
Without arguments, lists the names of the known remotes. With one argument, lists the commands
of the remote given as argument.

### send
Synonym for `transmit raw`, for compatibility with [AGirs](https://github.com/bengtmartensson/AGirs).

### settransmitter
With one argument, sets the transmitter of the default transmitting device to the value
of the argument. With two arguments, transmitter of the the transmitting device given as the first argument
 is set to the second argument.

### transmit
This command transmits an IR signal using the current value of `transmitDevice`.
It has four subcommand, which designates how the arguments are to be interpreted.

* `transmit raw` _nosends_ _frequency_ _intro_length_ _repeat_length_ _ending_length_ _<timing_data>_

Sends a raw signal, i.e., determined by its timing data.

* `transmit hex` _nosends_ _<Signal as Pronto hex>_

Sends a signal entered as Pronto hex.

* `transmit protocolparameter` _nosends_ _protocol_ _<parameters>_

Renders and sends a signal using the given protocol and parameters. The protocols recognized
depends on the renderer used. They can be listed by the command `protocols`.

* `transmit irp` _nosends_ _irpprotocol_as_string_ _<parameters>_

Renders and sends a signal using the given protocol in IRP form.

* `transmit name` _nosends_ _remotename_ _commandname_

## version
Without arguments, returns the version of the program. Can also be given an argument, which should
correspond to the name of a connected hardware item. In that case, the version number of that
hardware item is returned.
