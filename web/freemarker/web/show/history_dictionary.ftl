<#include "../header.ftl">

<p>Více informací o výkladovém slovníku portálu www.abclinuxu.cz
najdete na jeho <a href="/slovnik">stránce</a>.
</p>

<ul>
 <#list FOUND.data as rel>
  <li>
   <a href="/slovnik/${rel.child.subType}">${TOOL.xpath(rel.child,"data/name")}</a>
  </li>
 </#list>
</ul>

<form action="/History">
<table border="0"><tr>
<th>Pozice</th>
<th>Poèet</th>
<th>Smìr</th>
<td></td>
</tr><tr>
<td><input type="text" size="4" value="${FOUND.thisPage.row}" name="from" tabindex="1"></td>
<td><input type="text" size="3" value="${FOUND.pageSize}" name="count" tabindex="2"></td>
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
