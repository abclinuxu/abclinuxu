CREATE TABLE rubrika (
 clanek INT NOT NULL,
 rubrika INT NOT NULL,
 poradi INT(2) NOT NULL
);
ALTER TABLE rubrika ADD INDEX in_clanek (clanek);
ALTER TABLE rubrika ADD INDEX in_rubrika (rubrika);

insert into rubrika select R.potomek,R.predchozi,0 from polozka P join relace R on P.cislo=R.potomek and R.typ_potomka='P'
        and R.typ_predka='K' and R.predek in (select potomek from relace where typ_predka='K' and predek=1) where typ=2;

update relace set predchozi=296555, predek=2483 where typ_predka='K' and typ_potomka='K' and potomek in (5,6,7,9,242,305,394,792,850,851,852,853,916,924,932);

update relace R, polozka P set predchozi=315, predek=1 where R.typ_potomka='P' and P.cislo=R.potomek and P.typ=2
        and R.typ_predka='K' and R.predek in (5,6,7,9,242,305,394,792,850,851,852,853,916,924,932);

update kategorie set typ=4 where cislo=1;
