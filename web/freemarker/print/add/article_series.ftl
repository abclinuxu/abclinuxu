<#include "../header.ftl">

<@lib.showMessages/>

<h1>Výbìr seriálu</h1>

<p>
    Na této stránce si vyberte seriál, ke kterému má být zvolený
    èlánek pøiøazen.
</p>

<form action="${URL.noPrefix("/clanky/edit/"+RELATION.id)}" method="POST">
    <#list SERIES as series>
        <input type="radio" name="series" value="${series.id}">
        <a href="${series.url}">${TOOL.childName(series)}</a><br>
    </#list>
    <input type="submit" value="Dokonèi">
    <input type="hidden" name="action" value="addSeries2">
</form>

<#include "../footer.ftl">
