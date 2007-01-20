<#include "../header.ftl">

<@lib.showMessages/>

<h1>
    <#if EDIT_MODE?if_exists>Úprava seriálu<#else>Vytvoøení seriálu</#if>
</h1>

<p>
    Seriál má své jméno a adresu. Adresa musí zaèínat prefixem /serialy a být unikátní.
    Dále je mo¾né vlo¾it popis, který se zobrazí jak na stránce seriálu, tak ve výpise
    v¹ech seriálù a zadat URL obrázku, který se zobrazí na stránce seriálu.
</p>

<form action="${URL.noPrefix("/serialy/edit")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists?html}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}<div>
            </td>
        </tr>

        <#if ! EDIT_MODE?if_exists>
        <tr>
            <td class="required" width="60">URL seriálu</td>
            <td>
                <input type="text" name="url" value="${PARAMS.url?default("/serialy/")}" size="40" tabindex="2">
                <div class="error">${ERRORS.url?if_exists}</div>
            </td>
        </tr>
        </#if>

        <tr>
            <td width="60">Popis</td>
            <td>
                <textarea name="desc" class="siroka" tabindex="3">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">URL obrázku</td>
            <td>
                <input type="text" name="icon" value="${PARAMS.icon?if_exists}" size="40" tabindex="4">
                <div class="error">${ERRORS.icon?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonèi" tabindex="5"></td>
        </tr>
    </table>
    <#if EDIT_MODE?if_exists>
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="rid" value="${RELATION.id}">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>

<#include "../footer.ftl">
