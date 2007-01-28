<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="P�esta� sledovat"><#else><#assign monitorState="Sleduj z�znam">
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
                    <a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}">N�vrat na aktu�ln� verzi</a>
                </li>
            <#else>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Souvisej�c� dokumenty</a></li>
                <li><a href="${URL.make("/inset/"+RELATION.id+"?action=addScreenshot")}">P�idej obr�zek</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/hardware">Historie</a></li>
                <li><a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}?varianta=print">Tisk</a></li>
                <li>
                    <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle")}">${monitorState}</a>
                    <span title="Po�et lid�, kte�� sleduj� tento z�znam">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Za�le upozorn�n� na v� email p�i �prav� z�znamu.</span></a>
                </li>
                <form action="/Search"><input type="text" class="text" name="query" value="${TOOL.xpath(ITEM,"/data/name")}">
                    <input type="submit" class="button" value="Hledej">
                </form>
                <#if USER?exists>
                    <#if USER.hasRole("move relation")>
                        <hr />
                        <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">P�esunout polo�ku</a></li>
                    </#if>
                    <#if USER.hasRole("remove relation")>
                        <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/hardware")}">Smazat</a></li>
                    </#if>
                    <#if USER.hasRole("attachment admin")>
                        <li><a href="${URL.make("/inset/"+RELATION.id+"?action=manage")}">Spr�va p��loh</a></li>
                    </#if>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<div class="hw">

<@hwlib.showHardware ITEM />

</div>

<@lib.showRelated ITEM/>

<@lib.advertisement id="arbo-sq" />

<#assign images = TOOL.screenshotsFor(ITEM)>
<#if (images?size > 0)>
    <h3>Obr�zky</h3>

    <p class="galerie">
        <#list images as image>
            <#if image.thumbnailPath?exists>
                <a href="${image.path}"><img src="${image.thumbnailPath}" alt="Obr�zek ${image_index}" border="0"></a>
            <#else>
                <img src="${image.path}" alt="Obr�zek ${image_index}">
            </#if>
        </#list>
    </p>
</#if>

<#include "../footer.ftl">

