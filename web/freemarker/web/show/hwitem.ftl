<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj záznam">
</#if>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
                (${DATE.show(ITEM.updated,"CZ_FULL")})
            </li>
            <#if PARAMS.revize?exists>
                <li>
                    <a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}">Návrat na aktuální verzi</a>
                </li>
            <#else>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/hardware">Historie</a></li>
                <li><a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}?varianta=print">Tisk</a></li>
                <li>
                    <a href="${URL.make("/hardware/edit/"+RELATION.id+"?action=monitor")}">${monitorState}</a>
                    <span title="Poèet lidí, kteøí sledují tento záznam">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Za¹le upozornìní na vá¹ email pøi úpravì záznamu</span></a>
                </li>
                <li>
                    <form action="/Search"><input type="text" name="query" value="${TOOL.xpath(ITEM,"/data/name")}" size="30">
                        <input type="submit" value="Hledej">
                    </form>
                </li>
                <#if USER?exists && USER.hasRole("move relation")>
                    <li>
                        <a href="/SelectRelation/${RELATION.id}?prefix=/hardware&amp;url=/EditRelation&amp;action=move">Pøesunout</a>
                    </li>
                </#if>
                <#if USER?exists && USER.hasRole("remove relation")>
                    <li>
                        <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/hardware")}">Smazat</a>
                    </li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@hwlib.showHardware ITEM />

<#include "../footer.ftl">
