<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.registrace?exists>
 <h1>Pøivítání</h1>

 <p> Dìkujeme vám za projevenou dùvìru. Vìøíme, ¾e budete spokojeni
 se v¹emi slu¾bami na¹eho portálu. Doporuèujeme vám projít si tuto
 stránku a nastavit si osobní údaje, pøizpùsobit si va¹i veøejnou
 osobní stránku podle svých pøedstav a nakonfigurovat tento úèet.
 Pøihla¹ovací údaje jsme vám zaslali na va¹i emailovou adresu ${PROFILE.email}. </p>
</#if>

<h1>Nastavení mého úètu</h1>

<p>Nacházíte se ve své soukromé stránce. Zde mù¾ete
mìnit nastavení svého úètu, upravovat svùj profil
èi pøihlásit se k zasílání informací. Z dùvodu va¹í
ochrany budete pøi zmìnì údajù vyzvání k zadání hesla.
Vá¹ profil, jak jej vidí ostatní náv¹tìvníci, zobrazíte
na <a href="/Profile/${PROFILE.id}">této</a> stránce.</p>

<h2>Základní údaje</h2>

<p>Na této stránce nastavíte va¹e jméno (${PROFILE.name}),
pøihla¹ovací jméno (${PROFILE.login}),
pøezdívku (${PROFILE.nick?default("není nastavena")}),
email (${PROFILE.email}) a heslo.
</p>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='no']")?exists>
 <p class="error">Administrátoøi oznaèili vá¹ email za neplatný!
 Stane se tak tehdy, pokud se nìkterý odeslaný email vrátí jako
 trvale nedoruèitelný. Dokud si nezmìníte adresu, ¾ádný dal¹í email vám
 nebude zaslán.</p>
</#if>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBasic")}">
        zmìnit základní údaje</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=changePassword")}">
        zmìnit heslo</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=forgottenPassword")}">
        poslat heslo emailem</a>
    </li>
</ul>

<h2>Profil</h2>

<p>Portál www.abclinuxu.cz vám umo¾òuje bohaté nastavení va¹í osobní stránky,
která slou¾í pro va¹i prezentaci. Mù¾ete zadat ¹irokou paletu strukturovaných
informací, napøíklad bydli¹tì, rok narození, adresu va¹ich webových stránek,
pou¾ívané distribuce, nebo jak dlouho pou¾íváte Linux. Dále si mù¾ete vytvoøit
patièku zobrazovanou v diskusích u va¹ich pøíspìvkù, nahrát svou fotku
èi upravit profil.</p>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editPersonal")}">
        zmìnit osobní údaje</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editProfile")}">
        upravit profil</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=uploadPhoto")}">
        zmìnit fotku</a>
    </li>
</ul>

<h2>Nastavení úètu</h2>

<p>V této èásti si mù¾ete zmìnit nastavení va¹eho úètu a pøizpùsobit si
portál dle svých pøedstav. Napøíklad mù¾ete zmìnit servery v rozcestníku,
vybrat barevný styl èi zakázat automatické pøihla¹ování.</p>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editSettings")}">
        zmìnit nastavení</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBlacklist")}">
        upravit seznam blokovaných u¾ivatelù</a>
    </li>
</ul>

<h2>Blog</h2>

<p>Blog je moderní formou vedení deníèku na internetu. Je urèen u¾ivatelùm Linuxu,
kteøí zde mohou psát napøíklad návody, zamy¹lení, fejetony i jiné texty. Pokud nemáte
s Linuxem nic spoleèného a jen hledáte blogovací systém, radìji si zalo¾te deníèek
nìkde jinde.</p>

<ul>
    <li>
        <#if TOOL.xpath(PROFILE, "//settings/blog")?exists>
            <#assign blog=TOOL.createCategory(TOOL.xpath(PROFILE, "//settings/blog"))>
            <a href="/blog/${blog.subType}">zobrazit blog</a>
        <#else>
            <a href="${URL.noPrefix("/blog/edit/"+PROFILE.id+"?action=addBlog")}">vytvoøit blog</a>
        </#if>
    </li>
</ul>

<h2>Zasílání informací</h2>

<p>Máte rádi ná¹ portál, ale nemáte èas nás nav¹tìvovat dennì? Nastavte
si zasílání Mìsíèního zpravodaje a Týdenní souhrn èlánkù. Dále zde mù¾ete
zapnout zasílání dotazù a komentáøù z diskusního fóra, tak¾e vám ¾ádná odpovìï
neuteèe.</p>

<ul><li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=subscribe")}">
objednat/odhlásit</a>
</ul>

<h2>Va¹e veøejná stránka</h2>

<p><a href="${URL.noPrefix("/Profile/"+PROFILE.id)}">Zpátky</a>
na svou veøejnou domovskou stránku</p>

<#include "../footer.ftl">
