<#include "../header.ftl">

<h1>Smaz�n� p��loh</h1>

<@lib.showMessages/>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" name="form">
    <p>
        Pros�m potvr�te, zda si opravdu p�ejete smazat tyto p��lohy:
    </p>
    <ul>
        <#list PARAMS.attachment as attachment>
            <li><a href="${attachment}">${attachment}</a></li>
        </#list>
    </ul>
    <p>
        <input type="submit" value="Dokon�i">
    </p>
    ${TOOL.saveParams(PARAMS, ["rid","action"])}
    <input type="hidden" name="action" value="remove2">
</form>
