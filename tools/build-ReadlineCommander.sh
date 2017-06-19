#!/bin/sh
git clone https://github.com/bengtmartensson/ReadlineCommander.git
cd ReadlineCommander

mvn install -Dmaven.test.skip=true
