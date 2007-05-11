<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj dokument">
</#if>
<#assign public=(ITEM.subType?if_exists=='public')>
<#if public>
    <#assign plovouci_sloupec>
      <div class="s_sekce">
       <ul>
        <li><a href="/revize?rid=${RELATION.id}">Historie</a></li>
        <#if PARAMS.revize?exists>
            <li><a href="${RELATION.url}">Návrat na aktuální verzi</a></li>
        <#else>
            <li><a href="${RELATION.url}?varianta=print">Tisk</a></li>
            <li>
                <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle"+TOOL.ticket(USER?if_exists, false))}">${monitorState}</a>
                <span title="Počet lidí, kteří sledují tento dokument">(${TOOL.getMonitorCount(ITEM.data)})</span>
                <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při úpravě dokumentu</span></a>
            </li>
            <li><a href="${URL.make("/editContent/"+RELATION.id+"?action=editPublicContent")}">Uprav dokument</a></li>
            <li><a href="${URL.make("/zmeny/"+RELATION.id)}">Hierarchie</a></li>
        </#if>
       </ul>
      </div>
    </#assign>
</#if>
<#include "../header.ftl">
<#if USER?exists>
    <p>
        <#if USER.hasRole("content admin")>
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=edit")}">Uprav vše</a> &#8226;
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=alterPublic"+TOOL.ticket(USER?if_exists, false))}">
                <#if public>Zruš<#else>Nastav</#if> veřejnou editovatelnost</a> &#8226;
            <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix=/doc")}">Smaž</a>
        </#if>
        <#if ((public && USER.hasRole("derive content")) || USER.hasRole("content admin"))>
            &#8226; <a href="${URL.make("/editContent/"+RELATION.id+"?action=addDerivedPage")}">Vytvoř podstránku</a>
        </#if>
    </p>
</#if>

${TOOL.xpath(ITEM,"/data/content")}

<#if TOC?exists>
    <div class="uceb-nav">
      <span>
        <#if TOC.left?exists>
            <a href="${TOC.left.url}" title="${TOOL.childName(TOC.left)}">&#171; Předchozí</a>
        <#else>
            &#171; Předchozí
        </#if>
        <#if TOC.up?exists>
            | <a href="${TOC.up.url}" title="${TOOL.childName(TOC.up)}">Nahoru</a> |
        <#else>
            | Nahoru |
        </#if>
        <a href="${TOC.relation.url}" title="Zobraz obsah">Obsah</a>
        <#if TOC.right?exists>
            | <a href="${TOC.right.url}" title="${TOOL.childName(TOC.right)}">Další &#187;</a>
        <#else>
            | Další &#187;
        </#if>
      </span>
    </div>
</#if>

<#include "../footer.ftl">
