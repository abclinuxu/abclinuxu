<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nový komentář ke staré diskusi</h1>

<@lib.addForm URL.make("/EditDiscussion"), "name='replyForm'">
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

    <@lib.addSubmit "Pokračovat", "confirmOld" />
    <input type="button" value="Zpět" onclick="javascript: history.go(-1)">
    <@lib.addHidden "action", "add" />
    <@lib.addHidden "rid", RELATION.id />
    <@lib.addHidden "dizId", DISCUSSION.id />
    <#if PARAMS.threadId??>
        <@lib.addHidden "threadId", PARAMS.threadId />
    </#if>
    <#if PARAMS.url??>
        <@lib.addHidden "url", PARAMS.url />
    </#if>
</@lib.addForm>

<#include "../footer.ftl">
