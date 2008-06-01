#!/bin/bash

# This script generates a special "patch tree" composed of
# new versions of files, original versions and patches between
# two file trees
# Author: Lubos Dolezel
# Modified by: Karel Piwko
# History:
#	* Added support for excluding files from patch tree, motivation
#	  is to exclude configuration which differs from machine to machine

ROOTDIR="/home/kapy/devel/abclinuxu"
ORIGFILESDIR="${ROOTDIR}/portal" # original source tree
NEWFILESDIR="${ROOTDIR}/portal-devel" # modified source tree
TARGETDIR="${ROOTDIR}/patchtree" # output directory

ORIGEXT="orig" # original files will have a $ORIGEXT suffix
PATCHEXT="patch" # patch files will have a $PATCHEXT suffix

# insert space separated files which will be excluded from 
# $TARGETDIR directory
# same name of file in different directories leads to multiple
# matches, so add directory prefix if that is your case
EXCLUDE_FILES="conf_devel.properties conf_deploy.properties"

function doCopy {
	cesta=$(dirname "$1")

	# Create the directory tree
	mkdir -p "$TARGETDIR/$cesta"

	# Copy the new file
	cp "$NEWFILESDIR/$1" "$TARGETDIR/$cesta"

	# If original file exists...
	if [ -f "$ORIGFILESDIR/$1" ]; then
		# copy it
		cp "$ORIGFILESDIR/$1" "$TARGETDIR/${1}.$ORIGEXT"
		# and create a diff
		diff -u "$TARGETDIR/${1}.$ORIGEXT" "$TARGETDIR/$1" >\
		"$TARGETDIR/${1}.$PATCHEXT"
	fi
}

# construct grep expression for excluding files
exclude=""
for file in $EXCLUDE_FILES; do
	if [ -z $exclude ]; then
		exclude="\(${file}"
	else	
		exclude="${exclude}\|${file}"
	fi
done
exclude="$exclude\)"

# find modified files and exclude
diff=$(LC_ALL=C diff -qrN "$ORIGFILESDIR" "$NEWFILESDIR" |\
		awk '{print $2}' | grep -v $exclude)

for name in $diff; do
	# get a relative path
	relative="${name#$ORIGFILESDIR}"
	
	doCopy "$relative"
done
