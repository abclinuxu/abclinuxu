<#include "../header.ftl">

<h1>Smazání příloh</h1>

<@lib.showMessages/>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" name="form">
    <p>
        Prosím potvrďte, zda si opravdu přejete smazat tyto přílohy:
    </p>
    <ul>
        <#list PARAMS.attachment as attachment>
            <li><a href="${attachment}">${attachment}</a></li>
        </#list>
    </ul>
    <p>
        <input type="submit" value="Dokonči">
    </p>
    ${TOOL.saveParams(PARAMS, ["rid","action"])}
    <input type="hidden" name="action" value="remove2">
</form>
