<#include "../header.ftl">

<@lib.showMessages/>

<h1>
    <#if EDIT_MODE?if_exists>�prava seri�lu<#else>Vytvo�en� seri�lu</#if>
</h1>

<p>
    Seri�l m� sv� jm�no a adresu. Adresa mus� za��nat prefixem /serialy a b�t unik�tn�.
    D�le je mo�n� vlo�it popis, kter� se zobraz� jak na str�nce seri�lu, tak ve v�pise
    v�ech seri�l� a zadat URL obr�zku, kter� se zobraz� na str�nce seri�lu.
</p>

<form action="${URL.noPrefix("/serialy/edit")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Jm�no</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists?html}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}<div>
            </td>
        </tr>

        <#if ! EDIT_MODE?if_exists>
        <tr>
            <td class="required" width="60">URL seri�lu</td>
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
            <td width="60">URL obr�zku</td>
            <td>
                <input type="text" name="icon" value="${PARAMS.icon?if_exists}" size="40" tabindex="4">
                <div class="error">${ERRORS.icon?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokon�i" tabindex="5"></td>
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
