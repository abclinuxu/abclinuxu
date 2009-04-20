<#import "../macros.ftl" as lib>
<#assign who=TOOL.createUser(ITEM.owner)>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if PARAMS.revize??>
                <li>
                    <a href="${RELATION.url!("/hardware/show/"+RELATION.id)}">Návrat na aktuální verzi</a>
                </li>
            <#else>
            <#if TOOL.permissionsFor(USER, RELATION).canModify()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.make("/inset/"+RELATION.id+"?action=addScreenshot")}">Přidat obrázek</a></li>
            </#if>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li>
                    <@lib.showMonitor RELATION />
                </li>
                <form action="/hledani"><input type="text" class="text" name="dotaz" value="${ITEM.title}">
                    <input type="submit" class="button" value="Hledej">
                </form>
                <#if USER?? && TOOL.permissionsFor(USER, RELATION).canDelete()>
                    <li><a href="${URL.make("/inset/"+RELATION.id+"?action=manage")}">Správa příloh</a></li>
                    <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Přesunout</a></li>
                    <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/software")}">Smazat</a></li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#import "../misc/software.ftl" as swlib>

<@lib.showMessages/>

<div class="sw">

    <@lib.advertisement id="gg-sw-item" />

    <@swlib.showSoftware ITEM, true />

    <#assign feedUrl = TOOL.xpath(ITEM, "/data/url[@useType='rss']")!"UNDEFINED">
    <#if feedUrl!="UNDEFINED">
        <@lib.showNewsFromFeed feedUrl FEED_LINKS!"UNDEFINED" />
    </#if>

    <@lib.showRelated ITEM/>
</div>

<@lib.showRevisions RELATION, REVISIONS/>

<@lib.advertisement id="arbo-sq" />
<@lib.advertisement id="hosting90" />

<@lib.showPageTools RELATION />

<#include "../footer.ftl">

