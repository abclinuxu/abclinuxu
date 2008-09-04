<#include "../header.ftl">

<@lib.showMessages/>


<form action="${URL.noPrefix("/EditRelation")}" method="POST">
    <p>Zadejte číslo relace, pro kterou chcete nastavit URL.</p>
    <input type="text" name="rid" size="6" value="${PARAMS.rid?if_exists}">
    <input type="submit" value="Načti relaci">
    <input type="hidden" name="action" value="setURL2">
</form>

<#include "../footer.ftl">
