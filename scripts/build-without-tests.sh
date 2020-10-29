#!/usr/bin/env bash

mvn clean compile assembly:single -Dmaven.test.skip=true -Druntime.dependencies.scope=provided
