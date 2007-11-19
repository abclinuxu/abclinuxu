<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava ovladače</h1>

<p>
</p>

<form action="${URL.make("/edit")}" method="POST">
    <table cellpadding="0" border="0" style="margin-top: 1em;">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Popis</td>
            <td>
                <textarea name="desc" class="siroka" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="left">
                <input type="submit" name="submit" value="Dokonči" tabindex="3">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
