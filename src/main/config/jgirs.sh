#!/bin/sh

JAVA=java
READLIBDIR=/usr/local/lib64

JGIRSHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
JAR=${JGIRSHOME}/${project.name}-${project.version}-jar-with-dependencies.jar
CONFIG=${JGIRSHOME}/${project.nameLowercase}_config.xml

exec "${JAVA}" -Djava.library.path=${READLIBDIR} -jar "${JAR}" -c "${CONFIG}" "$@"
