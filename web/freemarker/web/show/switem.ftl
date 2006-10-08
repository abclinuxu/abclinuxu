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
                <li><a href="${URL.noPrefix("/inset/"+RELATION.id+"?action=addScreenshot")}">P�idat obr�zek</a></li>
                <li><a href="/revize?rid=${RELATION.id}&amp;prefix=/software">Historie</a></li>
                <li><a href="${RELATION.url?default("/software/show/"+RELATION.id)}?varianta=print">Tisk</a></li>
                <li>
                    <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle")}">${monitorState}</a>
                    <span title="Po�et lid�, kte�� sleduj� tento z�znam">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Za�le upozorn�n� na v� email p�i �prav� z�znamu.</span></a>
                </li>
                <form action="/Search"><input type="text" class="text" name="query" value="${TOOL.xpath(ITEM,"/data/name")}">
                    <input type="submit" class="button" value="Hledej">
                </form>
                <#if USER?exists && USER.hasRole("attachment admin")>
                    <li><a href="${URL.noPrefix("/inset/"+RELATION.id+"?action=manage")}">Spr�va p��loh</a></li>
                </#if>
                <#if USER?exists && USER.hasRole("move relation")>
                    <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">P�esunout</a></li>
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

<@lib.showMessages/>

<div class="sw">
    <@swlib.showSoftware ITEM, true />

    <#assign feedUrl = TOOL.xpath(ITEM, "/data/url[@useType='rss']")?default("UNDEFINED")>
    <#if feedUrl!="UNDEFINED">
        <h3>
            Aktuality
            <a href="${feedUrl}" rel="nofollow"><img src="/images/site2/feed16.png" width="16" height="16" border="0" alt="RSS URL"></a>
        </h3>
        <#if FEED_LINKS?exists>
            <ul>
            <#list FEED_LINKS as link>
                <li>
                    <#if link.child.url?exists>
                        <a href="${"/presmeruj?class=P&amp;id="+ITEM.id+"&amp;url="+link.child.url?url}"
                            rel="nofollow">${link.child.text}</a>
                    <#else>
                        ${link.child.text}
                    </#if>
                    (${DATE.show(link.child.updated,"CZ_FULL")})
                </li>
            </#list>
            </ul>
        <#else>
            <p>Zdroj zat�m nebyl na�ten, je pr�zdn� nebo obsahuje chybu.</p>
        </#if>
    </#if>

    <@lib.showRelated ITEM/>
</div>

<#include "../footer.ftl">

