#!/usr/bin/env bash
./build.sh
docker tag sw-perf-edgent:1.0 registry.sparkworks.net/sw-perf-edgent:1.0
docker push registry.sparkworks.net/sw-perf-edgent:1.0
