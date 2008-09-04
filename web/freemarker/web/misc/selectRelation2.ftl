<#include "../header.ftl">

<@lib.showMessages/>

<h2>Potvrzení výběru relace</h2>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">
    <p>
        Přejete si pokračovat s relací ${TOOL.childName(CURRENT)}?
        <input type="submit" name="finish" value="Ano">
    </p>
    <input type="hidden" NAME="currentId" VALUE="${CURRENT.id}">
    <input type="hidden" NAME="ticket" VALUE="${USER.getSingleProperty('ticket')}">
    ${TOOL.saveParams(PARAMS, ["confirm","currentId","enteredId"])}
</form>

<#include "../footer.ftl">
