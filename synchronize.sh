#!/bin/sh

find . -type f -and -newer checkpoint -not -path '*CVS*' | tar czf ~/source.tgz -T -
mv checkpoint orig.checkpoint
touch checkpoint
