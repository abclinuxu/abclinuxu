<#include "../header.ftl">

<@lib.showMessages/>

<h1>Výběr seriálu</h1>

<p>Na této stránce si vyberte seriál, ke kterému má být zvolený článek přiřazen.</p>

<form action="${URL.noPrefix("/serialy/edit")}" method="POST">
    <#list SERIES as series>
        <input type="radio" name="rid" value="${series.id}">
        <a href="${series.url}">${TOOL.childName(series)}</a><br>
    </#list>
    <input type="submit" value="Dokonči">
    <input type="hidden" name="action" value="addArticle2">
    <input type="hidden" name="articleRid" value="${PARAMS.articleRid}">
</form>

<#include "../footer.ftl">
