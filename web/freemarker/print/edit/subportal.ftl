<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úprava subportálu</h2>

<form action="${URL.make("/skupiny/edit")}" method="POST" enctype="multipart/form-data">
    <table cellpadding="5" border="0">
        <tr>
            <td class="required">Název</td>
            <td>
                <input type="text" name="title" size="40" maxlength="50" tabindex="1" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis</td>
            <td>
                <textarea name="desc" cols="80" rows="15" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <#assign icon=TOOL.xpath(RELATION.child,"/data/icon")?default("UNDEF")>
        <#if icon != "UNDEF">
            <tr>
                <td>Současná ikonka</td>
                <td>
                    <img src="${icon}" alt="icon">
                    <label><input type="checkbox" name="remove_icon" tabindex="3">Odstraň současnou ikonku</label>
                </td>
            </tr>
        </#if>
        <tr>
            <td>Ikonka</td>
            <td>
                <input type="file" name="icon" size="20" tabindex="4">
                <div class="error">${ERRORS.icon?if_exists}</div>
            </td>
        </tr>
        <#if USER.hasRole("root")>
        <tr>
            <td class="required">Vlastník</td>
            <td>
                <input type="text" name="owner" size="40" maxlength="50" tabindex="5" value="${PARAMS.owner?if_exists}">
                <div class="error">${ERRORS.owner?if_exists}</div>
            </td>
        </tr>
        </#if>
        <tr>
            <td width="120">&nbsp;</td>
            <td><input type="submit" VALUE="Dokonči" tabindex="6"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "../footer.ftl">
