<#include "/include/macros.ftl">
<#include "../header.ftl">
<#include "/include/zprava.txt">
<#call showMessages>

<#list ARTICLES as rel>
 <#call showArticle(rel)>
 <#if rel_has_next><#call separator><#else><#call doubleSeparator></#if>
</#list>

<#if NEWS?exists>
 <h1>Zprávièky</h1>
 <#list NEWS as rel>
  <#call showNews(rel)>
  <#if rel_has_next><#call separator></#if>
 </#list>
</#if>

<p>
 <b>Aktuální jádra</b><br>
 <#include "/include/kernel.txt">
</p>

<#include "../footer.ftl">
