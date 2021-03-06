<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
    <li>
        <a href="${URL.noPrefix("/sprava/redakce/autori/edit/${AUTHOR.relationId}?action=edit")}" title="Upravit autora">Upravit</a>
    </li>
    <#if EDITOR_MODE?? >
        <li>
            <a href="${URL.noPrefix("/sprava/redakce/autori/edit/${AUTHOR.relationId}?action=rm")}" title="Smazat autora">Smazat</a>
        </li>
    </#if>
    <li>
        <a href="${URL.make("/sprava/redakce/autori/honorare")}" title="Honoráře">Honoráře</a>
    </li>
    <li>
        <a href="${URL.make("/sprava/redakce/autori/clanky")}" title="Články">Články</a>
    </li>
    <li>
        <a href="${URL.make("/sprava/redakce/autori/zpravicky")}" title="Zprávičky">Zprávičky</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/sprava/redakce/namety?filterTopicsByAuthor" + AUTHOR.relationId)}" title="Náměty">Náměty</a>
    </li>
    <li>
        <a href="${URL.noPrefix("/sprava/redakce/smlouvy")}" title="Smlouvy">Smlouvy</a>
    </li>
</ul>
</@lib.showSignPost>

<h2>${(AUTHOR.title)!?html}</h2>
<table>
    <tbody>
    <tr>
        <td>Poslední článek:</td>
        <#assign date=""/>
        <#if AUTHOR.lastArticleDate??>
        <#assign date=DATE.show(AUTHOR.lastArticleDate, "CZ_DMY") />
        </#if>
        <td>${date} (celkem ${AUTHOR.articleCount!})</td>
    </tr>
    <tr>
        <td>Přezdívka:</td>
        <td>${AUTHOR.nickname!}</td>
    </tr>
    <#if AUTHOR.login?? >
        <tr>
            <td>Profil:</td>
            <td>
                <a href="${URL.noPrefix("/lide/${AUTHOR.login}")}">${AUTHOR.title!?html}</a>
            </td>
        </tr>
    </#if>
    <tr>
        <td>Aktivní:</td>
        <td>${AUTHOR.active?string("ano","ne")}</td>
    </tr>
    <tr>
        <td>Rodné číslo:</td>
        <td>${AUTHOR.birthNumber!}</td>
    </tr>
    <tr>
        <td>Číslo účtu:</td>
        <td>${AUTHOR.accountNumber!}</td>
    </tr>
    <tr>
        <td>Adresa:</td>
        <td>${AUTHOR.address!?html}</td>
    </tr>
    <tr>
        <td>Email:</td>
        <td><#if AUTHOR.email??><a href="mailto:${AUTHOR.email!?html}">${AUTHOR.email!?html}</a></#if></td>
    </tr>
    <tr>
        <td>Telefon:</td>
        <td>${AUTHOR.phone!}</td>
    </tr>
    <tr>
        <td>O autorovi:</td>
        <td>${AUTHOR.about!?html}</td>
    </tr>
    <tr>
        <td>Foto:</td>
        <td><#if AUTHOR.photoUrl?? >
            <img src="${AUTHOR.photoUrl?html}"/>
            </#if>
        </td>
    </tr>
    </tbody>
</table>
<#include "../../footer.ftl">
