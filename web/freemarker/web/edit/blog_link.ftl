<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.url?exists>
    <h3>Upravit odkaz</h3>
    <form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
    <table border="0">
        <tr>
            <td class="required">URL</td>
            <td>
                <input type="text" name="url" title="URL odkazu" value="${PARAMS.url?if_exists}" size="30">
                <div class="error">${ERRORS.url?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis</td>
            <td>
                <input type="text" name="title" title="Popis odkazu" value="${PARAMS.title?if_exists}" size="30">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td><input type="submit" name="finish" value="Ulo¾it"></td>
            <td></td>
        </tr>
    </table>
    <input type="hidden" name="position" value="${PARAMS.position}">
    <input type="hidden" name="action" value="editLink2">
    </form>
</#if>

<#include "../footer.ftl">
