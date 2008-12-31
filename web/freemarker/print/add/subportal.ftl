<#include "../header.ftl">

<@lib.showMessages/>

<h2>Vytvoření subportálu</h2>

<form action="${URL.make("/skupiny/edit")}" method="POST" enctype="multipart/form-data">
    <table cellpadding="5" border="0">
        <tr>
            <td class="required">Název</td>
            <td>
                <input type="text" name="title" size="40" maxlength="50" tabindex="1" value="${PARAMS.title!?html}">
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis</td>
            <td>
                <textarea name="desc" cols="80" rows="15" tabindex="2">${PARAMS.desc!?html}</textarea>
                <div class="error">${ERRORS.desc!}</div>
            </td>
        </tr>
        <tr>
            <td>Ikonka</td>
            <td>
                <input type="file" name="icon" size="20" tabindex="3">
                <div class="error">${ERRORS.icon!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Vlastník</td>
            <td>
                <input type="text" name="owner" size="40" maxlength="50" tabindex="4" value="${PARAMS.owner!}">
                <div class="error">${ERRORS.owner!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">URL</td>
            <td>
                <input type="text" name="url" size="40" maxlength="50" tabindex="5" value="${PARAMS.url!}">
                <div class="error">${ERRORS.url!}</div>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td><input type="submit" VALUE="Vytvoř" tabindex="6"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
</form>

<#include "../footer.ftl">
