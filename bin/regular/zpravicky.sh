#!/bin/sh
mysql abc -e "select P.vytvoreno, U.jmeno, P.data from polozka P, uzivatel U where P.pridal=U.cislo and P.typ=7 and vytvoreno>'2004-01-01 00:00' and vytvoreno<'2004-02-01 00:01' order by vytvoreno asc" > ~/zpravicky.txt

