<#include "/include/macros.ftl">
<#include "../header.ftl">

<@lib.showMessages/>

<#if NEWS?exists>
 <#if NEWS.currentPage.size gt 0>
  <h1>Mé zprávièky</h1>
  <ol start="${NEWS.currentPage.row+1}">
  <#list NEWS.data as rel>
   <#call showNews(rel)>
  </#list>
  </ol>
  Celkem ${NEWS.total} zprávièek.
  <#if NEWS.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;news=yes&amp;uid="+PARAMS.uid+"&amp;from="+NEWS.prevPage.row)}">
    Pøedchozích ${NEWS.pageSize}</a>.
  </#if>
  <#if NEWS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;news=yes&amp;uid="+PARAMS.uid+"&amp;from="+NEWS.nextPage.row)}">
    Následujících ${NEWS.pageSize}</a>.
  </#if>
 <#else>
  Nenapsal(a) jsem ¾ádné zprávièky.
 </#if>
</#if>

<#if DIZS?exists>
 <#if DIZS.currentPage.size gt 0>
  <h1>Mé diskuse</h1>
  <ol start="${DIZS.currentPage.row+1}">
  <#list DIZS.data as a>
   <li><a href="${URL.make("/software/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.child,"/data/title")}</a>
  </#list>
  </ol>
  Celkem ${DIZS.total} otázek.
  <#if DIZS.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;articles=yes&amp;uid="+PARAMS.uid+"&amp;from="+DIZS.prevPage.row)}">
    Pøedchozích ${DIZS.pageSize}</a>.
  </#if>
  <#if DIZS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&amp;articles=yes&amp;uid="+PARAMS.uid+"&amp;from="+DIZS.nextPage.row)}">
    Následujících ${DIZS.pageSize}</a>.
  </#if>
 <#else>
  Nepolo¾il(a) jsem ¾ádné otázky v diskusním fóru.
 </#if>
</#if>


<#include "../footer.ftl">
