#!/bin/sh

find . -type f -and -newer checkpoint | tar czf ~/source.tgz -T -
touch checkpoint
