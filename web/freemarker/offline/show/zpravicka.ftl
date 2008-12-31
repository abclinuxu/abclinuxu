<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<#assign title=TOOL.xpath(ITEM, "/data/title")?default("Zprávička")>
<h1 class="st_nadpis">${title}</h1>

<p>
    <#assign autor=TOOL.createUser(ITEM.owner)>
    ${DATE.show(ITEM.created,"CZ_FULL")}
    | ${NEWS_CATEGORIES[ITEM.subType].name}
    | <a href="http://www.abclinuxu.cz/Profile/${autor.id}">${autor.name}</a>
</p>

<p class="zpravicka">${TOOL.xpath(ITEM,"data/content")}</p>

<#if CHILDREN.discussion??>
    <#assign DISCUSSION=CHILDREN.discussion[0].child>
    <#assign diz = TOOL.createDiscussionTree(DISCUSSION,"no",true)>
    <#if (diz.threads?size>0) >
        <h2>Diskuse</h2>
        <#list diz.threads as thread>
            <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, false />
        </#list>
    </#if>
</#if>

<#include "../footer.ftl">
