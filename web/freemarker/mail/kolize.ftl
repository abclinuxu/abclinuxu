Vazeny uzivateli ${USER.name},

dovolujeme si vas informovat o dulezite zmene, ktera
se tyka vaseho uctu na portalu abclinuxu.cz. Kvuli
chybe v kodu vznikly ucty, jejichz login ci prezdivka
neodpovidaji nasim pravidlum nebo jsou v kolizi s jinymi
uzivateli. Vzhledem k chystanym novym sluzbam je nutne
uvest databazi uzivatelu do konzistentniho stavu.
A bohuzel se problem tyka i vaseho uctu.

Pravidla pro login:
1) znaky jen a-z, A-Z, 0-9, pomlcky a podtrzitka
2) po prevedeni na mala pismena nesmi existovat stejny login

Pravidla pro prezdivku:
1) libovolna pismena, pomlcky, podtrzitka
2) po odstraneni diakritiky a prevodu na mala pismena nesmi
existovat stejna prezdivka

Na portalu abclinuxu jste registrovan:
id: ${USER.id}
login: ${USER.login}
prezdivka: ${USER.nick?default("neni nastavena")}

Ve vasem pripade se jedna o:
<#if USER.loginConflict>* kolizi loginu</#if><#rt>
<#if USER.nickConflict>* kolizi prezdivky</#if><#rt>
<#if USER.illegalLogin>* zakazane znaky v loginu</#if>

Odhad vygenerovaneho loginu: ${USER.newLogin?default("beze zmeny")}
<#if USER.nick?exists>Odhad vygenerovane prezdivky: ${USER.newNick?default("beze zmeny")}</#if>

Podrobnejsi informace, vcetne rad, co delat, najdete v clanku na adrese
http://www.abclinuxu.cz/clanky/novinky/upozorneni-pro-nase-uzivatele-loginy

Pokud dojde ke zmene vaseho loginu ci prezdivky, budete
informovani emailem.

Dekujeme za pochopeni

Admini portalu abclinuxu

