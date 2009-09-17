<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER!,RELATION.id,true)>
<#if SUBPORTAL??>
    <#import "../macros.ftl" as lib>
    <#assign plovouci_sloupec>
        <@lib.showSubportal SUBPORTAL, true/>
    </#assign>
</#if>

<#include "../header.ftl">

<@lib.advertisement id="gg-ds-obsah" />

<@lib.advertisement id="arbo-sq" />

<@lib.showMessages/>

<div class="ds_toolbox">
    <b>Nástroje:</b>
    <#if DIZ.hasUnreadComments && DIZ.firstUnread??>
        <a href="#${DIZ.firstUnread}" title="Skočit na první nepřečtený komentář" rel="nofollow">První nepřečtený komentář</a>
    </#if>
    <@lib.showMonitor RELATION "Zašle upozornění na váš email při vložení nového komentáře."/>
    <@lib.showAdminTools RELATION DIZ.frozen />
</div>

<#if !DIZ.frozen>
    <br />
    <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}" rel="nofollow">
    Vložit další komentář</a>
<#else>
    <p class="error">
        Diskuse byla administrátory uzamčena.<br />
    </p>
</#if>

<#list DIZ.threads as thread>
   <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
</#list>

<#if (!DIZ.frozen)>
    <p>
        <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}" rel="nofollow">
        Založit nové vlákno</a> &#8226;
        <a href="#www-abclinuxu-cz">Nahoru</a>
    </p>
</#if>

<@lib.advertisement id="obsah-box" />
<@lib.advertisement id="arbo-full" />

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
