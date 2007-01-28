<#include "../header.ftl">
<#--<#include "/include/zprava.txt">-->

<h1>Aktu�ln� �l�nky</h1>
<#assign ARTICLES=VARS.getFreshArticles(USER?if_exists)>
<#list ARTICLES as rel>
    <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
    <@lib.separator double=!rel_has_next />
</#list>

<#assign NEWS=VARS.getFreshNews(USER?if_exists)>
<h1>Zpr�vi�ky</h1>
<#list NEWS as rel>
    <@lib.showTemplateNews rel/>
    <@lib.separator double=!rel_has_next />
</#list>

<#include "../footer.ftl">
