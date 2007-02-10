<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.url?exists>
    <form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
        <p>
            Přejete si smazat tento odkaz - <a href="${PARAMS.url}">${PARAMS.title}</a>?
            <input type="submit" name="finish" value="ano">
        </p>
        <input type="hidden" name="position" value="${PARAMS.position}">
        <input type="hidden" name="action" value="rmLink2">
    </form>
</#if>


<#include "../footer.ftl">
