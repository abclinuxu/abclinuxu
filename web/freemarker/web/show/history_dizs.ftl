<#include "../header.ftl">

<#assign byCreated = FOUND.isQualifierSet("SORT_BY_CREATED")>


<div class="ds">
   <table>
     <thead>
           <tr>
                <td class="td01">Titulek</td>
                <td class="td02">Reakcí</td>
                <td class="td03"><#if byCreated>Vytvoøeno<#else>Poslední</#if></td>
            </tr>
        </thead>
        <tbody>
 <#list TOOL.analyzeDiscussions(FOUND.data) as diz>
   <tr>
    <td class="td01">
     <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,100," ..")}</a>
     <#if TOOL.xpath(diz.discussion,"/data/frozen")?exists>
         <img src="/images/site2/zamceno.gif" alt="Z" title="Diskuse byla administrátory uzamèena">
     </#if>
     <#if TOOL.isQuestionSolved(diz.discussion.data)>
         <img src="/images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle ètenáøù vyøe¹ena">
     </#if>
     <#if USER?exists && TOOL.xpath(diz.discussion,"//monitor/id[text()='"+USER.id+"'")?exists>
         <img src="/images/site2/sledovano.gif" alt="S" title="Tuto diskusi sledujete monitorem">
     </#if>
    </td>
    <td class="td02"><span class="pidi">${diz.responseCount}</span></td>
    <td class="td03"><span class="pidi">
      <#if byCreated>${DATE.show(diz.created,"CZ_FULL")}<#else>${DATE.show(diz.updated,"CZ_FULL")}</#if>
    </td>
   </tr>
        </#list>
        </tbody>
  </table>
</div>


<form action="/History">
<table border="0"><tr>
<th>Pozice</th>
<th>Poèet</th>
<th>Tøídit podle</th>
<th>Smìr</th>
<td></td>
</tr><tr>
<td><input type="text" size="4" value="${FOUND.thisPage.row}" name="from" tabindex="1"></td>
<td><input type="text" size="3" value="${FOUND.pageSize}" name="count" tabindex="2"></td>
<td>
 <select name="orderBy" tabindex="3">
  <option value="update">data poslední odpovìdi</option>
  <option value="create">data polo¾ení dotazu</option>
 </select>
</td>
<td>
 <select name="orderDir" tabindex="4">
  <option value="desc">sestupnì</option>
  <option value="asc">vzestupnì</option>
 </select>
</td>
<td><input type="submit" value="Zobrazit"></td>
</tr></table>
<input type="hidden" name="type" value="${PARAMS.type}">
<#if PARAMS.uid?exists><input type="hidden" name="uid" value="${PARAMS.uid}"></#if>
</form>

<#if FOUND.prevPage?exists>
 <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
 <a href="${URL_BEFORE_FROM}${FOUND.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>0 &lt;&lt;
</#if>
${FOUND.thisPage.row}-${FOUND.thisPage.row+FOUND.thisPage.size}
<#if FOUND.nextPage?exists>
 <a href="${URL_BEFORE_FROM}${FOUND.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
 <a href="${URL_BEFORE_FROM}${(FOUND.total - FOUND.pageSize)?string["#"]}${URL_AFTER_FROM}">${FOUND.total}</a>
<#else>&gt;&gt; ${FOUND.total}
</#if>

<#include "../footer.ftl">
