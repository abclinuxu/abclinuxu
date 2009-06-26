<#include "../header.ftl">

<@lib.showMessages/>

<h1><#if EDIT_MODE!>Úprava seriálu<#else>Vytvoření seriálu</#if></h1>

<p>
    Seriál má své jméno a adresu. Adresa musí začínat prefixem <tt>/serialy</tt> a být unikátní.
    Dále je možné vložit popis, který se zobrazí jak na stránce seriálu, tak ve výpise
    všech seriálů, a zadat URL obrázku, který se zobrazí na stránce seriálu.
</p>

<form action="${URL.noPrefix("/serialy/edit")}" method="POST">
    <table class="siroka" border=0 cellpadding=5>
        <tr>
            <td class="required">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name!?html}" size="40" tabindex="1">
                <div class="error">${ERRORS.name!}<div>
            </td>
        </tr>

        <#if ! EDIT_MODE!false>
        <tr>
            <td class="required">URL seriálu</td>
            <td>
                <input type="text" name="url" value="${PARAMS.url?default("/serialy/")}" size="40" tabindex="2">
                <div class="error">${ERRORS.url!}</div>
            </td>
        </tr>
        </#if>

        <tr>
            <td>Popis</td>
            <td>
                <textarea name="desc" class="siroka" tabindex="3">${PARAMS.desc!?html}</textarea>
                <div class="error">${ERRORS.desc!}</div>
            </td>
        </tr>

        <tr>
            <td>URL obrázku</td>
            <td>
                <input type="text" name="icon" value="${PARAMS.icon!}" size="40" tabindex="4">
                <div class="error">${ERRORS.icon!}</div>
            </td>
        </tr>

        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="5"></td>
        </tr>
    </table>
    <#if EDIT_MODE!false>
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="rid" value="${RELATION.id}">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>

<#include "../footer.ftl">
