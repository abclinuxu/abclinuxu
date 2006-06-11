<#assign who=TOOL.createUser(ITEM.owner)>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if PARAMS.revize?exists>
                <li>
                    <a href="${RELATION.url?default("/hardware/show/"+RELATION.id)}">Návrat na aktuální verzi</a>
                </li>
            <#else>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=edit")}">Upravit</a></li>
                <li><a href="${URL.noPrefix("/inset/"+RELATION.id+"?action=addScreenshot")}">Pøidat obrázek</a></li>
                <li><a href="${RELATION.url?default("/software/show/"+RELATION.id)}?varianta=print">Tisk</a></li>
                <li>
                    <form action="/Search"><input type="text" class="text" name="query" value="${TOOL.xpath(ITEM,"/data/name")}" size="30">
                        <input type="submit" class="button" value="Hledej">
                    </form>
                </li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/software">Historie</a></li>
                <li>
                    <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
                    (${DATE.show(ITEM.updated,"CZ_FULL")})
                </li>
                <#if USER?exists && USER.hasRole("move relation")>
                    <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Pøesunout</a></li>
                </#if>
                <#if USER?exists && USER.hasRole("remove relation")>
                    <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/software")}">Smazat</a></li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#import "../misc/software.ftl" as swlib>

<@swlib.showSoftware ITEM, true />

<#if FEED_LINKS?exists>
    <h3>
        Aktuality
        <#assign feedUrl = TOOL.xpath(ITEM, "/data/url[@useType='rss']")?default("UNDEFINED")>
        <#if feedUrl!="UNDEFINED">
            <a href="${feedUrl}"><img src="/images/site2/feed16.png" width="16" height="16" border="0" alt="RSS URL"></a>
        </#if>
    </h3>
    <ul>
    <#list FEED_LINKS as link>
        <li>
            <a href="${link.child.url}">${link.child.text}</a>
            (${DATE.show(link.child.updated,"CZ_FULL")})
        </li>
    </#list>
    </ul>
</#if>

<#include "../footer.ftl">

