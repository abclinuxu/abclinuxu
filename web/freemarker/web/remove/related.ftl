<#include "../header.ftl">

<h1>Smaz�n� souvisej�c�ch dokument�</h1>

<@lib.showMessages/>

<form action="${URL.noPrefix("/EditRelated/"+RELATION.id)}" method="POST" name="form">
    <p>
        Pros�m potvr�te, zda si opravdu p�ejete smazat tyto souvisej�c� dokumenty:
    </p>
    <ul>
        <#list DOCUMENTS as document>
            <li><a href="${document.url}">${document.title}</a></li>
        </#list>
    </ul>
    <p>
        <input type="submit" value="Dokon�i">
    </p>
    ${TOOL.saveParams(PARAMS, ["rid","action"])}
    <input type="hidden" name="action" value="remove2">
</form>
