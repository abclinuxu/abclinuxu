<#include "/include/macros.ftl">
<#include "../header.ftl">
<#include "/include/zprava.txt">
<#call showMessages>

<#list ARTICLES as rel>
 <#call showArticle(rel)>
 <#if rel_has_next><#call separator><#else><#call doubleSeparator></#if>
</#list>

<#global NEWS=VARS.getFreshNews(user?if_exists)>
<h1>Zprávièky</h1>
<#list NEWS as rel>
 <#call showNews(rel)>
 <#if rel_has_next><#call separator></#if>
</#list>

<p>
 <b>Aktuální jádra</b><br>
 <#include "/include/kernel.txt">
</p>

<#include "../footer.ftl">
