<#include "/include/macros.ftl">
<#include "../header.ftl">
<#include "/include/zprava.txt">
<#call showMessages>

<#global articleCount=8>
<#list TOOL.sublist(VARS.newArticles,0,articleCount) as rel>
 <#call showArticle(rel)>
 <#if rel_has_next><#call separator><#else><#call doubleSeparator></#if>
</#list>

<p>
 <b>Aktu�ln� j�dra</b><br>
 <#include "/include/kernel.txt">
</p>

<#include "../footer.ftl">
