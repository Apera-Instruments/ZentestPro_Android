#!/usr/bin/env bash
rsync -azv --delete --exclude-from=excludelist.txt ./  192.168.101.12:/home/sync/zentest/

