alter table uzivatel COLLATE=utf8_czech_ci;
alter table uzivatel modify login CHAR(16) NOT NULL UNIQUE COLLATE utf8_czech_ci;
alter table uzivatel modify jmeno VARCHAR(35) NOT NULL COLLATE utf8_czech_ci;
alter table uzivatel modify email VARCHAR(60) NOT NULL COLLATE utf8_czech_ci;
alter table uzivatel modify heslo VARCHAR(12) NOT NULL COLLATE utf8_czech_ci;
alter table uzivatel modify prezdivka VARCHAR(20) NULL UNIQUE COLLATE utf8_czech_ci;
alter table uzivatel modify data TEXT COLLATE utf8_czech_ci;
