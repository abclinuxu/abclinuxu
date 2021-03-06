<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.registrace??>
    <h1>Přivítání</h1>

    <p>
        Děkujeme vám za projevenou důvěru. Věříme, že budete spokojeni se službami našeho portálu. Doporučujeme vám projít si tuto stránku a nastavit si osobní údaje, přizpůsobit si vaši veřejnou osobní stránku podle svých představ a nakonfigurovat si účet.
        <#if PROFILE.email??>
            Přihlašovací údaje jsme vám zaslali na vaši emailovou adresu ${PROFILE.email}.
        </#if>
    </p>
</#if>

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>


<h1>Nastavení účtu</h1>

<p>Nacházíte se ve své soukromé stránce. Zde můžete měnit nastavení svého účtu, upravovat svůj profil či se přihlásit k zasílání informací. Z důvodu vaší ochrany budete při změně údajů vyzváni k zadání hesla. Váš profil, jak jej vidí ostatní návštěvníci, zobrazíte na <a href="/Profile/${PROFILE.id}">této</a> stránce.</p>

<table>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBasic")}">změnit</a>
        </td>
        <td>
            jméno, přezdívka, email
            <#if PROFILE.email?? && INVALID_EMAIL!false>
                <p class="error">
                    Administrátoři označili váš email za neplatný! Stane se tak tehdy, pokud se některý odeslaný email vrátí jako trvale nedoručitelný. Dokud si nezměníte adresu, žádný další email vám nebude zaslán.
                </p>
            </#if>
        </td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editPersonal")}">změnit</a>
        </td>
        <td>rok narození, město, kraj, země, pohlaví</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editProfile")}">změnit</a>
        </td>
        <td>domovská stránka, distribuce, doba používání Linuxu, patička, text o vás</td>
    </tr>
    <#if ! TOOL.xpath(PROFILE, "//settings/blog")??>
        <tr>
            <td align="right" valign="top">
                <a href="${URL.noPrefix("/blog/edit/"+PROFILE.id+"?action=addBlog")}">vytvořit blog</a>
            </td>
            <td>
                Blog je moderní forma vedení deníčku na Internetu. Je určen uživatelům Linuxu, kteří zde mohou psát například návody, zamyšlení, fejetony i jiné texty. Pokud nemáte s Linuxem nic společného a jen hledáte blogovací systém, raději si založte deníček někde jinde.
            </td>
        </tr>
    </#if>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=changePassword")}">změnit</a>
        </td>
        <td>heslo</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=uploadPhoto")}">změnit</a>
        </td>
        <td>fotografie</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=uploadAvatar")}">změnit</a>
        </td>
        <td>avatar</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editGPG")}">změnit</a>
        </td>
        <td>GPG klíč</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editSettings")}">změnit</a>
        </td>
        <td>
            nastavení WYSIWYG editoru, cookies, CSS, zobrazování avatarů, patičky, grafických emotikonů, záložkových služeb, rozcestníku, serverů v rozcestníku a počtu dokumentů na titulní stránce i mimo ni
        </td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/lide/"+PROFILE.login+"/zalozky")}">upravit</a>
        </td>
        <td>seznam záložek</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=editBlacklist")}">upravit</a>
        </td>
        <td>seznam blokovaných uživatelů</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/lide/"+PROFILE.login+"?action=monitors")}">zobrazit</a>
        </td>
        <td>seznam sledovaných dokumentů</td>
    </tr>
    <tr>
        <td align="right" valign="top">
            <a href="${URL.noPrefix("/EditUser/"+PROFILE.id+"?action=subscribe")}">upravit</a>
        </td>
        <td>zasílání vyžádaných informací a reklamních emailů</td>
    </tr>
</table>

<#include "../footer.ftl">
