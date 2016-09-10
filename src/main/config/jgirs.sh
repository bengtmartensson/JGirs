#!/bin/sh

JAVA=java
READLIBDIR=/usr/local/lib64

JGIRSHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
#JAR=${JGIRSHOME}/../../../target/JGirs-0.1.0-jar-with-dependencies.jar
JAR=${JGIRSHOME}/JGirs-0.1.0-jar-with-dependencies.jar
CONFIG=${JGIRSHOME}/jgirs_config.xml

exec "${JAVA}" -Djava.library.path=${READLIBDIR} -jar "${JAR}" -c "${CONFIG}"
