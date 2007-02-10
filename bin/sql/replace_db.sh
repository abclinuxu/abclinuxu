#!/bin/sh

mysql @DB_SCHEMA@ < empty_all_tables.sql

bzcat $1 | mysql @DB_SCHEMA@
