<#assign who=TOOL.createUser(ITEM.owner)>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj záznam">
</#if>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if PARAMS.revize?exists>
                <li>
                    <a class="bez-slovniku" href="${RELATION.url}">Návrat na aktuální verzi</a>
                </li>
            <#else>
            <#if USER?exists && TOOL.permissionsFor(USER, RELATION).canModify()>
                <li><a class="bez-slovniku" href="${URL.make("/edit/"+RELATION.id+"?action=edit")}" rel="nofollow">Upravit</a></li>
                <li><a href="${URL.make("/inset/"+RELATION.id+"?action=addScreenshot")}">Přidat fotografii</a></li>
            </#if>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li><a class="bez-slovniku" href="${RELATION.url}?varianta=print" rel="nofollow">Tisk</a></li>
                <li>
                    <a class="bez-slovniku" href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle"+TOOL.ticket(USER?if_exists, false))}">${monitorState}</a>
                    <span title="Počet lidí, kteří sledují tento záznam">(${TOOL.getMonitorCount(ITEM.data)})</span>
                    <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při úpravě záznamu.</span></a>
                </li>
                <#if USER?exists>
                    <#if USER?exists && TOOL.permissionsFor(USER, RELATION.upper).canModify()>
                        <li><a href="${URL.make("/inset/"+RELATION.id+"?action=manage")}">Správa příloh</a></li>
                    </#if>
                    <#if USER.hasRole("root")>
                        <li>
                            <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=setURL2")}">Url</a>
                        </li>
                    </#if>
                    <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
                        <li>
                            <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/slovnik")}" title="Smaž">Smazat</a>
                        </li>
                    </#if>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#import "../misc/personality.ftl" as perslib>

<@lib.showMessages/>

<div class="personality">
    <@perslib.showPersonality ITEM, true />

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
            <p>Zdroj zatím nebyl načten, je prázdný nebo obsahuje chybu.</p>
        </#if>
    </#if>
</div>

<@lib.showRelated ITEM/>

<@lib.showRevisions RELATION, REVISIONS/>

<p class="dalsi_pojmy">
    Další osobnosti:
    <#list PREV?reverse as relation>
        <a href="${relation.url}">${TOOL.childName(relation.child)}</a> -
    </#list>
    ${TOOL.childName(ITEM)} <#if (NEXT?size>0)>-</#if>
    <#list NEXT?if_exists as relation>
        <a href="${relation.url}">${TOOL.childName(relation.child)}</a>
        <#if relation_has_next> - </#if>
    </#list>
</p>

<p class="dict-abc">
    Filtr:
    <a href="/kdo-je?prefix=a">A</a>
    <a href="/kdo-je?prefix=b">B</a>
    <a href="/kdo-je?prefix=c">C</a>
    <a href="/kdo-je?prefix=d">D</a>
    <a href="/kdo-je?prefix=e">E</a>
    <a href="/kdo-je?prefix=f">F</a>
    <a href="/kdo-je?prefix=g">G</a>
    <a href="/kdo-je?prefix=h">H</a>
    <a href="/kdo-je?prefix=i">I</a>
    <a href="/kdo-je?prefix=j">J</a>
    <a href="/kdo-je?prefix=k">K</a>
    <a href="/kdo-je?prefix=l">L</a>
    <a href="/kdo-je?prefix=m">M</a>
    <a href="/kdo-je?prefix=n">N</a>
    <a href="/kdo-je?prefix=o">O</a>
    <a href="/kdo-je?prefix=p">P</a>
    <a href="/kdo-je?prefix=q">Q</a>
    <a href="/kdo-je?prefix=r">R</a>
    <a href="/kdo-je?prefix=s">S</a>
    <a href="/kdo-je?prefix=t">T</a>
    <a href="/kdo-je?prefix=u">U</a>
    <a href="/kdo-je?prefix=v">V</a>
    <a href="/kdo-je?prefix=w">W</a>
    <a href="/kdo-je?prefix=x">X</a>
    <a href="/kdo-je?prefix=y">Y</a>
    <a href="/kdo-je?prefix=z">Z</a>
</p>

<#include "../footer.ftl">
