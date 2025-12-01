#!/usr/bin/env bash
rsync -azv --delete 192.168.101.12:/home/sync/zentest/app/build/outputs/apk/ ./app/build/outputs/apk/
