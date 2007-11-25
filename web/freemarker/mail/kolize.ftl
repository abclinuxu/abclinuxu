Vazeny uzivateli ${USER.name}, ${TO?if_exists}

dovolujeme si vas informovat o dulezite zmene, ktera
se tyka vase uctu na nasem portale. Kvuli chybe v kodu
vznikly ucty, jejichz login ci prezdivka neodpovidaji
nasim pravidlum nebo jsou v kolizi s jinymi uzivateli.
Vzhledem k chystanym novym sluzbam je nutne uvest databazi
uzivatelu do konzistentniho stavu. A bohuzel se problem
tyka i vaseho uctu.

Pravidla pro login:
1) znaky jen a-z, A-Z, 0-9, pomlcka a podtrzitko
2) po prevedeni na mala pismena nesmi existovat stejny login

Pravidla pro prezdivku:
1) libovolna pismena, pomlcka, podtrzitko
2) po odstraneni diakritiky a prevodu na mala pismena nesmi
existovat stejna prezdivka

Na portalu abclinuxu jste registrovan:
id: ${USER.id}
login: ${USER.login}
prezdivka: ${USER.nick}

Ve vasem pripade doslo ke:
<#if USER.loginConflict>* kolizi loginu</#if>
<#if USER.nickConflict>* kolizi prezdivky</#if>
<#if USER.illegalLogin>* zakazanym znakum v loginu</#if>

Podrobnejsi informace, vcetne rad, co delat, najdete v clanku na adrese
http://www.abclinuxu.cz/clanky/novinky/upozorneni-pro-nase-uzivatele-loginy

<#if USER.newLogin?exists>Odhad vygenerovaneho loginu: ${USER.newLogin}</#if>
<#if USER.newNick?exists>Odhad vygenerovane prezdivky: ${USER.newNick}</#if>

Pokud dojde ke zmene vaseho loginu ci prezdivky, budete
informovani emailem.

Dekujeme za pochopeni

Admini portalu abclinuxu

