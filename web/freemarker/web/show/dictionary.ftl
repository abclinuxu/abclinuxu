<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj záznam">
</#if>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a><br />
                <span class="regular-color">(${DATE.show(ITEM.updated,"CZ_FULL")})</span>
            </li>
            <#if PARAMS.revize?exists>
                <li>
                    <a class="bez-slovniku" href="${RELATION.url}">Návrat na aktuální verzi</a>
                </li>
            <#else>
                <li><a class="bez-slovniku" href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/slovnik">Historie</a></li>
                <li><a class="bez-slovniku" href="${RELATION.url}?varianta=print">Tisk</a></li>
                <li>
                    <a class="bez-slovniku" href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle")}">${monitorState}</a>
                    <span title="Poèet lidí, kteøí sledují tento záznam">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Za¹le upozornìní na vá¹ email pøi úpravì záznamu.</span></a>
                </li>
                <#if USER?exists>
                    <#if USER.hasRole("root")>
                        <li>
                            <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=setURL2")}">Url</a>
                        </li>
                    </#if>
                    <#if USER.hasRole("remove relation")>
                        <li>
                            <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/slovnik")}" title="Sma¾">Smazat</a>
                        </li>
                    </#if>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="dict-item">
    ${TOOL.render(TOOL.xpath(ITEM,"/data/description"),USER?if_exists)}
</div>

<@lib.showRelated ITEM/>

<p class="dalsi_pojmy">
    Dal¹í pojmy:
    <#list PREV?reverse as relation>
        <a href="${relation.url}">${TOOL.xpath(relation.child,"/data/name")}</a> -
    </#list>
    ${TOOL.xpath(ITEM,"/data/name")} <#if (NEXT?size>0)>-</#if>
    <#list NEXT?if_exists as relation>
        <a href="${relation.url}">${TOOL.xpath(relation.child,"/data/name")}</a>
        <#if relation_has_next> - </#if>
    </#list>
</p>

<#include "../footer.ftl">
