<#include "../header.ftl">
<#include "/include/zprava.txt">

<h1>Aktuální èlánky</h1>
<#assign ARTICLES=VARS.getFreshArticles(USER?if_exists)>
<#list ARTICLES as rel>
 <@lib.showArticle rel />
 <@lib.separator double=!rel_has_next />
</#list>

<#assign NEWS=VARS.getFreshNews(user?if_exists)>
<h1>Zprávièky</h1>
<#list NEWS as rel>
 <@lib.showNews rel/>
 <@lib.separator double=!rel_has_next />
</#list>

<h1>Aktuální jádra</h1>
<p>
 <#include "/include/kernel.txt">
</p>

<#include "../footer.ftl">
