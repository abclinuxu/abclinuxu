alter table verze add column typ CHAR(1) NOT NULL default 'X' after relace;
alter table verze add column cislo MEDIUMINT NOT NULL default 0 after typ;
alter table verze drop index in_relace_verze;
update verze V, relace R set V.cislo=R.potomek, V.typ=R.typ_potomka where R.cislo=V.relace;
delete from verze where typ='X';

update verze set verze=verze+6 where typ='P' and cislo=204 and relace=5731;
select typ, cislo, verze, count(*) as cnt from verze group by typ, cislo, verze having cnt > 1;

alter table verze add unique index in_vazba_verze (typ,cislo,verze);
alter table verze drop column relace;

insert into verze values('P',21826,1,1,'2004-11-03 09:32:12','<data><name>Konference admini</name><content execute="no">&lt;h1&gt;Seznam přihlášených členů&lt;/h1&gt;

&lt;ul&gt;
&lt;li&gt;Tomáš Hála&lt;/li&gt;
&lt;li&gt;Zdeněk Burda&lt;/li&gt;
&lt;li&gt;Honza Beránek&lt;/li&gt;
&lt;li&gt;Vlasta Ott&lt;/li&gt;
&lt;li&gt;Robert Krátký&lt;/li&gt;
&lt;li&gt;Leoš Literák&lt;/li&gt;
&lt;li&gt;Honza Bartoš&lt;/li&gt;
&lt;li&gt;Pavel Szalbot&lt;/li&gt;
&lt;/ul&gt;</content></data>',NULL,NULL);
insert into verze values('P',30294,1,2121,'2005-03-29 14:36:52','<data><name>Osobní údaje</name><content execute="no">&lt;h1&gt;Ochrana osobních údajů&lt;/h1&gt;

&lt;p&gt;Veškeré osobní údaje, které uživatelé poskytnou Provozovateli (Stickfish, s.r.o.) prostřednictvím internetové adresy http://www.abclinuxu.cz (dále jen "Osobní údaje"), budou považovány za důvěrné (to se netýká údajů, které uživatel vyplní do svého veřejně přístupného uživatelského profilu).&lt;/p&gt;

&lt;p&gt;Účel, k němuž budou Osobní údaje zpracovány, vyplývá z nebo je uveden na příslušné stránce, která umožňuje vložení takových údajů. Provozovatel zaručuje, že neposkytne Osobní údaje jiné osobě. Provozovatel dále zaručuje, že s Osobními údaji bude nakládat v souladu se zákonem č. 101/2000 Sb. o ochraně osobních údajů.&lt;/p&gt;

&lt;p&gt;Osoba, která poskytla Osobní údaje, souhlasí s tím, aby Provozovatel všechny poskytnuté Osobní
údaje zpracovával pro účely uvedené výše, nebo aby takové údaje nechal zpracovat jinou osobou.
Osoba, která poskytla Osobní údaje, má právo požádat Provozovatele, aby její Osobní údaje vyloučil
ze zpracovávání. Taková žádost musí být písemná a musí být doručena na adresu:&lt;/p&gt;

&lt;p&gt;Stickfish, s.r.o.&lt;br&gt;
Řehořova 1039/54&lt;br&gt;
130 00 Praha 3&lt;/p&gt;</content></data>',NULL,NULL);
insert into verze values('P',30296,1,2121,'2005-03-29 14:41:00','<data><name>Podmínky použití</name><content execute="no">&lt;h1&gt;Podmínky použití&lt;/h1&gt;

&lt;p&gt;Pokud není uvedeno jinak, autorská práva k textům, grafice nebo jiným materiálům chráněným
autorským právem, které jsou umístěny na internetové adrese http://www.abclinuxu.cz, svědčí
provozovateli příslušných stránek, jímž je Stickfish,&amp;nbsp;s.r.o. (dále jen "Provozovatel").&lt;/p&gt;

&lt;p&gt;Autorská práva k textům přísluší jejich autorům. Mezi texty se řadí komentáře, zprávičky, hardwarové a softwarové záznamy, pojmy ve slovníku, ovladače a další služby portálu. Publikováním dává autor provozovateli portálu časově a geograficky neomezenou neexkluzivní licenci k šíření libovolnými kanály (například www, email, wap, PDF, tisk a další). K tomuto šíření smí provozovatel použít i externí firmy.&lt;/p&gt;

&lt;p&gt;Veškeré ochranné známky nebo jiná označení třetích osob nebo výrobků či služeb třetích osob, které jsou zmíněny na těchto stránkách, přísluší jejich právoplatným držitelům a jsou použity výlučně k identifikaci příslušných osob nebo jejich výrobků či služeb.&lt;/p&gt;

&lt;h1&gt;Omezení odpovědnosti&lt;/h1&gt;

&lt;p&gt;Provozovatel nezaručuje, že informace publikované na internetové adrese http://www.abclinuxu.cz
jsou pravdivé, správné, úplné a aktuální. Z toho důvodu provozovatel neodpovídá za škodu nebo jinou újmu, která by uživateli uvedených stránek mohla vzniknout v důsledku užití takových informací. Provozovatel dále nenese žádnou odpovědnost za obsah internetových stránek třetích subjektů, na které jeho stránky odkazují.&lt;/p&gt;

&lt;p&gt;Provozovatel neodpovídá za informace nebo obsah materiálů, které na jeho stránky umístí třetí osoby, a to bez ohledu na skutečnost, zda se tak stane s jeho vědomím nebo nikoliv. Provozovatel si však vyhrazuje právo odstranit z internetových stránek jakékoliv informace nebo materiály, které na jeho stránky umístí třetí osoby, a to bez udání důvodu.&lt;/p&gt;</content></data>',
NULL,NULL);
