<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<#if HW?exists>
 <#if HW.currentPage.size gt 0>
  <h1>M� hardwarov� z�znamy</h1>
  <ol start="${HW.currentPage.row+1}">
  <#list HW.data as a>
   <li><a href="${URL.make("/hardware/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.parent,"/data/name")}</a>
  </#list>
  </ol>
  Celkem ${HW.total} z�znam�.
  <#if HW.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&hardware=yes&uid="+PARAMS.uid+"&from="+HW.prevPage.row)}">
    P�edchoz�ch ${HW.pageSize}</a>.
  </#if>
  <#if HW.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&hardware=yes&uid="+PARAMS.uid+"&from="+HW.nextPage.row)}">
    N�sleduj�c�ch ${HW.pageSize}</a>.
  </#if>
 <#else>
  Nevytvo�il jsem ��dn� hardwarov� z�znamy.
 </#if>
</#if>

<#if SW?exists>
 <#if SW.currentPage.size gt 0>
  <h1>M� softwarov� z�znamy</h1>
  <ol start="${SW.currentPage.row+1}">
  <#list SW.data as a>
   <li><a href="${URL.make("/software/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.parent,"/data/name")}</a>
  </#list>
  </ol>
  Celkem ${SW.total} z�znam�.
  <#if SW.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&software=yes&uid="+PARAMS.uid+"&from="+SW.prevPage.row)}">
    P�edchoz�ch ${SW.pageSize}</a>.
  </#if>
  <#if SW.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&software=yes&uid="+PARAMS.uid+"&from="+SW.nextPage.row)}">
    N�sleduj�c�ch ${SW.pageSize}</a>.
  </#if>
 <#else>
  Nevytvo�il jsem ��dn� softwarov� z�znamy.
 </#if>
</#if>

<#if ARTICLES?exists>
 <#if ARTICLES.currentPage.size gt 0>
  <h1>M� �l�nky</h1>
  <ol start="${ARTICLES.currentPage.row+1}">
  <#list ARTICLES.data as a>
   <li><a href="${URL.make("/clanky/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.child,"/data/name")}</a>
  </#list>
  </ol>
  Celkem ${ARTICLES.total} �l�nk�.
  <#if ARTICLES.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+ARTICLES.prevPage.row)}">
    P�edchoz�ch ${ARTICLES.pageSize}</a>.
  </#if>
  <#if ARTICLES.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+ARTICLES.nextPage.row)}">
    N�sleduj�c�ch ${ARTICLES.pageSize}</a>.
  </#if>
 <#else>
  Nenapsal jsem ��dn� �l�nky.
 </#if>
</#if>

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
    <a href="${URL.make("/Profile?action=showContent&news=yes&uid="+PARAMS.uid+"&from="+NEWS.prevPage.row)}">
    P�edchoz�ch ${NEWS.pageSize}</a>.
  </#if>
  <#if NEWS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&news=yes&uid="+PARAMS.uid+"&from="+NEWS.nextPage.row)}">
    N�sleduj�c�ch ${NEWS.pageSize}</a>.
  </#if>
 <#else>
  Nenapsal jsem ��dn� zpr�vi�ky.
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
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+DIZS.prevPage.row)}">
    P�edchoz�ch ${DIZS.pageSize}</a>.
  </#if>
  <#if DIZS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+DIZS.nextPage.row)}">
    N�sleduj�c�ch ${DIZS.pageSize}</a>.
  </#if>
 <#else>
  Nepolo�il jsem ��dn� ot�zky v diskusn�m f�ru.
 </#if>
</#if>


<#include "../footer.ftl">
