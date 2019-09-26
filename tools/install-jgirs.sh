#!/bin/sh

DESTDIR=/usr/local/share/jgirs
BINDIR=/usr/local/bin

mkdir -p "${DESTDIR}"
cp target/JGirs-*-jar-with-dependencies.jar ${DESTDIR}
#if [ ! -e ${DESTDIR}/jgirs.sh ] ; then
    cp target/jgirs.sh ${DESTDIR}
#else
#    echo "NOT overwriting existing ${DESTDIR}/jgirs.sh"
#fi
ln -sf ${DESTDIR}/jgirs.sh ${BINDIR}/jgirs

if [ ! -e ${DESTDIR}/jgirs_config.xml ] ; then
    cp src/main/config/jgirs_config.xml ${DESTDIR}
else
    echo "NOT overwriting existing ${DESTDIR}/jgirs_config.xml"
fi

SYSTEM=`uname -s`
if [ ${SYSTEM} = "Darwin" ] ; then
    SYSTEM=Mac\ OS\ X
fi

if [ `uname -m` = "armv6l" ] ; then
    ARCH=arml
elif [ `uname -m` = "x86_64" -a "${SYSTEM}" != "Mac OS X" ] ; then
    ARCH=amd64
else
    ARCH=`uname -m`
fi

cp -r native/${SYSTEM}-${ARCH} ${DESTDIR}
