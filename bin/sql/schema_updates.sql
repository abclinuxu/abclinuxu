alter table kategorie add column numeric3 INT NULL after numeric2;
alter table kategorie add column boolean1 CHAR(1) NULL after numeric3;
alter table kategorie add column boolean2 CHAR(1) NULL after boolean1;
alter table kategorie add column boolean3 CHAR(1) NULL after boolean2;
alter table kategorie add column string3 VARCHAR(50) NULL after string2;
alter table kategorie add column date3 DATETIME NULL after date2;

alter table polozka add column numeric3 INT NULL after numeric2;
alter table polozka add column boolean1 CHAR(1) NULL after numeric3;
alter table polozka add column boolean2 CHAR(1) NULL after boolean1;
alter table polozka add column boolean3 CHAR(1) NULL after boolean2;
alter table polozka add column string3 VARCHAR(50) NULL after string2;
alter table polozka add column date3 DATETIME NULL after date2;

alter table zaznam add column numeric3 INT NULL after numeric2;
alter table zaznam add column boolean1 CHAR(1) NULL after numeric3;
alter table zaznam add column boolean2 CHAR(1) NULL after boolean1;
alter table zaznam add column boolean3 CHAR(1) NULL after boolean2;
alter table zaznam add column string3 VARCHAR(50) NULL after string2;
alter table zaznam add column date3 DATETIME NULL after date2;

alter table data add column numeric3 INT NULL after numeric2;
alter table data add column boolean1 CHAR(1) NULL after numeric3;
alter table data add column boolean2 CHAR(1) NULL after boolean1;
alter table data add column boolean3 CHAR(1) NULL after boolean2;
alter table data add column string3 VARCHAR(50) NULL after string2;
alter table data add column date3 DATETIME NULL after date2;

update polozka set boolean1='1',numeric2=null where typ=19 and (numeric2=1 or numeric2 is null);
update polozka set boolean1='0',numeric2=null where typ=19 and numeric2=0;
