<#import "../macros.ftl" as lib>
<#assign who=TOOL.createUser(ITEM.owner)>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if PARAMS.revize??>
                <li>
                    <a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}">Návrat na aktuální verzi</a>
                </li>
            <#else>
            <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.make("/inset/"+RELATION.id+"?action=addScreenshot")}">Přidej obrázek</a></li>
            </#if>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li><a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}?varianta=print">Tisk</a></li>
                <li>
                    <@lib.showMonitor RELATION />
                </li>
                <form action="/hledani"><input type="text" class="text" name="dotaz" value="${ITEM.title}">
                    <input type="submit" class="button" value="Hledej">
                </form>
                <#if USER??>
                    <#if TOOL.permissionsFor(USER, RELATION.upper).canModify()>
                        <hr />
                        <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Přesunout položku</a></li>
                    </#if>
                    <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
                        <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/hardware")}">Smazat</a></li>
                    </#if>
                    <#if TOOL.permissionsFor(USER, RELATION.upper).canModify()>
                        <li><a href="${URL.make("/inset/"+RELATION.id+"?action=manage")}">Správa příloh</a></li>
                    </#if>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<div class="hw">

<@lib.advertisement id="gg-hw-item" />

<@hwlib.showHardware ITEM />

<@lib.showRelated ITEM/>

<#assign images = TOOL.screenshotsFor(ITEM)>
<#if (images?size > 0)>
    <h3>Obrázky</h3>

    <p class="galerie">
        <#list images as image>
            <#if image.thumbnailPath??>
                <a href="${image.path}"><img src="${image.thumbnailPath}" alt="${ITEM.title}" border="0"></a>
            <#else>
                <img src="${image.path}" alt="${ITEM.title}">
            </#if>
        </#list>
    </p>
</#if>
</div>

<@lib.showRevisions RELATION, REVISIONS/>

<@lib.advertisement id="arbo-sq" />
<@lib.advertisement id="hosting90" />

<#include "../footer.ftl">

