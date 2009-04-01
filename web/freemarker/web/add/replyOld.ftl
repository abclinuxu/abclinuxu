<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nový komentář ke staré diskusi</h1>

<form action="${URL.make("/EditDiscussion")}" method="GET" name="replyForm">
    <#if TOOL.isQuestion(DISCUSSION)>
        <p>
            Pozor, chystáte se komentovat ${AGE} dní starý dotaz. Pokud se nechystáte vložit či doplnit řešení tohoto dotazu,
            ale naopak se chcete na něco zeptat, položte raději nový dotaz.
        </p>
    <#else>
        <p>
            Pozor, chystáte se komentovat ${AGE} dní dní starou diskusi.
        </p>
    </#if>

    <input type="submit" name="confirmOld" value="Pokračovat">
    <input type="button" value="Zpět" onclick="javascript: history.go(-1)">

    <input type="hidden" name="action" value="add">
    <input type="hidden" name="rid" value="${RELATION.id}">
    <input type="hidden" name="dizId" value="${DISCUSSION.id}">
    <#if PARAMS.threadId??>
        <input type="hidden" name="threadId" value="${PARAMS.threadId}">
    </#if>
    <#if PARAMS.url??>
        <input type="hidden" name="url" value="${PARAMS.url}">
    </#if>
</form>

<#include "../footer.ftl">
