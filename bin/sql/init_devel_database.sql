SET CHARACTER SET latin2;

insert into uzivatel values(1,'admin','Superu�ivatel','root@localhost','changeit',NULL,
'<data><personal><sex>man</sex></personal><system><group>11246</group></system><settings><emoticons>yes</emoticons>
<blog name="leos">591</blog></settings><roles><role>root</role></roles></data>');

insert into uzivatel values(2,'user','U�ivatel','root@localhost','changeit','Minime',
'<data><personal><sex>man</sex><birth_year>1975</birth_year><city>M�sto</city><country>St�t</country>
<signature>Pati�ky jsou nuda</signature></personal><communication><email valid="yes">
<weekly_summary>yes</weekly_summary><newsletter>yes</newsletter><forum>no</forum></email></communication>
<profile><home_page>http://www.abclinuxu.cz</home_page><about_myself format="1">J� jsem j�</about_myself>
<linux_user_from_year>1995</linux_user_from_year><distributions><distribution>Redhat</distribution>
<distribution>Mandrake</distribution><distribution>Debian</distribution></distributions></profile>
<settings><emoticons>no</emoticons></settings></data>');

-- prenest vsechny sekce
insert into devel.kategorie select * from abc.kategorie;
insert into devel.relace select * from abc.relace where typ_potomka='K';
-- prenest servery vcetne jejich odkazu
insert into devel.server (cislo,jmeno,url) select cislo,jmeno,url from abc.server;
update server set kontakt="";
insert into devel.odkaz select * from abc.odkaz;
insert into devel.relace select * from abc.relace where typ_predka='S';
-- dynamic RSS polozka
insert into polozka values(59516,0,NULL,'<data><title>Dynamicka konfigurace</title></data>',1,now(),NULL);
