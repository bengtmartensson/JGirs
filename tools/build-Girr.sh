#!/bin/sh
git clone https://github.com/bengtmartensson/Girr.git
cd Girr
mvn install -Dmaven.test.skip=true

git checkout Version-2.1.0
mvn install -Dmaven.test.skip=true
