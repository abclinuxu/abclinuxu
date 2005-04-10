#!/bin/sh

mysql abc < empty_all_tables.sql

bzcat $1 | mysql abc 
