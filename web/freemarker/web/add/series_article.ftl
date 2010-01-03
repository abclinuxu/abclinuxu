<#include "../header.ftl">

<@lib.showMessages/>

<h1>Výběr seriálu</h1>

<p>Na této stránce si vyberte seriál, ke kterému má být zvolený článek přiřazen.</p>

<@lib.addForm URL.noPrefix("/serialy/edit")>
    <#list SERIES as series>
        <label><input type="radio" name="rid" value="${series.id}">
        ------------ </label><a href="${series.url}">${TOOL.childName(series)}</a><br />
    </#list>
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "addArticle2" />
    <@lib.addHidden "articleRid", PARAMS.articleRid />
</form>

<#include "../footer.ftl">
