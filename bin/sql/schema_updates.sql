insert into spolecne (typ,cislo,jmeno,vytvoreno,zmeneno,pridal) select 'P',P.cislo,'todo',P.vytvoreno,P.zmeneno,P.pridal from polozka P;
insert into spolecne (typ,cislo,jmeno,vytvoreno,zmeneno,pridal) select 'K',K.cislo,'todo',K.vytvoreno,K.zmeneno,K.pridal from kategorie K;
insert into spolecne (typ,cislo,jmeno,vytvoreno,zmeneno,pridal) select 'Z',Z.cislo,NULL,Z.vytvoreno,Z.zmeneno,Z.pridal from zaznam Z;
insert into spolecne (typ,cislo,jmeno,vytvoreno,zmeneno,pridal) select 'D',D.cislo,NULL,D.vytvoreno,D.zmeneno,D.pridal from data D;

-- TODO po uspesne migraci dropnout sloupecky zmeneno, vytvoreno
alter table kategorie drop column pridal;
alter table polozka drop column pridal;
alter table zaznam drop column pridal;
alter table data drop column pridal;
alter table kategorie drop column vytvoreno;
alter table polozka drop column vytvoreno;
alter table zaznam drop column vytvoreno;
alter table data drop column vytvoreno;
alter table kategorie drop column zmeneno;
alter table polozka drop column zmeneno;
alter table zaznam drop column zmeneno;
alter table data drop column zmeneno;
