<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER?if_exists,true)>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>

<#if is_question>
 <h1 class="st_nadpis">Otázka</h1>
 <@lib.showThread TOOL.createComment(ITEM), 0, ITEM.id, RELATION.id, false />
 <#if DIZ?size==0>
    <p>Na otázku zatím nikdo bohužel neodpověděl.</p>
 <#else>
     <p><b>Odpovědi</b></p>
 </#if>
</#if>

<#list DIZ.threads as thread>
 <@lib.showThread thread, 0, ITEM.id, RELATION.id, false />
</#list>

<#include "../footer.ftl">
