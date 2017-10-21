#!/bin/bash

if [ ${1+1} ]; then echo "Got value ${1}"; fi

mvn install:install-file -DgroupId=com.jefferson.revised.turn -DartifactId=ttorrent-core \
	-Dversion=${1} -Dfile=ttorrent-core-${1}.jar -Dpackaging=jar -DgeneratePom=true \
	-DlocalRepositoryPath=. -DcreateChecksum=true