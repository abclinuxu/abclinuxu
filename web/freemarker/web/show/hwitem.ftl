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
                    <a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}">Návrat na aktuální verzi</a>
                </li>
            <#else>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/hardware">Historie</a></li>
                <li><a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}?varianta=print">Tisk</a></li>
                <li>
                    <a href="${URL.make("/monitor/"+RELATION.id+"?action=toggle")}">${monitorState}</a>
                    <span title="Poèet lidí, kteøí sledují tento záznam">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Za¹le upozornìní na vá¹ email pøi úpravì záznamu.</span></a>
                </li>
                <form action="/Search"><input type="text" class="text" name="query" value="${TOOL.xpath(ITEM,"/data/name")}">
                    <input type="submit" class="button" value="Hledej">
                </form>
                <#if USER?exists && USER.hasRole("move relation")>
                    <hr />
                    <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Pøesunout polo¾ku</a></li>
                </#if>
                <#if USER?exists && USER.hasRole("remove relation")>
                    <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/hardware")}">Smazat</a></li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<div class="hw"

<@hwlib.showHardware ITEM />

</div>

<@lib.showRelated ITEM/>

<#include "../footer.ftl">

