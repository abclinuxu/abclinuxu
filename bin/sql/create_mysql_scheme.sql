--CREATE DATABASE abc;

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
 data TEXT NOT NULL,                    -- XML s nazvem, ikonou, poznamkou ...
 pridal INT(6) NOT NULL,                -- odkaz na uzivatele
 vytvoreno DATETIME,       -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL             -- cas posledni zmeny
);


-- obecna struktura pro ukladani polozek
-- polozka muze byt: druh, otazka z diskuse, pozadavek,
-- hlavicka clanku
CREATE TABLE polozka (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- jednoznacny identifikator
 typ SMALLINT,                          -- typ polozky (druh, novinka, ..)
 data TEXT NOT NULL,                    -- XML s nazvem, ikonou, poznamkou ...
 pridal INT(6) NOT NULL,                -- odkaz na uzivatele
 vytvoreno DATETIME,       -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL             -- cas posledni zmeny
);
ALTER TABLE polozka ADD INDEX in_vytvoreno (vytvoreno);
ALTER TABLE polozka ADD INDEX in_typ (typ);

-- kazdy uzivatel muze pridat k polozce svuj zaznam, kazda polozka
-- ma nejmene jeden zaznam od autora polozky
-- zaznam nemusi byt jen soucasti polozky, napriklad se da pouzit
-- treba i pro odpovedi v diskusi apod.
-- pak ale musi byt ve stromu prirazen specialni kategorii
CREATE TABLE zaznam (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- jednoznacny identifikator
 typ SMALLINT,                          -- typ zaznamu (HW, SW, clanek ..)
 data LONGTEXT NOT NULL,                    -- XML s nazvem, poznamkou ...
 pridal INT(6) NOT NULL,                -- odkaz na uzivatele
 vytvoreno DATETIME,       -- cas vytvoreni
 zmeneno TIMESTAMP NOT NULL             -- cas posledni zmeny
);
ALTER TABLE zaznam ADD INDEX in_zmeneno (zmeneno);
ALTER TABLE zaznam ADD INDEX in_typ (typ);


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


-- definice ankety
CREATE TABLE anketa (
 cislo INT AUTO_INCREMENT PRIMARY KEY,  -- identifikator ankety
 typ SMALLINT,                          -- typ ankety (hodnoceni)
 otazka VARCHAR(255) NOT NULL,             -- otazka ankety
 vice CHAR(1),                          -- logicka, NULL pro FALSE,
                                        -- povoluje vice voleb
                                        -- (checkbox misto radio button)
 kdy DATETIME NOT NULL,                 -- datum vytvoreni ankety
 uzavrena CHAR(1)                       -- logicka, NULL pro FALSE
);


-- data pro anketu
CREATE TABLE data_ankety (
   cislo INT DEFAULT '0' NOT NULL,     -- index volby dane ankety
   anketa INT NOT NULL,                -- identifikator ankety
   volba VARCHAR(255) NOT NULL,        -- text volby
   pocet INT DEFAULT '0' NOT NULL      -- kolikrat bylo zvoleno
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
 data TEXT DEFAULT NULL                 -- volitelne jmeno vazby
);
ALTER TABLE relace ADD INDEX in_potomek (typ_potomka,potomek);
ALTER TABLE relace ADD INDEX in_predek (typ_predka,predek);
ALTER TABLE relace ADD INDEX in_predchozi (predchozi);

-- tabulka se ctennosti daneho objektu
CREATE TABLE citac (
 typ CHAR(1) NOT NULL,                 -- id tabulky predka
 cislo MEDIUMINT NOT NULL,             -- id predka
 soucet MEDIUMINT                      -- kolikrat byl precten
);


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
