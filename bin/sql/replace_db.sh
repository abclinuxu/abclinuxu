#!/bin/sh

mysql abc <<EOF
delete from anketa;
delete from citac;
delete from data_ankety;
delete from kategorie;
delete from objekt;
delete from odkaz;
delete from polozka;
delete from pravo;
delete from presmerovani;
delete from relace;
delete from server;
delete from uzivatel;
delete from zaznam;
EOF

bzcat $1 | mysql abc 
