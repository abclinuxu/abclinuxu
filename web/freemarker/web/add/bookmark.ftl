<#include "../header.ftl">

<h1>Přidání záložky</h1>

<#assign directory=PARAMS.path?default("/")>
<#if directory==""><#assign directory="/"></#if>
<p>
    Záložka bude přidána do adresáře <b>${directory}</b>.
</p>

<form action="${URL.noPrefix("/EditBookmarks/"+MANAGED.id)}" method="POST">
<table>
    <tr>
        <td>Název</td>
        <td>
            <input type="text" name="title" value="${PARAMS.title!}" size="40"> Zadejte, pokud vkládáte externí odkaz
            <div class="error">${ERRORS.title!}</div>
        </td>
    </tr>
    <tr>
        <td class="required">URL</td>
        <td>
            <input type="text" name="url" value="${PARAMS.url!}" size="40">
            <div class="error">${ERRORS.url!}</div>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td><input type="submit" value="Přidat"></td>
    </tr>
    <input type="hidden" name="ticket" value="${USER.getSingleProperty('ticket')}">
    <input type="hidden" name="path" value="${PARAMS.path!}">
    <input type="hidden" name="action" value="addLink2">
</table>
</form>

<#include "../footer.ftl">
