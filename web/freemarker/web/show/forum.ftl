<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <@lib.showMonitor RELATION "Zašle upozornění na váš email při novém dotazu v této poradně."/>
            </li>
            <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
                <li><a href="/forum/edit/${RELATION.id}?action=edit">Nastavení poradny</a></li>
            </#if>
        </ul>
    </div>
    <#if SUBPORTAL??>
        <#import "../macros.ftl" as lib>
        <@lib.showSubportal SUBPORTAL, true/>
    </#if>
</#assign>

<#include "../header.ftl">

<#if USER??>
    <#assign single_mode=false>
    <#if USER??>
        <#if TOOL.xpath(USER, "/data/profile/forum_mode")?default("")=="single">
            <#assign single_mode=true>
        </#if>
    </#if>
    <#if !single_mode>
        <form method="post" action="/EditUser/${USER.id}">
        <div style="float: right">
            <#assign questionCount=TOOL.getUserForums(USER).get(RELATION.id)!0>
            <#if questionCount!=0>
                <input type="image" title="Odlepit" src="/images/actions/remove.png" style="background-color:transparent">
            <#else>
                <input type="image" title="Přilepit" src="/images/actions/add.png" style="background-color:transparent">
            </#if>
            <input type="hidden" name="action" value="toggleForumHP">
            <input type="hidden" name="rid" value="${RELATION.id}">
            <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER)}">
        </div>
     </#if>
</#if>

<h1 align="center">Fórum ${CATEGORY.title}</h1>

<#if USER?? && !single_mode></form></#if>

<#if TOOL.xpath(CATEGORY,"data/note")??>
<p>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER!)}
</p>
</#if>

<#--<p>Toto diskusní fórum obsahuje celkem ${DIZS.total} diskusí.</p>-->

<@lib.showMessages/>

<form action="" method="get">
<div id="tagfilter">
    Filtr: <@lib.showHelp>Filtruje dotazy podle přiřazených štítků. Můžete použít logické operátory AND, OR a NOT a závorky.</@lib.showHelp>
    <input type="text" name="tags" id="tags" value="${PARAMS.tags!?html}" size="50"> <input type="submit" value="Filtruj">
    <div class="error">${ERRORS.tags!}</div>
    <br>
</div>
</form>

<#if (DIZS.total > 0) >

<table class="ds">
  <thead>
    <tr>
      <td class="td-nazev">Dotaz</td>
      <td class="td-meta">Stav</td>
      <td class="td-meta">Reakcí</td>
      <td class="td-datum">Poslední</td>
    </tr>
  </thead>
  <tbody>
   <#list TOOL.analyzeDiscussions(DIZS.data) as diz>
    <tr>
      <td><a href="${diz.url!("/forum/show/"+diz.relationId)}" title="${diz.title}">${TOOL.limit(diz.title,60," ..")}</a></td>
      <td class="td-meta"><@lib.showDiscussionState diz /></td>
      <td class="td-meta">${diz.responseCount}</td>
      <td class="td-datum">${DATE.show(diz.updated,"SMART")}</td>
    </tr>
   </#list>
  </tbody>
</table>

</#if>

 <ul>
  <li>
   <form action="/hledani" method="GET">
    <input type="text" name="dotaz" size="30" tabindex="1">
    <input type="submit" value="Prohledej toto fórum" tabindex="2">
    <input type="hidden" name="parent" value="${RELATION.id}">
    <input type="hidden" name="typ" value="poradna">
   </form>
  <li><a href="${URL.make("/forum/EditDiscussion?action=addQuez&amp;rid="+RELATION.id)}">Položit nový dotaz</a>
  <#if PARAMS.tags??>
    <#assign tags=PARAMS.tags.replace("\"", "%22")>
  <#else>
    <#assign tags="">
  </#if>
  <#if (DIZS.currentPage.row > 0) >
   <#assign start=DIZS.currentPage.row-DIZS.pageSize><#if (start<0)><#assign start=0></#if>
   <li><a href="${RELATION.url?default("/forum/dir/"+RELATION.id)}?from=${start}&amp;count=${DIZS.pageSize}&amp;tags=${tags}">Novější dotazy</a>
  </#if>
  <#assign start=DIZS.currentPage.row + DIZS.pageSize>
  <#if (start < DIZS.total) >
   <li><a href="${RELATION.url?default("/forum/dir/"+RELATION.id)}?from=${start}&amp;count=${DIZS.pageSize}&amp;tags=${tags}">Starší dotazy</a>
  </#if>
 </ul>

<@lib.advertisement id="arbo-sq" />

<#include "../footer.ftl">
