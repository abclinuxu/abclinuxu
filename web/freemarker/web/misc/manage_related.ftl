<#include "../header.ftl">

<h1>Spr�va souvisej�c�ch dokument�</h1>

<@lib.showMessages/>

<form action="${URL.make("/EditRelated/"+RELATION.id)}" method="POST" name="form">
    <#assign related = TOOL.getRelatedDocuments(RELATION.child)>
    <#list related as link>
        <label>
            <input type="checkbox" name="document" value="${link.url?url}">
            <a href="${link.url}">${link.title}</a>
        </label>
        <a href="${URL.make("/EditRelated/"+RELATION.id+"?action=edit&amp;document="+link.url?url)}">upravit</a>
        <br>
    </#list>
    <input type="hidden" name="action" value="remove">
    <input type="submit" value="Smazat">
</form>

<h3>P�id�n� nov�ho souvisej�c�ho dokumentu</h3>

<form action="${URL.make("/EditRelated/"+RELATION.id)}" method="POST" name="form">
    <p>
        Zde m��ete p�idat nov� souvisej�c� dokument. Povinn�m pol��kem je adresa dokumentu.
        Pol��ko jm�no mus�te vyplnit pouze tehdy, vkl�d�te-li dokument, kter� se nenach�z�
        na tomto port�le, nebo kdy� chcete zm�nit jeho jm�no. Popis nemus�te v�bec zad�vat,
        pou��vejte jej jen tehdy, urychl�-li to navigaci u�ivatele.
    </p>
    <table cellpadding="5" style="margin-top:1em">
        <tr>
            <td class="required">Adresa</td>
            <td>
                <input tabindex="1" type="text" name="url" size="60" value="${PARAMS.url?if_exists?html}">
                <div class="error">${ERRORS.url?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Jm�no</td>
            <td>
                <input tabindex="2" type="text" name="title" size="40" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Popis</td>
            <td>
                <textarea tabindex="3" name="description" cols="40" rows="4">${PARAMS.description?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.description?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="4" type="submit" name="submit" value="P�idej">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
</form>
