<#include "../header.ftl">

<@lib.showMessages/>

<h1>Výběr seriálu</h1>

<p>Na této stránce si vyberte seriál, ke kterému má být zvolený
článek přiřazen.</p>

<@lib.addForm URL.noPrefix("/clanky/edit/"+RELATION.id)>
    <#list SERIES as series>
        <input type="radio" name="series" value="${series.id}">
        <a href="${series.url}">${TOOL.childName(series)}</a><br>
    </#list>
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "addSeries2" />
</@lib.addForm>

<#include "../footer.ftl">
