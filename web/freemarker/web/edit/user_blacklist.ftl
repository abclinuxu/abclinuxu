<#include "../header.ftl">

<h1>Seznam blokovaných uživatelů</h1>

<p>
    Pokud vás některý uživatel abclinuxu vytáčí a nechcete si jeho výplody
    kazit náladu, máte možnost zařadit si jej na tento seznam blokovaných
    uživatelů. Jeho komentáře budou zobrazeny miniaturním písmem, takže
    je nejspíše přehlédnete, navíc jejich obsah a případné reakce budou
    schovány úplně. Jeho zápisky z blogu budou úplně odfiltrovány.
    <a href="${URL.noPrefix("/SelectUser/?sAction=form&amp;sParam=bUid&amp;url=/EditUser${USER.id}?action=toBlacklist")}">Přidat uživatele do seznamu</a>
</p>

<@lib.showMessages/>

<#assign blacklist=TOOL.getBlacklist(MANAGED, false)>
<#if (blacklist?size > 0)>
    <p>Na vašem seznamu jsou tito uživatelé:</p>
    <form action="${URL.noPrefix("/EditUser"+MANAGED.id+"?action=fromBlacklist")}" method="POST">
        <ul>
            <#list blacklist as who_>
                <li>
                <#if who_?is_string>
                    <input type="checkbox" name="bName" value="${who_}"> ${who_}
                <#else>
                    <#assign who = TOOL.createUser(who_)>
                    <input type="checkbox" name="bUid" value="${who.id}">
                    <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
                </#if>
                </li>
            </#list>
        </ul>
        <p>
            <input type="hidden" name="ticket" value="${USER.getSingleProperty('ticket')}">
            <input name="submit" type="submit" value="Odstranit ze seznamu">
        </p>
    </form>
<#else>
    <p>Nikoho neblokujete.</p>
</#if>


<#include "../footer.ftl">
