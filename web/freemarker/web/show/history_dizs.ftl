<#include "../header.ftl">

<#assign byCreated = FOUND.isQualifierSet("SORT_BY_CREATED")>

<table width="99%" cellspacing="0" cellpadding="1" border="0">
 <tr>
  <td><b>Dotaz</b></td>
  <td align="center"><b>Odpovìdí</b></td>
  <td align="right"><b>Datum</b></td>
 </tr>
 <#list TOOL.analyzeDiscussions(FOUND.data) as diz>
  <tr>
   <td>
    <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,60," ..")}</a>
   </td>
   <td align="center">${diz.responseCount}</td>
   <td align="right">
    <#if byCreated>${DATE.show(diz.created,"CZ_FULL")}<#else>${DATE.show(diz.updated,"CZ_FULL")}</#if>
   </td>
  </tr>
  <tr><td colspan="3"><@lib.separator double=!diz_has_next /></td></tr>
 </#list>
</table>

<form action="/History">
<table border="0"><tr>
<th>Pozice</th>
<th>Poèet</th>
<th>Tøídit podle</th>
<th>Smìr</th>
<td></td>
</tr><tr>
<td><input type="text" size="4" value="${FOUND.thisPage.row}" name="from" tabindex="1" class="pole"></td>
<td><input type="text" size="3" value="${FOUND.pageSize}" name="count" tabindex="2" class="pole"></td>
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
<td><input type="submit" value="Zobrazit" class="buton"></td>
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
