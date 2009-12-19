-- CREATE DATABASE abc default character set utf8 collate utf8_czech_ci;
-- utf8_czech_ci mozna nerozlisuje se mezi normalnimi a akcentovanymi znaky

-- USE abc;

-- GRANT ALL ON *.* TO 'literakl'@'localhost' IDENTIFIED BY 'password' WITH GRANT OPTION;
-- FLUSH PRIVILEGES;


-- tabulka obsahujici definici vsech uzivatelu
CREATE TABLE uzivatel (
 cislo INT(6) AUTO_INCREMENT PRIMARY KEY,        -- jednoznacny identifikator
 login CHAR(16) NOT NULL UNIQUE,                 -- prihlasovaci jmeno
 jmeno VARCHAR(35) NOT NULL,                     -- realne jmeno uzivatele
 email VARCHAR(60) NULL,                         -- email
 openid VARCHAR(255) NULL,     	                 -- openid url
 prezdivka VARCHAR(20) NULL UNIQUE,              -- prezdivka
 sync DATETIME NULL,                             -- cas posledni synchronizace s LDAPem
 data TEXT                                       -- XML s nazvem, ikonou, poznamkou ...
);
ALTER TABLE uzivatel ADD INDEX in_nick (prezdivka);
ALTER TABLE uzivatel ADD INDEX in_openid (prezdivka);
ALTER TABLE uzivatel ADD INDEX in_sync (sync);


-- tabulka s kategoriemi
CREATE TABLE kategorie (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- jednoznacny identifikator
 typ SMALLINT,                                   -- typ kategorie
 podtyp VARCHAR(50) NULL,                        -- podtyp
 numeric1 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric2 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric3 INT NULL,                              -- obecne pouzitelny sloupecek
 boolean1 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean2 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean3 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 string1 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string2 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string3 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 date1 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date2 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date3 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 data LONGTEXT NOT NULL                          -- XML s nazvem, ikonou, poznamkou ...
);
ALTER TABLE kategorie ADD INDEX in_podtyp (podtyp);
ALTER TABLE kategorie ADD INDEX in_typ (typ);
ALTER TABLE kategorie ADD INDEX in_numeric1 (numeric1);
ALTER TABLE kategorie ADD INDEX in_numeric2 (numeric2);
ALTER TABLE kategorie ADD INDEX in_string1 (string1);
ALTER TABLE kategorie ADD INDEX in_string2 (string2);


-- obecna struktura pro ukladani polozek
-- polozka muze byt: druh, otazka z diskuse, pozadavek,
-- hlavicka clanku
CREATE TABLE polozka (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- jednoznacny identifikator
 typ SMALLINT,                                   -- typ polozky (druh, novinka, ..)
 podtyp VARCHAR(50) NULL,                        -- podtyp
 numeric1 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric2 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric3 INT NULL,                              -- obecne pouzitelny sloupecek
 boolean1 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean2 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean3 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 string1 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string2 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string3 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 date1 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date2 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date3 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 data LONGTEXT NOT NULL                          -- XML s nazvem, ikonou, poznamkou ...
);
ALTER TABLE polozka ADD INDEX in_typ (typ);
ALTER TABLE polozka ADD INDEX in_podtyp (podtyp);
ALTER TABLE polozka ADD INDEX in_numeric1 (numeric1);
ALTER TABLE polozka ADD INDEX in_numeric2 (numeric2);
ALTER TABLE polozka ADD INDEX in_string1 (string1);
ALTER TABLE polozka ADD INDEX in_string2 (string2);

-- kazdy uzivatel muze pridat k polozce svuj zaznam, kazda polozka
-- ma nejmene jeden zaznam od autora polozky
-- zaznam nemusi byt jen soucasti polozky, napriklad se da pouzit
-- treba i pro odpovedi v diskusi apod.
-- pak ale musi byt ve stromu prirazen specialni kategorii
CREATE TABLE zaznam (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- jednoznacny identifikator
 typ SMALLINT,                                   -- typ zaznamu (HW, SW, clanek ..)
 podtyp VARCHAR(50) NULL,                        -- podtyp
 numeric1 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric2 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric3 INT NULL,                              -- obecne pouzitelny sloupecek
 boolean1 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean2 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean3 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 string1 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string2 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string3 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 date1 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date2 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date3 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 data LONGTEXT NOT NULL                          -- XML s nazvem, poznamkou ...
);
ALTER TABLE zaznam ADD INDEX in_typ (typ);
ALTER TABLE zaznam ADD INDEX in_podtyp (podtyp);
ALTER TABLE zaznam ADD INDEX in_numeric1 (numeric1);
ALTER TABLE zaznam ADD INDEX in_numeric2 (numeric2);
ALTER TABLE zaznam ADD INDEX in_string1 (string1);
ALTER TABLE zaznam ADD INDEX in_string2 (string2);

-- obecna struktura pro ukladani priloh ruznych typu (logy, konfiguraky ..)
CREATE TABLE data (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- jednoznacny identifikator
 typ SMALLINT,                                   -- typ polozky (druh, novinka, ..)
 podtyp VARCHAR(50) NULL,                        -- podtyp
 numeric1 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric2 INT NULL,                              -- obecne pouzitelny sloupecek
 numeric3 INT NULL,                              -- obecne pouzitelny sloupecek
 boolean1 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean2 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 boolean3 CHAR(1) NULL,                          -- obecne pouzitelny sloupecek
 string1 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string2 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 string3 VARCHAR(50) NULL,                       -- obecne pouzitelny sloupecek
 date1 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date2 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 date3 DATETIME NULL,                            -- obecne pouzitelny sloupecek
 data LONGTEXT NOT NULL                          -- XML s cestou k soboru, nazvem, ikonou, poznamkou ...
);
ALTER TABLE data ADD INDEX in_typ (typ);
ALTER TABLE data ADD INDEX in_podtyp (podtyp);
ALTER TABLE data ADD INDEX in_numeric1 (numeric1);
ALTER TABLE data ADD INDEX in_numeric2 (numeric2);
ALTER TABLE data ADD INDEX in_string1 (string1);
ALTER TABLE data ADD INDEX in_string2 (string2);

-- tabulka obsahujici udaje spolecne vice jinym tabulkam
CREATE TABLE spolecne (
 typ CHAR(1) NOT NULL,
 cislo INT NOT NULL,
 jmeno VARCHAR(255) NULL,
 vytvoreno DATETIME,                             -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL,                     -- cas posledni zmeny
 pridal INT(6) NOT NULL,                         -- odkaz na uzivatele
 skupina INT(6) NOT NULL,                        -- cislo skupiny
 prava INT(6) NOT NULL                           -- maska prav
);
ALTER TABLE spolecne ADD UNIQUE in_predek (typ,cislo);
ALTER TABLE spolecne ADD INDEX in_jmeno (jmeno);
ALTER TABLE spolecne ADD INDEX in_vytvoreno (vytvoreno);
ALTER TABLE spolecne ADD INDEX in_zmeneno (zmeneno);
ALTER TABLE spolecne ADD INDEX in_pridal (pridal);


-- tabulka s definicemi serveru, kterym zobrazujeme odkazy
CREATE TABLE server (
  cislo INT(3) auto_increment PRIMARY KEY,       -- identifikator serveru
  jmeno VARCHAR(60) NOT NULL,                    -- zobrazovany nazev serveru
  url VARCHAR(255) NOT NULL,                     -- URL serveru
  kontakt VARCHAR(60),                           -- email na kontaktni osobu
  rss VARCHAR(255) NOT NULL                      -- URL RSS feedu
);


-- tabulka obsahujici odkazy ostatnich serveru
CREATE TABLE odkaz (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- jednoznacny identifikator
 server INT(3),                                  -- identifikator serveru
 nazev VARCHAR(80),                              -- nazev odkazu (clanku)
 url VARCHAR(255),                               -- jeho URL, kam pujde redirect
 trvaly CHAR(1),                                 -- logicka, NULL pro FALSE, urcuje, zda muze byt link nahrazen novejsim ze seznamu clanku
 pridal INT(6) NOT NULL,                         -- odkaz na uzivatele
 kdy TIMESTAMP                                   -- cas pridani
);

-- tabulka obsahujici anketu
CREATE TABLE anketa2 (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- identifikator ankety
 vice CHAR(1),                                   -- logicka, NULL pro FALSE, povoluje vice hlasu
 uzavrena CHAR(1),                               -- logicka, NULL pro FALSE
 pridal INT(6) NOT NULL,                         -- odkaz na vlastnika
 vytvoreno DATETIME NOT NULL,                    -- datum vytvoreni ankety
 hlasu SMALLINT DEFAULT 0,                       -- celkovy pocet hlasujicich
 volba1 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba2 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba3 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba4 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba5 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba6 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba7 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba8 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba9 SMALLINT DEFAULT 0,                      -- pocet hlasu pro volbu
 volba10 SMALLINT DEFAULT 0,                     -- pocet hlasu pro volbu
 volba11 SMALLINT DEFAULT 0,                     -- pocet hlasu pro volbu
 volba12 SMALLINT DEFAULT 0,                     -- pocet hlasu pro volbu
 volba13 SMALLINT DEFAULT 0,                     -- pocet hlasu pro volbu
 volba14 SMALLINT DEFAULT 0,                     -- pocet hlasu pro volbu
 volba15 SMALLINT DEFAULT 0,                     -- pocet hlasu pro volbu
 data LONGTEXT NOT NULL                          -- XML s otazkou a odpovedmi
);

-- hlavni tabulka celeho serveru, popisuje vztahy mezi objekty
--   tabulka | id tabulky
-- ----------+--------
--    anketa | 'A'
--    objekt | 'O'
--   polozka | 'P'
--    zaznam | 'Z'
-- kategorie | 'K'
--     odkaz | 'L'
--    chybne | 'E'
CREATE TABLE relace (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- identifikator vazby
 predchozi INT NOT NULL,                         -- id predchozi vazby, podobne jako .. ve fs
 typ_predka CHAR(1) NOT NULL,                    -- id tabulky predka
 predek INT NOT NULL,                            -- id predka
 typ_potomka CHAR(1) NOT NULL,                   -- id tabulky obsahu
 potomek INT NOT NULL,                           -- id obsahu
 url VARCHAR(255) DEFAULT NULL,                  -- URL stranky
 data TEXT DEFAULT NULL                          -- volitelne jmeno vazby
);
ALTER TABLE relace ADD INDEX in_potomek (typ_potomka,potomek);
ALTER TABLE relace ADD INDEX in_predek (typ_predka,predek);
ALTER TABLE relace ADD INDEX in_predchozi (predchozi);
ALTER TABLE relace ADD UNIQUE INDEX in_url (url);

-- tabulka obsahujici dalsi hodnoty pro zvoleny objekt
-- oddelene z XML aby se dalo dobre hledat uz v SQL
CREATE TABLE vlastnost (
 typ_predka CHAR(1) NOT NULL,
 predek INT NOT NULL,
 typ VARCHAR(16) NOT NULL,
 hodnota VARCHAR(255) NOT NULL
);
ALTER TABLE vlastnost ADD INDEX in_predek (typ_predka,predek);
ALTER TABLE vlastnost ADD INDEX in_typ (typ,hodnota);

-- tabulka se ctennosti daneho objektu
CREATE TABLE citac (
 typ CHAR(1) NOT NULL,                           -- id tabulky predka
 cislo MEDIUMINT NOT NULL,                       -- id predka
 soucet MEDIUMINT,                               -- kolikrat byl precten
 druh VARCHAR(15) NOT NULL DEFAULT 'read'        -- druh citace
);
ALTER TABLE citac ADD INDEX in_citac (typ,cislo);
ALTER TABLE citac ADD INDEX in_druh (druh);
ALTER TABLE citac ADD INDEX in_cislo (cislo);

-- seznam monitoru daneho objektu
CREATE TABLE monitor (
 typ CHAR(1) NOT NULL,                           -- id tabulky dokumentu
 cislo INT NOT NULL,                             -- id dokumentu
 uzivatel INT NOT NULL                           -- uid uzivatele
);
ALTER TABLE monitor ADD INDEX in_citac (typ,cislo);
ALTER TABLE monitor ADD INDEX in_uzivatel (uzivatel);

-- statistiky pouzivani sluzeb
CREATE TABLE statistika (
 den DATE NOT NULL,                              -- den
 typ CHAR(30) NOT NULL,                          -- typ
 pocet INT DEFAULT 0,                            -- pocet pouziti daneho typu dany den
 PRIMARY KEY statistika_den_typ (den, typ)
);


-- urcuje, zda je uzivatel admin
CREATE TABLE pravo (
 cislo INT(3) AUTO_INCREMENT PRIMARY KEY,        -- id tohoto radku
 admin CHAR(1)                                   -- logicka, NULL pro FALSE
);

-- seznam poslednich komentaru, ktere si uzivatel precetl
CREATE TABLE posledni_komentar (
 kdo INT(5) NOT NULL,                            -- cislo uzivatele
 diskuse INT(6) NOT NULL,                        -- cislo polozky
 posledni INT(5) NOT NULL,                       -- cislo posledniho komentare
 kdy TIMESTAMP                                   -- cas pridani
);
ALTER TABLE posledni_komentar ADD PRIMARY KEY posledni_komentar_pk (kdo,diskuse);

CREATE TABLE akce (
 kdo INT(5) NOT NULL,                            -- cislo uzivatele
 relace INT,                                     -- cislo relace
 typ VARCHAR(20),                                -- identifikator akce
 kdy TIMESTAMP                                   -- cas pridani
);
ALTER TABLE akce ADD INDEX akce_index (kdo,relace);

-- mala implementace RCS
CREATE TABLE verze (
 typ CHAR(1) NOT NULL,                           -- id tabulky dokumentu
 cislo MEDIUMINT NOT NULL,                       -- id dokumentu
 verze INT(4) NOT NULL,                          -- verze dokumentu
 kdo INT(6) NOT NULL,                            -- identifikator uzivatele
 kdy DATETIME NOT NULL,                          -- cas pridani
 obsah TEXT NULL,                                -- obsah dokumentu
 zmena TEXT NULL,                                -- diff oproti minule verzi
 popis VARCHAR(255) NULL                         -- popis teto zmeny
);
ALTER TABLE verze ADD UNIQUE INDEX in_vazba_verze (typ,cislo,verze);

CREATE TABLE komentar (
 cislo INT AUTO_INCREMENT PRIMARY KEY,           -- id tohoto radku; v podstate je zbytecny
 zaznam INT NOT NULL,                            -- id asociovaneho zaznamu
 id INT(5) NOT NULL,                             -- id komentare v ramci diskuse
 nadrazeny INT(5) NULL,                          -- id nadrazeneho komentare, NULL pokud je na nejvyssi urovni
 vytvoreno DATETIME,                             -- cas pridani
 autor INT(5) NULL,                              -- cislo autora prispevku, NULL pokud byl anonymni
 data LONGTEXT NOT NULL                          -- XML s textem komentare atd
);
ALTER TABLE komentar ADD INDEX komentar_zaznam (zaznam);
ALTER TABLE komentar ADD INDEX komentar_autor (autor);

-- tabulka nahrad URL
CREATE TABLE stara_adresa (
 puvodni VARCHAR(255) PRIMARY KEY,               -- puvodni URL, ktere jiz neni platne
 rid INT(5) NULL,                                -- id relace smerujici na tentyz objekt jako puvodni url
 nova VARCHAR(255) NULL                          -- pokud je rid NULL, pak nova adresa
);

-- seznam retezcu, ktere ctenari hledaji
CREATE TABLE hledano (
 retezec VARCHAR(255) PRIMARY KEY,               -- dotaz do fulltextoveho hledani
 pocet INT(6) NOT NULL DEFAULT 0                 -- kolikrat bylo hledano (bez kliku na dalsi stranky vysledku)
) collate utf8_bin;

-- tabulka obsahujici popisy konstant pro sloupecek typ
CREATE TABLE konstanty (
  tabulka char(1) default NULL,
  typ int(4) default NULL,
  popis varchar(255) default NULL
);

-- seznam stitku
CREATE TABLE stitek (
  id VARCHAR(30) PRIMARY KEY,                    -- ascii identifikator stitku
  titulek VARCHAR(30) NOT NULL,                  -- jmeno stitku
  vytvoreno DATETIME NOT NULL,                   -- cas vytvoreni
  nadrazeny VARCHAR(30) NULL                     -- id nadrazeneho stitku (vztah dabatabaze - mysql)
);
ALTER TABLE stitek ADD INDEX in_stitek_kdy (vytvoreno);

-- seznam prirazeni stitku dokumentum
CREATE TABLE stitkovani (
  typ CHAR(1) NOT NULL,                          -- id tabulky dokumentu
  cislo MEDIUMINT NOT NULL,                      -- id dokumentu
  stitek VARCHAR(30) NOT NULL                    -- id stitku
);
ALTER TABLE stitkovani ADD UNIQUE INDEX in_stitkovani_vazba (typ,cislo,stitek);
ALTER TABLE stitkovani ADD INDEX in_stitkovani_stitek (stitek);

-- log akci kolem stitku
CREATE TABLE stitky_log (
  akce VARCHAR(8) NOT NULL,                      -- druh akce nad stitky (add, update, delete, assign, unassign)
  kdy TIMESTAMP NOT NULL,                        -- datum akce
  stitek VARCHAR(30) NOT NULL,                   -- id stitku
  autor INT(5) NULL,                             -- cislo autora akce, NULL pokud byl anonymni
  ip CHAR(15) NULL,                              -- IP adresa autora akce
  typ CHAR(1) NULL,                              -- id tabulky dokumentu
  cislo MEDIUMINT NULL,                          -- id dokumentu
  titulek VARCHAR(30) NULL                       -- titulek stitku, jen pri akci add, edit a delete
);
ALTER TABLE stitky_log ADD INDEX in_stitky_log_stitek (stitek);
ALTER TABLE stitky_log ADD INDEX in_stitky_log_kdy (kdy);
