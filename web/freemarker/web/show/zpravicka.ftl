<#include "../header.ftl">

<@lib.showMessages/>

<#assign title=ITEM.title, autor=TOOL.createUser(ITEM.owner),
         locked = TOOL.xpath(ITEM, "//locked_by")??,
         approved = TOOL.xpath(ITEM, "//approved_by")??,
         forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")!"UNDEF">

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>


<h1>${title}</h1>

<p class="zpravicka">${TOOL.xpath(ITEM,"data/content")}</p>

<p class="meta-vypis">
    ${DATE.show(ITEM.created,"SMART")} |
    <@lib.showUser autor/> |
    <#if CATEGORY??>
        <b>Kategorie:</b> ${CATEGORY.name} |
    </#if>
    <#if RELATION.upper=37672>
        <br /><b>Stav:</b> čeká na
        <#if approved>
            čas publikování
        <#else>
            schválení
        </#if>
        <#if USER?? && USER.id=RELATION.child.owner>
            - <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
        </#if>
    </#if>
</p>
<p>
    <#if USER?? && USER.hasRole("news admin")>
        <#assign shortened=TOOL.xpath(ITEM,"data/perex")?default("UNDEFINED")>
        <#if shortened != "UNDEFINED" && RELATION.upper=37672>
            <div style="padding-left: 30pt"><strong>Perex:</strong>${shortened}</div>
        </#if>
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
    </#if>
</p>

<br />
<@lib.advertisement id="gg-zpravicka" />

<@lib.showPageTools RELATION />

<#if CHILDREN.discussion??>
    <h3>Komentáře</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#elseif forbidDiscussion!="yes">
    <h3>Komentáře</h3>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<br />
<@lib.advertisement id="fullbanner" />

<#include "../footer.ftl">
