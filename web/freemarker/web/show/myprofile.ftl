<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.registrace?exists>
 <h1>Přivítání</h1>

 <p> Děkujeme vám za projevenou důvěru. Věříme, že budete spokojeni
 se všemi službami našeho portálu. Doporučujeme vám projít si tuto
 stránku a nastavit si osobní údaje, přizpůsobit si vaši veřejnou
 osobní stránku podle svých představ a nakonfigurovat tento účet.
 Přihlašovací údaje jsme vám zaslali na vaši emailovou adresu ${PROFILE.email}. </p>
</#if>

<h1>Nastavení mého účtu</h1>

<p>Nacházíte se ve své soukromé stránce. Zde můžete
měnit nastavení svého účtu, upravovat svůj profil
či přihlásit se k zasílání informací. Z důvodu vaší
ochrany budete při změně údajů vyzvání k zadání hesla.
Váš profil, jak jej vidí ostatní návštěvníci, zobrazíte
na <a href="/Profile/${PROFILE.id}">této</a> stránce.</p>

<h2>Základní údaje</h2>

<p>Na této stránce nastavíte vaše jméno (${PROFILE.name}),
přihlašovací jméno (${PROFILE.login}),
přezdívku (${PROFILE.nick?default("není nastavena")}),
email (${PROFILE.email}) a heslo.
</p>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='no']")?exists>
 <p class="error">Administrátoři označili váš email za neplatný!
 Stane se tak tehdy, pokud se některý odeslaný email vrátí jako
 trvale nedoručitelný. Dokud si nezměníte adresu, žádný další email vám
 nebude zaslán.</p>
</#if>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBasic")}">
        změnit základní údaje</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=changePassword")}">
        změnit heslo</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=forgottenPassword")}">
        poslat heslo emailem</a>
    </li>
</ul>

<h2>Profil</h2>

<p>Portál www.abclinuxu.cz vám umožňuje bohaté nastavení vaší osobní stránky,
která slouží pro vaši prezentaci. Můžete zadat širokou paletu strukturovaných
informací, například bydliště, rok narození, adresu vašich webových stránek,
používané distribuce, nebo jak dlouho používáte Linux. Dále si můžete vytvořit
patičku zobrazovanou v diskusích u vašich příspěvků, nahrát svou fotku
či upravit profil.</p>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editPersonal")}">
        změnit osobní údaje</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editProfile")}">
        upravit profil</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=uploadPhoto")}">
        změnit fotku</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=uploadAvatar")}">
        změnit avatar</a>
    </li>
</ul>

<h2>Nastavení účtu</h2>

<p>V této části si můžete změnit nastavení vašeho účtu a přizpůsobit si
portál dle svých představ. Například můžete změnit servery v rozcestníku,
vybrat barevný styl či zakázat automatické přihlašování.</p>

<ul>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editSettings")}">
        změnit nastavení</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBlacklist")}">
        upravit seznam blokovaných uživatelů</a>
    </li>
</ul>

<h2>Blog</h2>

<p>Blog je moderní formou vedení deníčku na internetu. Je určen uživatelům Linuxu,
kteří zde mohou psát například návody, zamyšlení, fejetony i jiné texty. Pokud nemáte
s Linuxem nic společného a jen hledáte blogovací systém, raději si založte deníček
někde jinde.</p>

<ul>
    <li>
        <#if TOOL.xpath(PROFILE, "//settings/blog")?exists>
            <#assign blog=TOOL.createCategory(TOOL.xpath(PROFILE, "//settings/blog"))>
            <a href="/blog/${blog.subType}">zobrazit blog</a>
        <#else>
            <a href="${URL.noPrefix("/blog/edit/"+PROFILE.id+"?action=addBlog")}">vytvořit blog</a>
        </#if>
    </li>
</ul>

<h2>Zasílání informací</h2>

<p>Máte rádi náš portál, ale nemáte čas nás navštěvovat denně? Nastavte
si zasílání Měsíčního zpravodaje a Týdenní souhrn článků. Dále zde můžete
zapnout zasílání dotazů a komentářů z diskusního fóra, takže vám žádná odpověď
neuteče.</p>

<ul><li>
<a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=subscribe")}">
objednat/odhlásit</a>
</ul>

<h2>Vaše veřejná stránka</h2>

<p><a href="${URL.noPrefix("/Profile/"+PROFILE.id)}">Zpátky</a>
na svou veřejnou domovskou stránku</p>

<#include "../footer.ftl">
