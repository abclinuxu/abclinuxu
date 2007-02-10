<#include "../header.ftl">

<h1>Smazání souvisejících dokumentů</h1>

<@lib.showMessages/>

<form action="${URL.noPrefix("/EditRelated/"+RELATION.id)}" method="POST" name="form">
    <p>
        Prosím potvrďte, zda si opravdu přejete smazat tyto související dokumenty:
    </p>
    <ul>
        <#list DOCUMENTS as document>
            <li><a href="${document.url}">${document.title}</a></li>
        </#list>
    </ul>
    <p>
        <input type="submit" value="Dokonči">
    </p>
    ${TOOL.saveParams(PARAMS, ["rid","action"])}
    <input type="hidden" name="action" value="remove2">
</form>
