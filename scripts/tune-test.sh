#!/usr/bin/env bash
# Mesure launcher (2 passes) sous une étiquette. Usage : tune-test.sh <étiquette>
echo "== $1"
for i in 1 2; do /lab/lenovo/scripts/launcherjank.sh; done
