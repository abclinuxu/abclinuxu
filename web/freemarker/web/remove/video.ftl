<#include "../header.ftl">

<@lib.showMessages/>

<h1>Smazání videa</h1>

<p>
    Opravdu chcete smazat video <i>${TOOL.childName(RELATION)}</i>?
</p>

<form action="${URL.make("/edit")}" method="POST">
    <input type="submit" value="Smazat">
    <input type="hidden" name="action" value="remove2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
