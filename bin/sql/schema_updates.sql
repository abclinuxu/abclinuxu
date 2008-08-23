update polozka P, vlastnost V set numeric1=V.hodnota where P.typ=19 and V.typ_predka='P' and V.predek=P.cislo;
update polozka P, uzivatel U set P.string1=U.login where P.typ=19 and P.numeric1=U.cislo;
delete from  vlastnost where typ='user';
