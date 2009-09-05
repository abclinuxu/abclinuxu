<#import "../macros.ftl" as lib>
<#assign who=TOOL.createUser(ITEM.owner)>
<#assign permissions = TOOL.permissionsFor(USER!, RELATION)>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if USER?? && permissions.canModify()>
                <li><a class="bez-slovniku" href="${URL.make("/edit/"+RELATION.id+"?action=edit")}" rel="nofollow">Upravit</a></li>
                <li><a href="${URL.noPrefix("/EditRelated/"+RELATION.id)}">Související dokumenty</a></li>
            </#if>
            <li>
                <@lib.showMonitor RELATION />
            </li>
            <#if USER??>
                <#if USER.hasRole("root")>
                    <li>
                        <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=setURL2")}">Url</a>
                    </li>
                </#if>
                <#if permissions.canDelete()>
                    <li>
                        <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/slovnik")}" title="Smaž">Smazat</a>
                    </li>
                </#if>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<@lib.advertisement id="arbo-sq" />

<h1>${ITEM.title}</h1>

<div class="dict-item">
    ${TOOL.render(TOOL.xpath(ITEM,"/data/description"),USER!)}
</div>

<@lib.showRelated ITEM/>

<@lib.showRevisions RELATION, REVISIONS/>

<p class="dalsi_pojmy">
    Další pojmy:
    <#list PREV?reverse as relation>
        <a href="${relation.url}">${relation.child.title}</a> -
    </#list>
    ${ITEM.title} <#if (NEXT?size>0)>-</#if>
    <#list NEXT! as relation>
        <a href="${relation.url}">${relation.child.title}</a>
        <#if relation_has_next> - </#if>
    </#list>
</p>

<p class="dict-abc">
    Filtr:
    <a href="/slovnik?prefix=a">A</a>
    <a href="/slovnik?prefix=b">B</a>
    <a href="/slovnik?prefix=c">C</a>
    <a href="/slovnik?prefix=d">D</a>
    <a href="/slovnik?prefix=e">E</a>
    <a href="/slovnik?prefix=f">F</a>
    <a href="/slovnik?prefix=g">G</a>
    <a href="/slovnik?prefix=h">H</a>
    <a href="/slovnik?prefix=i">I</a>
    <a href="/slovnik?prefix=j">J</a>
    <a href="/slovnik?prefix=k">K</a>
    <a href="/slovnik?prefix=l">L</a>
    <a href="/slovnik?prefix=m">M</a>
    <a href="/slovnik?prefix=n">N</a>
    <a href="/slovnik?prefix=o">O</a>
    <a href="/slovnik?prefix=p">P</a>
    <a href="/slovnik?prefix=q">Q</a>
    <a href="/slovnik?prefix=r">R</a>
    <a href="/slovnik?prefix=s">S</a>
    <a href="/slovnik?prefix=t">T</a>
    <a href="/slovnik?prefix=u">U</a>
    <a href="/slovnik?prefix=v">V</a>
    <a href="/slovnik?prefix=w">W</a>
    <a href="/slovnik?prefix=x">X</a>
    <a href="/slovnik?prefix=y">Y</a>
    <a href="/slovnik?prefix=z">Z</a>
</p>

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
