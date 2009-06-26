-- seznam monitoru daneho objektu
CREATE TABLE monitor (
 typ CHAR(1) NOT NULL,                           -- id tabulky dokumentu
 cislo INT NOT NULL,                             -- id dokumentu
 uzivatel INT NOT NULL                           -- uid uzivatele
);
ALTER TABLE monitor ADD INDEX in_citac (typ,cislo);
ALTER TABLE monitor ADD INDEX in_uzivatel (uzivatel);
