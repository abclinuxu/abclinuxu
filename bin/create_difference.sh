#!/bin/bash

# Yin - script used to create package with modified files

FIRST=source
SECOND=abclinuxu
OUTDIR=to_leos

TMPDIR=.temp
DFILE=$TMPDIR/diff
OFILE=$TMPDIR/only
FFILE=$TMPDIR/first
SFILE=$TMPDIR/second
CFILE=$TMPDIR/changed

mkdir $TMPDIR

diff --brief -b -r $FIRST $SECOND | grep -v '~$' > $DFILE

grep '^Only in ' $DFILE | cut -d ' ' -f 3,4 | sed -e 's/: /\//' > $OFILE

grep ^$FIRST $OFILE > $FFILE
grep ^$SECOND $OFILE > $SFILE

grep '^Files ' $DFILE | cut -d ' ' -f 2 > $CFILE
cat $CFILE >> $FFILE
sed -e 's/^'$FIRST'/'$SECOND'/' $CFILE >> $SFILE

mkdir $OUTDIR
cat $FFILE | xargs -I '%' cp -a --parents '%' $OUTDIR
cat $SFILE | xargs -I '%' cp -a --parents '%' $OUTDIR
