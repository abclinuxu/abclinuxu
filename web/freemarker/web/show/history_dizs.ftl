<#include "../header.ftl">

<#assign byCreated = FOUND.isQualifierSet("SORT_BY_CREATED")>


<div class="ds">
   <table>
     <thead>
           <tr>
                <td class="td01">Dotaz</td>
                <td class="td02">Reakc�</td>
                <td class="td03">Posledn�</td>
            </tr>
        </thead>
        <tbody>
 <#list TOOL.analyzeDiscussions(FOUND.data) as diz>
   <tr onmouseover="javascript:style.backgroundColor='#F7F7F7'" onmouseout="javascript:style.backgroundColor='#FFFFFF'">
    <td class="td01">
     <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,60," ..")}</a>
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
<th>Po�et</th>
<th>T��dit podle</th>
<th>Sm�r</th>
<td></td>
</tr><tr>
<td><input type="text" size="4" value="${FOUND.thisPage.row}" name="from" tabindex="1"></td>
<td><input type="text" size="3" value="${FOUND.pageSize}" name="count" tabindex="2"></td>
<td>
 <select name="orderBy" tabindex="3">
  <option value="update">data posledn� odpov�di</option>
  <option value="create">data polo�en� dotazu</option>
 </select>
</td>
<td>
 <select name="orderDir" tabindex="4">
  <option value="desc">sestupn�</option>
  <option value="asc">vzestupn�</option>
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
