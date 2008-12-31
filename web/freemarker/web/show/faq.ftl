<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?? && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")??>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj otázku">
</#if>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if PARAMS.revize??>
                <li>
                    <a href="${RELATION.url}">Návrat na aktuální verzi</a>
                </li>
            <#else>
                <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
                    <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                </#if>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li><a href="${RELATION.url}?varianta=print" rel="nofollow">Tisk otázky</a></li>
                <li>
                    <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle"+TOOL.ticket(USER!, false))}">${monitorState}</a>
                    <span title="Počet lidí, kteří sledují tuto otázku">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při úpravě otázky</span></a>
                </li>
                <#if USER?? && TOOL.permissionsFor(USER, RELATION).canDelete()>
                    <li>
                        <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix=/faq")}">Smazat otázku</a>
                    </li>
                </#if>
                <#if USER?? && TOOL.permissionsFor(USER, RELATION.upper).canModify()>
                    <li>
                        <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Přesunout</a>
			            <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">Vytvořit link</a>
                    </li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1 style="margin-bottom:1em;">${ITEM.title}</h1>

<div>
    ${TOOL.render(TOOL.xpath(ITEM.data,"data/text"), USER!)}
</div>

<@lib.showRelated ITEM/>

<@lib.showRevisions RELATION, REVISIONS/>

<#include "../footer.ftl">
