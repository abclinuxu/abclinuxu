<#include "../header.ftl">

<@lib.showMessages/>

<#assign title=TOOL.xpath(ITEM, "/data/title")?default("Zpr�vi�ka"), autor=TOOL.createUser(ITEM.owner),
         locked = TOOL.xpath(ITEM, "//locked_by")?exists, approved = TOOL.xpath(ITEM, "//approved_by")?exists>

<@lib.advertisement id="arbo-sq" />

<h1>${title}</h1>

<p>
    <b>Autor:</b> <a href="/Profile/${autor.id}">${autor.name}</a><br>
    <#if CATEGORY?exists>
        <b>Kategorie:</b> ${CATEGORY.name}<br>
    </#if>
    <b>Datum:</b> ${DATE.show(ITEM.created,"SMART")}<br>
    <#if RELATION.upper=37672>
        <b>Stav:</b> �ek� na
        <#if approved>
            �as publikov�n�
        <#else>
            schv�len�
        </#if>
        <#if USER?exists && USER.id=RELATION.child.owner>
        - <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
        </#if>
        <br>
    </#if>
    <#if USER?exists && USER.hasRole("news admin")>
        <#if locked>
            <#assign admin=TOOL.createUser(TOOL.xpath(ITEM, "//locked_by"))>
                Uzamknul <a href="/Profile/${admin.id}">${admin.name}</a> -
                <a href="${URL.make("/edit?action=unlock&amp;rid="+RELATION.id)}">odemknout</a>
            <#else>
                <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
                <#if RELATION.upper=37672 && ! approved>
                    <a href="${URL.make("/edit?action=approve&amp;rid="+RELATION.id)}">Schv�lit</a>
                </#if>
                <a href="${URL.make("/edit?action=remove&amp;rid="+RELATION.id)}">Smazat</a>
                <a href="${URL.make("/edit?action=mail&amp;rid="+RELATION.id)}">Poslat email autorovi</a>
                <a href="${URL.make("/edit?action=lock&amp;rid="+RELATION.id)}">Zamknout</a>
        </#if>
        <br>
    </#if>
</p>

<p class="zpravicka">${TOOL.xpath(ITEM,"data/content")}</p>

<p><b>N�stroje</b>: <a href="${RELATION.url?default("/zpravicky/show/"+RELATION.id)}?varianta=print">Tisk</a></p>

<h3>Koment��e</h3>
<#if CHILDREN.discussion?exists>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo�it prvn� koment��</a>
</#if>

<#include "../footer.ftl">
