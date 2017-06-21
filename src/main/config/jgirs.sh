#!/bin/sh

# Intended for Unix-like systems (like Linux and MacOsX).
# May need to be locally adapted.

# Set to the preferred Java VM, with or without directory.
#JAVA=/opt/jdk1.7.0_65/bin/java
JAVA=java

# Where the programs are installed, adjust if required
JGIRSHOME="$(dirname -- "$(readlink -f -- "${0}")" )"

# Configuration file to use
CONFIG=${JGIRSHOME}/${project.nameLowercase}_config.xml

# Extra library to search for JNI libraries.
#EXTRA_JNI_LIBS=/usr/local/lib64:
EXTRA_JNI_LIBS=

if [ `uname -m` = "armv6l" ] ; then
    ARCH=arml
elif [ `uname -m` = "x86_64" ] ; then
    ARCH=amd64
else
    ARCH=i386
fi

# Use a system supplied librxtxSerial.so if present.
# Fedora: dnf install rxtx
# Ubunto >= 16: apt-get install librxtx-java
if [ -f /usr/lib64/rxtx/librxtxSerial.so ] ; then
    LOAD_RXTX_PATH=/usr/lib64/rxtx:
fi
if [ -f /usr/lib/rxtx/librxtxSerial.so ] ; then
    LOAD_RXTX_PATH=/usr/lib/rxtx:
fi

# Use if you need /dev/ttyACM* (IrToy, many Arduino types) and your rxtx does not support it
#RXTX_SERIAL_PORTS=-Dgnu.io.rxtx.SerialPorts=/dev/ttyS0:/dev/ttyUSB0:/dev/ttyUSB1:/dev/ttyACM0:/dev/ttyACM1

if grep dialout /etc/group > /dev/null ; then
    if ! groups | grep dialout > /dev/null ; then
        needs_dialout=t
        MESSAGE="dialout"
    fi
fi

if grep lock /etc/group > /dev/null ; then
    if ! groups | grep lock > /dev/null ; then
        needs_lock=t
        MESSAGE="lock"
    fi
fi

if [ "x$needs_dialout" != "x" -a "x$needs_lock" != "x" ] ; then
    MESSAGE="dialout,lock"
fi

MESSAGEPRE="You are not a member of the group(s) "
MESSAGETAIL=", so you will probably not have access to the USB serial devices.\nYou probably want to correct this. Otherwise, functionality will be limited."

if [ "x$MESSAGE" != "x" ] ; then
    echo -e "${MESSAGEPRE}${MESSAGE}${MESSAGETAIL}" "$@"
fi

JAR=${JGIRSHOME}/${project.name}-${project.version}-jar-with-dependencies.jar

# Path to the shared libraries
JAVA_LIBRARY_PATH=${EXTRA_JNI_LIBS}${LOAD_RXTX_PATH}${JGIRSHOME}/`uname -s`-${ARCH}

exec "${JAVA}" -Djava.library.path="${JAVA_LIBRARY_PATH}" ${RXTX_SERIAL_PORTS} -jar "${JAR}" -c "${CONFIG}" "$@"
