<#include "../header.ftl">

<@lib.showMessages/>

<h1>V�b�r seri�lu</h1>

<p>
    Na t�to str�nce si vyberte seri�l, ke kter�mu m� b�t zvolen�
    �l�nek p�i�azen.
</p>

<form action="${URL.noPrefix("/serialy/edit")}" method="POST">
    <#list SERIES as series>
        <input type="radio" name="rid" value="${series.id}">
        <a href="${series.url}">${TOOL.childName(series)}</a><br>
    </#list>
    <input type="submit" value="Dokon�i">
    <input type="hidden" name="action" value="addArticle2">
    <input type="hidden" name="articleRid" value="${PARAMS.articleRid}">
</form>

<#include "../footer.ftl">
