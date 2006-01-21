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
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=edit")}">Uprav v¹e</a> &#8226;
            <a href="${URL.make("/editContent/"+RELATION.id+"?action=addDerivedPage")}">Vytvoø podstránku</a> &#8226;
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

<#if TOC?exists>
    <table border="0" class="siroka">
        <tr>
            <td width="33%">
                <#if TOC.left?exists>
                    <a href="${TOC.left.url}" title="${TOOL.childName(TOC.left)}">Pøedchozí kapitola</a>
                </#if>
            </td>
            <td width="33%" align="center">
                <#if TOC.up?exists>
                    <a href="${TOC.up.url}" title="${TOOL.childName(TOC.up)}">Nahoru</a>
                </#if>
            </td>
            <td width="33%" align="right">
                <#if TOC.right?exists>
                    <a href="${TOC.right.url}" title="${TOOL.childName(TOC.right)}">Následující kapitola</a>
                </#if>
            </td>
        </tr>
    </table>
</#if>

<#include "../footer.ftl">
