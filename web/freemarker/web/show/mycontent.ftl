<#include "/include/macros.ftl">
<#include "../header.ftl">

<@lib.showMessages/>

<#if NEWS?exists>
 <#if NEWS.currentPage.size gt 0>
  <h1>M� zpr�vi�ky</h1>
  <ol start="${NEWS.currentPage.row+1}">
  <#list NEWS.data as rel>
   <#call showNews(rel)>
  </#list>
  </ol>
  Celkem ${NEWS.total} zpr�vi�ek.
  <#if NEWS.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;news=yes&amp;uid="+PARAMS.uid+"&amp;from="+NEWS.prevPage.row)}">
    P�edchoz�ch ${NEWS.pageSize}</a>.
  </#if>
  <#if NEWS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;news=yes&amp;uid="+PARAMS.uid+"&amp;from="+NEWS.nextPage.row)}">
    N�sleduj�c�ch ${NEWS.pageSize}</a>.
  </#if>
 <#else>
  Nenapsal(a) jsem ��dn� zpr�vi�ky.
 </#if>
</#if>

<#if DIZS?exists>
 <#if DIZS.currentPage.size gt 0>
  <h1>M� diskuse</h1>
  <ol start="${DIZS.currentPage.row+1}">
  <#list DIZS.data as a>
   <li><a href="${URL.make("/software/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.child,"/data/title")}</a>
  </#list>
  </ol>
  Celkem ${DIZS.total} ot�zek.
  <#if DIZS.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;articles=yes&amp;uid="+PARAMS.uid+"&amp;from="+DIZS.prevPage.row)}">
    P�edchoz�ch ${DIZS.pageSize}</a>.
  </#if>
  <#if DIZS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;articles=yes&amp;uid="+PARAMS.uid+"&amp;from="+DIZS.nextPage.row)}">
    N�sleduj�c�ch ${DIZS.pageSize}</a>.
  </#if>
 <#else>
  Nepolo�il(a) jsem ��dn� ot�zky v diskusn�m f�ru.
 </#if>
</#if>


<#include "../footer.ftl">
