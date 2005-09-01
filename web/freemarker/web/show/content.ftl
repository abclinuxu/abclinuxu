<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj dokument">
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
                <a href="${URL.make("/editContent/"+RELATION.id+"?action=monitor")}">${monitorState}</a>
                <span title="Poèet lidí, kteøí sledují tento dokument">(${TOOL.getMonitorCount(ITEM.data)})</span>
                <a class="info" href="#">?<span class="tooltip">Za¹le upozornìní na vá¹ email pøi úpravì dokumentu</span></a>
            </li>
            <li><a href="${URL.make("/editContent/"+RELATION.id+"?action=editPublicContent")}">Uprav dokument</a></li>
        </#if>
       </ul>
      </div>
    </#assign>
</#if>
<#include "../header.ftl">
<#if USER?exists>
    <p>
        <#if USER.hasRole("content admin")>
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=edit")}">Uprav v¹e</a> &#8226;
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=alterPublic")}">
                <#if public>Zru¹ veøejnou editovalnost<#else>Nastav veøejnou editovatelnost</#if></a> &#8226;
            <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix=/doc")}">Sma¾</a>
        </#if>
        <#if (public && USER.hasRole("derive content"))>
            &#8226; <a href="${URL.make("/editContent/"+RELATION.id+"?action=addDerivedPage")}">Vytvoø podstránku</a>
        </#if>
    </p>
</#if>

${TOOL.xpath(ITEM,"/data/content")}

<#include "../footer.ftl">
