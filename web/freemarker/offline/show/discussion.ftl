<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER?if_exists,true)>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>

<#if is_question>
 <h1 class="st_nadpis">Ot�zka</h1>
 <@lib.showThread TOOL.createComment(ITEM), 0, ITEM.id, RELATION.id, false />
 <#if DIZ?size==0>
    <p>Na ot�zku zat�m nikdo bohu�el neodpov�d�l.</p>
 <#else>
     <p><b>Odpov�di</b></p>
 </#if>
</#if>

<#list DIZ.threads as thread>
 <@lib.showThread thread, 0, ITEM.id, RELATION.id, false />
</#list>

<#include "../footer.ftl">
