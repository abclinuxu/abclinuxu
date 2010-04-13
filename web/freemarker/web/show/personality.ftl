<#import "../macros.ftl" as lib>
<#assign who=TOOL.createUser(ITEM.owner)>
<#assign plovouci_sloupec>

   <@lib.advertisement id="hypertext2nahore" />

    <div class="s_sekce">
        <ul>
            <#if PARAMS.revize??>
                <li>
                    <a class="bez-slovniku" href="${RELATION.url}">Návrat na aktuální verzi</a>
                </li>
            <#else>
            <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
                <li><a class="bez-slovniku" href="${URL.make("/edit/"+RELATION.id+"?action=edit")}" rel="nofollow">Upravit</a></li>
                <li><a href="${URL.make("/inset/"+RELATION.id+"?action=addScreenshot")}">Přidat fotografii</a></li>
            </#if>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
                <li>
                    <@lib.showMonitor RELATION />
                </li>
                <#if USER??>
                    <#if USER?? && TOOL.permissionsFor(USER, RELATION.upper).canModify()>
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

    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">
<#import "../misc/personality.ftl" as perslib>

<@lib.advertisement id="square" />

<@lib.showMessages/>

<div class="personality">
    <@perslib.showPersonality ITEM, true />

    <#assign feedUrl = TOOL.xpath(ITEM, "/data/url[@useType='rss']")!"UNDEFINED">
    <#if feedUrl!="UNDEFINED">
        <@lib.showNewsFromFeed feedUrl FEED_LINKS!"UNDEFINED" />
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
    <#list NEXT! as relation>
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

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
