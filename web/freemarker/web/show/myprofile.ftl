<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.registrace?exists>
 <h1 class="st_nadpis">Pøivítání</h1>

 <p>
 Dìkujeme vám za projevenou dùvìru. Vìøíme, ¾e budete spokojeni
 se v¹emi slu¾bami na¹eho portálu. Doporuèujeme vám projít si toto
 stránku a nastavit si osobní údaje, pøizpùsobit si va¹i veøejnou
 osobní stránku ke svému obrazu a nakonfigurovat tento úèet.
 Pøihla¹ovacích údajù jsme vám zaslali na va¹i emailovou adresu ${PROFILE.email}.
 </p>
</#if>

<h1 class="st_nadpis">Va¹e soukromá stránka</h1>

<p>Nacházíte se ve své soukromé stránce. Zde mù¾ete
mìnit nastavení svého úètu, upravovat svùj profil
èi pøihlásit se k zasílání informací. Z dùvodu va¹í
ochrany budete pøi zmìnì údajù vyzvání k zadání hesla.
Vá¹ profil, jak jej vidí ostatní náv¹tìvníci, zobrazíte
na <a href="/Profile/${PROFILE.id}">této</a> stránce.
</p>

<h2>Základní údaje</h2>

<p>Mezi základní údaje patøí va¹e jméno (${PROFILE.name}),
va¹e pøihla¹ovací jméno (${PROFILE.login}),
pøezdívka (${PROFILE.nick?default("není nastavena")}),
email (${PROFILE.email}) a heslo.
</p>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='no']")?exists>
 <p class="error">Administrátoøi oznaèili vá¹ email za neplatný!
 Stane se tak tehdy, pokud se nìkterý odeslaný email vrátí jako
 trvale nedoruèitelný. Dokud si nezmìníte email, ¾ádný dal¹í vám
 nebude zaslán.</p>
</#if>

<ul>
<li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBasic")}">
zmìnit údaje</a>
<li>
<a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=forgottenPassword")}">
poslat heslo emailem</a>
<li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=changePassword")}">
zmìnit heslo</a>
</ul>

<h2>Profil</h2>

<p>Portál www.abclinuxu.cz vám umo¾òuje bohaté nastavení va¹í osobní stránky,
která slou¾í pro va¹i prezentaci. Mù¾ete zadat ¹irokou paletu strukturovaných
informací, napøíklad geografické informace o místì, kde ¾ijete, jak dlouho pracujete
s Linuxem nebo jaké pou¾íváte linuxové distribuce. Novì mù¾ete zadat patièku,
která se bude zobrazovat u ka¾dého va¹eho pøíspìvku v diskusi.
</p>

<p>Dále mù¾ete zadat rok narození, adresu va¹ich stránek nebo zmìnit svou fotku.
Poslední polo¾kou je text <i>O mnì</i>. Jak název napovídá, je urèen k tomu, abyste
zde svìtu sdìlili dal¹í informace, napøíklad záliby, koníèky èi ¾ivotní krédo.
</p>

<ul>
<li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editPersonal")}">
zmìnit osobní údaje</a>
<li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editProfile")}">
upravit profil</a>
<li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=uploadPhoto")}">
zmìnit fotku</a>
</ul>

<h2>Zasílání informací</h2>

<p>Máte rádi ná¹ portál, ale nemáte èas nás nav¹tìvovat dennì? Nastavte
si zasílání Mìsíèního zpravodaje a Týdenní souhrn èlánkù. Obì jsou zdarma!
Novinkou je emailové rozhraní k diskusnímu fóru.
</p>

<ul><li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=subscribe")}">
objednat/odhlásit</a>
</ul>

<h2>Nastavení úètu</h2>

<p>V této èásti si mù¾ete zmìnit nastavení va¹eho úètu. De facto
se jedná o personalizaci portálu, kde mù¾ete upravit jeho chování
dle svých pøedstav.</p>

<ul><li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editSettings")}">
zmìnit nastavení</a>
</ul>

<h2>Va¹e veøejná stránka</h2>

<p><a href="${URL.noPrefix("/Profile/"+PROFILE.id)}">Zpátky</a>
na svou veøejnou domovskou stránku</p>

<#include "../footer.ftl">
