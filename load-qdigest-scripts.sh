#!/bin/sh

for i in "./qd_*.lua" ; do
  h=`cat $i | redis-cli -x SCRIPT LOAD`
  echo "$i: $h"
done
