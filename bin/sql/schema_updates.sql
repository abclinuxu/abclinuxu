drop table objekt;
CREATE TABLE data (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- jednoznacny identifikator
 typ SMALLINT,                                   -- typ polozky (druh, novinka, ..)
 podtyp VARCHAR(30) NULL,                        -- podtyp
 data LONGTEXT NOT NULL,                         -- XML s cestou k soboru, nazvem, ikonou, poznamkou ...
 pridal INT(6) NOT NULL,                         -- odkaz na uzivatele
 vytvoreno DATETIME,                             -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL                      -- cas posledni zmeny
);
ALTER TABLE data ADD INDEX in_vytvoreno (vytvoreno);
ALTER TABLE data ADD INDEX in_typ (typ);
ALTER TABLE data ADD INDEX in_podtyp (podtyp);


update relace R1, relace R2 set R2.predchozi=R1.cislo where R2.typ_potomka='D' and
R2.typ_predka=R1.typ_potomka and R2.predek=R1.potomek;
