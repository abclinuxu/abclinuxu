<#import "../macros.ftl" as lib>
<#if (USER?? && TOOL.permissionsFor(USER, RELATION).canModify()) || SUBPORTAL??>
    <#assign plovouci_sloupec>
      <#if SUBPORTAL??><@lib.showSubportal SUBPORTAL, true/></#if>
      <div class="s_nadpis">Nástroje</div>
      <div class="s_sekce">
       <ul>
        <#if PARAMS.revize??>
            <li><a href="${RELATION.url}">Návrat na aktuální verzi</a></li>
        <#else>
            <li>
                <@lib.showMonitor RELATION />
            </li>
            <li><a href="${URL.make("/editContent/"+RELATION.id+"?action=editPublicContent")}" rel="nofollow">Uprav dokument</a></li>
            <li><a href="${URL.make("/zmeny/"+RELATION.id)}">Hierarchie</a></li>
        </#if>
       </ul>
      </div>
    </#assign>
</#if>
<#include "../header.ftl">
<#if USER??>
    <p>
        <#assign public=TOOL.permissionsFor(null, RELATION).canModify()>
        <#if TOOL.permissionsFor(USER, RELATION.upper).canModify()>
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=edit")}">Uprav vše</a> &#8226;
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=alterPublic"+TOOL.ticket(USER!, false))}">
                <#if public>Zruš<#else>Nastav</#if> veřejnou editovatelnost</a> &#8226;
        </#if>
        <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
            <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix=/doc")}">Smaž</a>
        </#if>
        <#if TOOL.permissionsFor(USER, RELATION).canCreate()>
            &#8226; <a href="${URL.make("/editContent/"+RELATION.id+"?action=addDerivedPage")}">Vytvoř podstránku</a>
        </#if>
    </p>
</#if>

<@lib.advertisement id="arbo-sq" />

<#assign exec=TOOL.xpath(ITEM,"/data/content/@execute")!"no", content=TOOL.xpath(ITEM,"/data/content")>
<#if exec!="yes">
${content}
<#else>
<@content?interpret />
</#if>

<#if TOC??>
    <div class="uceb-nav">
      <span>
        <#if TOC.left??>
            <a href="${TOC.left.url}" title="${TOOL.childName(TOC.left)}">&#171; Předchozí</a>
        <#else>
            &#171; Předchozí
        </#if>
        <#if TOC.up??>
            | <a href="${TOC.up.url}" title="${TOOL.childName(TOC.up)}">Nahoru</a> |
        <#else>
            | Nahoru |
        </#if>
        <a href="${TOC.relation.url}" title="Zobraz obsah">Obsah</a>
        <#if TOC.right??>
            | <a href="${TOC.right.url}" title="${TOOL.childName(TOC.right)}">Další &#187;</a>
        <#else>
            | Další &#187;
        </#if>
      </span>
    </div>
</#if>

<#if exec!="yes" || (USER?? && USER.hasRole("root"))>
    <@lib.showRevisions RELATION, REVISIONS/>
</#if>

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
