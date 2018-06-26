#!/usr/bin/env bash
docker build --build-arg JAR_FILE=./target/edgent.jar -t sw-perf-edgent:1.0 .
