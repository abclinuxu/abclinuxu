<#include "../header.ftl">

<@lib.showMessages/>

<#assign title=ITEM.title, autor=TOOL.createUser(ITEM.owner),
         locked = TOOL.xpath(ITEM, "//locked_by")?exists, approved = TOOL.xpath(ITEM, "//approved_by")?exists>

<@lib.advertisement id="arbo-sq" />

<h1>${title}</h1>

<p>
    <b>Autor:</b> <@lib.showUser autor/><br>
    <#if CATEGORY?exists>
        <b>Kategorie:</b> ${CATEGORY.name}<br>
    </#if>
    <b>Datum:</b> ${DATE.show(ITEM.created,"SMART")}<br>
    <#if RELATION.upper=37672>
        <b>Stav:</b> čeká na
        <#if approved>
            čas publikování
        <#else>
            schválení
        </#if>
        <#if USER?exists && USER.id=RELATION.child.owner>
        - <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
        </#if>
        <br>
    </#if>
    <#if USER?exists && USER.hasRole("news admin")>
        <#if locked>
            <#assign admin=TOOL.createUser(TOOL.xpath(ITEM, "//locked_by"))>
                Uzamknul <@lib.showUser admin/> -
                <a href="${URL.make("/edit?action=unlock&amp;rid="+RELATION.id+TOOL.ticket(USER, false))}">odemknout</a>
            <#else>
                <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
                <#if RELATION.upper=37672 && ! approved>
                    <a href="${URL.make("/edit?action=approve&amp;rid="+RELATION.id+TOOL.ticket(USER, false))}">Schválit</a>
                </#if>
                <a href="${URL.make("/edit?action=remove&amp;rid="+RELATION.id)}">Smazat</a>
                <a href="${URL.make("/edit?action=mail&amp;rid="+RELATION.id)}">Poslat email autorovi</a>
                <a href="${URL.make("/edit?action=lock&amp;rid="+RELATION.id+TOOL.ticket(USER, false))}">Zamknout</a>
        </#if>
        <br>
    </#if>
</p>

<p class="zpravicka">${TOOL.xpath(ITEM,"data/content")}</p>

<@lib.advertisement id="gg-zpravicka" />

<p><b>Nástroje</b>:
<a href="${RELATION.url?default("/zpravicky/show/"+RELATION.id)}?varianta=print" rel="nofollow">Tisk</a></p>

<h3>Komentáře</h3>
<#if CHILDREN.discussion?exists>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<#include "../footer.ftl">
