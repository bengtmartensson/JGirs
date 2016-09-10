#!/bin/sh

DESTDIR=/usr/local/jgirs
BINDIR=/usr/local/bin

mkdir -p "${DESTDIR}"
cp target/JGirs-*-jar-with-dependencies.jar ${DESTDIR}
if [ ! -e ${DESTDIR}/jgirs.sh ] ; then
    cp src/main/config/jgirs.sh ${DESTDIR}
else
    echo "NOT overwriting existing ${DESTDIR}/jgirs.sh"
fi
ln -sf ${DESTDIR}/jgirs.sh ${BINDIR}/jgirs

if [ ! -e ${DESTDIR}/jgirs_config.xml ] ; then
    cp src/main/config/jgirs_config.xml ${DESTDIR}
else
    echo "NOT overwriting existing ${DESTDIR}/jgirs_config.xml"
fi
