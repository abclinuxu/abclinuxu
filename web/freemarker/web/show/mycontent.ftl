<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<#if HW?exists>
 <#if HW.currentPage.size gt 0>
  <h1>Mé hardwarové záznamy</h1>
  <ol start="${HW.currentPage.row+1}">
  <#list HW.data as a>
   <li><a href="${URL.make("/hardware/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.parent,"/data/name")}</a>
  </#list>
  </ol>
  Celkem ${HW.total} záznamù.
  <#if HW.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&hardware=yes&uid="+PARAMS.uid+"&from="+HW.prevPage.row)}">
    Pøedchozích ${HW.pageSize}</a>.
  </#if>
  <#if HW.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&hardware=yes&uid="+PARAMS.uid+"&from="+HW.nextPage.row)}">
    Následujících ${HW.pageSize}</a>.
  </#if>
 <#else>
  Nevytvoøil jsem ¾ádné hardwarové záznamy.
 </#if>
</#if>

<#if SW?exists>
 <#if SW.currentPage.size gt 0>
  <h1>Mé softwarové záznamy</h1>
  <ol start="${SW.currentPage.row+1}">
  <#list SW.data as a>
   <li><a href="${URL.make("/software/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.parent,"/data/name")}</a>
  </#list>
  </ol>
  Celkem ${SW.total} záznamù.
  <#if SW.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&software=yes&uid="+PARAMS.uid+"&from="+SW.prevPage.row)}">
    Pøedchozích ${SW.pageSize}</a>.
  </#if>
  <#if SW.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&software=yes&uid="+PARAMS.uid+"&from="+SW.nextPage.row)}">
    Následujících ${SW.pageSize}</a>.
  </#if>
 <#else>
  Nevytvoøil jsem ¾ádné softwarové záznamy.
 </#if>
</#if>

<#if ARTICLES?exists>
 <#if ARTICLES.currentPage.size gt 0>
  <h1>Mé èlánky</h1>
  <ol start="${ARTICLES.currentPage.row+1}">
  <#list ARTICLES.data as a>
   <li><a href="${URL.make("/clanky/ViewRelation?rid="+a.id)}">
   ${TOOL.xpath(a.child,"/data/name")}</a>
  </#list>
  </ol>
  Celkem ${ARTICLES.total} èlánkù.
  <#if ARTICLES.prevPage?exists>
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+ARTICLES.prevPage.row)}">
    Pøedchozích ${ARTICLES.pageSize}</a>.
  </#if>
  <#if ARTICLES.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+ARTICLES.nextPage.row)}">
    Následujících ${ARTICLES.pageSize}</a>.
  </#if>
 <#else>
  Nenapsal jsem ¾ádné èlánky.
 </#if>
</#if>

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
    <a href="${URL.make("/Profile?action=showContent&news=yes&uid="+PARAMS.uid+"&from="+NEWS.prevPage.row)}">
    Pøedchozích ${NEWS.pageSize}</a>.
  </#if>
  <#if NEWS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&news=yes&uid="+PARAMS.uid+"&from="+NEWS.nextPage.row)}">
    Následujících ${NEWS.pageSize}</a>.
  </#if>
 <#else>
  Nenapsal jsem ¾ádné zprávièky.
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
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+DIZS.prevPage.row)}">
    Pøedchozích ${DIZS.pageSize}</a>.
  </#if>
  <#if DIZS.nextPage?exists>
    <a href="${URL.make("/Profile?action=showContent&articles=yes&uid="+PARAMS.uid+"&from="+DIZS.nextPage.row)}">
    Následujících ${DIZS.pageSize}</a>.
  </#if>
 <#else>
  Nepolo¾il jsem ¾ádné otázky v diskusním fóru.
 </#if>
</#if>


<#include "../footer.ftl">
