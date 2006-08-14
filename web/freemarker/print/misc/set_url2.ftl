<#include "../header.ftl">

<@lib.showMessages/>


<form action="${URL.noPrefix("/EditRelation")}" method="POST">
    <p>
        Chystáte se nastavit URL pro <a href="${CURRENT.url?default('/hardware/show/'+CURRENT.id)}">${TOOL.childName(CURRENT)}</a>
    </p>
    <p>
        Zadejte lokální èást URL:
        ${PARENT.url}/ <input type="text" name="url" size="80" value="${PARAMS.url?if_exists}">
        <div class="error">${ERRORS.url?if_exists}</div>
    </p>
    <input type="submit" value="Dokonèi">
    <input type="hidden" name="rid" value="${CURRENT.id}">
    <input type="hidden" name="action" value="setURL3">
</form>

<#include "../footer.ftl">
