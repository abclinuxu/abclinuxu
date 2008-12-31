<#include "../header.ftl">
<#--<#include "/include/zprava.txt">-->

<h1>Aktuální články</h1>
<#assign ARTICLES=VARS.getFreshArticles(USER!)>
<#list ARTICLES as rel>
    <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
    <@lib.separator double=!rel_has_next />
</#list>

<#assign NEWS=VARS.getFreshNews(USER!)>
<h1>Zprávičky</h1>
<#list NEWS as rel>
    <@lib.showTemplateNews rel/>
    <@lib.separator double=!rel_has_next />
</#list>

<#include "../footer.ftl">
