#!/bin/sh
git clone https://github.com/bengtmartensson/java-readline.git
cd java-readline

mvn install -Dmaven.test.skip=true
