<#include "../header.ftl">

<@lib.showMessages/>

<h2>Nastavení URL</h2>

<form action="${URL.noPrefix("/EditRelation")}" method="POST">
    <p>Zadejte číslo relace, pro kterou chcete nastavit URL.</p>
    <input type="text" name="rid" size="6" value="${PARAMS.rid!}">
    <input type="submit" value="Načti relaci">
    <input type="hidden" name="action" value="setURL2">
</form>

<#include "../footer.ftl">
