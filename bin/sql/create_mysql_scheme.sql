--CREATE DATABASE abc default character set latin2 collate latin2_bin; -- tenhle fungoval pro devel databazi
--CREATE DATABASE abc default character set utf8 collate utf8_bin;
-- collate utf8_czech_ci nefunguje, nerozlisuje se mezi normalnimi a akcentovanymi znaky

--USE abc;

--GRANT ALL ON *.* TO 'literakl'@'localhost' IDENTIFIED BY 'password' WITH GRANT OPTION;
--FLUSH PRIVILEGES;


-- tabulka obsahujici definici vsech uzivatelu
CREATE TABLE uzivatel (
 cislo INT(6) AUTO_INCREMENT PRIMARY KEY,   -- jednoznacny identifikator
 login CHAR(16) NOT NULL UNIQUE,            -- prihlasovaci jmeno
 jmeno VARCHAR(35) NOT NULL,                -- realne jmeno uzivatele
 email VARCHAR(60) NOT NULL,                -- email
 heslo VARCHAR(12) NOT NULL,     	        -- nekryptovane heslo
 prezdivka VARCHAR(20) NULL UNIQUE,         -- prezdivka
 data TEXT                                  -- XML s nazvem, ikonou, poznamkou ...
);
ALTER TABLE uzivatel ADD INDEX in_nick (prezdivka);


-- tabulka s kategoriemi
CREATE TABLE kategorie (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- jednoznacny identifikator
 typ SMALLINT,                          -- typ kategorie
 podtyp VARCHAR(30) NULL,               -- podtyp
 data TEXT NOT NULL,                    -- XML s nazvem, ikonou, poznamkou ...
 pridal INT(6) NOT NULL,                -- odkaz na uzivatele
 vytvoreno DATETIME,       -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL             -- cas posledni zmeny
);
ALTER TABLE kategorie ADD INDEX in_podtyp (podtyp);


-- obecna struktura pro ukladani polozek
-- polozka muze byt: druh, otazka z diskuse, pozadavek,
-- hlavicka clanku
CREATE TABLE polozka (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- jednoznacny identifikator
 typ SMALLINT,                          -- typ polozky (druh, novinka, ..)
 podtyp VARCHAR(30) NULL,               -- podtyp
 data TEXT NOT NULL,                    -- XML s nazvem, ikonou, poznamkou ...
 pridal INT(6) NOT NULL,                -- odkaz na uzivatele
 vytvoreno DATETIME,       -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL             -- cas posledni zmeny
);
ALTER TABLE polozka ADD INDEX in_vytvoreno (vytvoreno);
ALTER TABLE polozka ADD INDEX in_typ (typ);
ALTER TABLE polozka ADD INDEX in_podtyp (podtyp);

-- kazdy uzivatel muze pridat k polozce svuj zaznam, kazda polozka
-- ma nejmene jeden zaznam od autora polozky
-- zaznam nemusi byt jen soucasti polozky, napriklad se da pouzit
-- treba i pro odpovedi v diskusi apod.
-- pak ale musi byt ve stromu prirazen specialni kategorii
CREATE TABLE zaznam (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- jednoznacny identifikator
 typ SMALLINT,                          -- typ zaznamu (HW, SW, clanek ..)
 podtyp VARCHAR(30) NULL,               -- podtyp
 data LONGTEXT NOT NULL,                    -- XML s nazvem, poznamkou ...
 pridal INT(6) NOT NULL,                -- odkaz na uzivatele
 vytvoreno DATETIME,       -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL             -- cas posledni zmeny
);
ALTER TABLE zaznam ADD INDEX in_zmeneno (zmeneno);
ALTER TABLE zaznam ADD INDEX in_typ (typ);
ALTER TABLE zaznam ADD INDEX in_podtyp (podtyp);


-- tabulka s definicemi serveru, kterym zobrazujeme odkazy
CREATE TABLE server (
  cislo INT(3) PRIMARY KEY,            -- identifikator serveru
  jmeno VARCHAR(60) NOT NULL,          -- zobrazovany nazev serveru
  url VARCHAR(255) NOT NULL,           -- URL serveru
  kontakt VARCHAR(60)                  -- email na kontaktni osobu
);


-- tabulka obsahujici odkazy ostatnich serveru
CREATE TABLE odkaz (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- jednoznacny identifikator
 server INT(3),                         -- identifikator serveru
 nazev VARCHAR(80),                     -- nazev odkazu (clanku)
 url VARCHAR(255),                      -- jeho URL, kam pujde redirect
 trvaly CHAR(1),                        -- logicka, NULL pro FALSE,
                                        -- urcuje, zda muze byt link
					                    -- nahrazen novejsim ze seznamu clanku
 pridal INT(6) NOT NULL,                -- odkaz na uzivatele
 kdy TIMESTAMP                          -- cas pridani
);


-- tabulka obsahujici objekty ( obrazky, zvuky, video )
-- pokud format obsahuje "URL", data obsahuje URL na externi objekt
CREATE TABLE objekt (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- identifikator objektu
 format VARCHAR(30) NOT NULL,           -- mime-type nebo "URL"
 data BLOB,                             -- URL nebo binarni data objektu
 vlastnik INT NOT NULL                  -- majitel objektu
);

-- tabulka obsahujici anketu
CREATE TABLE anketa2 (
 cislo INT AUTO_INCREMENT PRIMARY KEY,   -- identifikator ankety
 vice CHAR(1),                           -- logicka, NULL pro FALSE, povoluje vice hlasu
 uzavrena CHAR(1),                       -- logicka, NULL pro FALSE
 pridal INT(6) NOT NULL,                 -- odkaz na vlastnika
 vytvoreno DATETIME NOT NULL,            -- datum vytvoreni ankety
 hlasu SMALLINT DEFAULT 0,               -- celkovy pocet hlasujicich
 volba1 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba2 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba3 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba4 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba5 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba6 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba7 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba8 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba9 SMALLINT DEFAULT 0,              -- pocet hlasu pro volbu
 volba10 SMALLINT DEFAULT 0,             -- pocet hlasu pro volbu
 data LONGTEXT NOT NULL                  -- XML s otazkou a odpovedmi
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
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- identifikator vazby
 predchozi INT NOT NULL,                -- id predchozi vazby, podobne jako .. ve fs
 typ_predka CHAR(1) NOT NULL,           -- id tabulky predka
 predek INT NOT NULL,                   -- id predka
 typ_potomka CHAR(1) NOT NULL,          -- id tabulky obsahu
 potomek INT NOT NULL,                  -- id obsahu
 url VARCHAR(255) DEFAULT NULL,         -- URL stranky
 data TEXT DEFAULT NULL                 -- volitelne jmeno vazby
);
ALTER TABLE relace ADD INDEX in_potomek (typ_potomka,potomek);
ALTER TABLE relace ADD INDEX in_predek (typ_predka,predek);
ALTER TABLE relace ADD INDEX in_predchozi (predchozi);
ALTER TABLE relace ADD INDEX in_url (url);

-- tabulka se ctennosti daneho objektu
CREATE TABLE citac (
 typ CHAR(1) NOT NULL,                 -- id tabulky predka
 cislo MEDIUMINT NOT NULL,             -- id predka
 soucet MEDIUMINT                      -- kolikrat byl precten
);
ALTER TABLE citac ADD INDEX in_citac (typ,cislo);

-- statistiky pouzivani odkazu
CREATE TABLE presmerovani (
 den DATE NOT NULL,                     -- den statistiky
 server INT(3) NOT NULL,                -- identifikator serveru
 soucet SMALLINT DEFAULT 0,             -- pocet presmerovani daneho dne pro
                                        -- urcity server
 PRIMARY KEY a (den,server)
);


-- urcuje, zda je uzivatel admin
CREATE TABLE pravo (
 cislo INT(3) AUTO_INCREMENT PRIMARY KEY,  -- id tohoto radku
 admin CHAR(1)                             -- logicka, NULL pro FALSE
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
 cesta VARCHAR(255) NOT NULL,                    -- identifikator dokumentu
 verze VARCHAR(25) NOT NULL,                     -- verze dokumentu
 kdo VARCHAR(50) NOT NULL,                       -- identifikator uzivatele
 kdy DATETIME NOT NULL,                          -- cas pridani
 obsah TEXT NOT NULL                             -- obsah dokumentu
);

ALTER TABLE verze ADD UNIQUE INDEX verze_cesta_verze (cesta,verze);

CREATE TABLE komentar (
 cislo INT AUTO_INCREMENT PRIMARY KEY,     -- id tohoto radku; v podstate je zbytecny
 zaznam INT NOT NULL,                      -- id asociovaneho zaznamu
 id INT(5) NOT NULL,                       -- id komentare v ramci diskuse
 nadrazeny INT(5) NULL,                    -- id nadrazeneho komentare, NULL pokud je na nejvyssi urovni
 vytvoreno DATETIME,                       -- cas pridani
 autor INT(5) NULL,                        -- cislo autora prispevku, NULL pokud byl anonymni
 data LONGTEXT NOT NULL                    -- XML s textem komentare atd
);

ALTER TABLE komentar ADD INDEX komentar_zaznam (zaznam);
ALTER TABLE komentar ADD INDEX komentar_autor (autor);
