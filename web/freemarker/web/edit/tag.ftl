<#include "../header.ftl">

<@lib.showMessages/>

<h1>Upravit štítek</h1>

<p>
    Každý štítek musí mít unikátní jméno, které smí obsahovat pouze znaky
    české abecedy, číslice, podtržítko, plus a pomlčku. Editací se nezmění
    url štítku. Nadřazený štítek můžete definovat pro vztah mezi konkrétnějším
    a obecnějším štítkem, například mysql&nbsp;-&nbsp;databáze či
    java&nbsp;-&nbsp;programování.
</p>

<form action="${URL.make("/stitky/edit")}" method="POST" name="form">
    <table cellpadding="5" class="siroka">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input tabindex="1" type="text" name="title" size="30" value="${PARAMS.title!?html}">
                <@lib.showError key="title" />
            </td>
        </tr>
        <tr>
            <td>
                Nadřazený štítek
                <@lib.showHelp>Id štítku, například programovani z URL /stitky/programovani</@lib.showHelp>
            </td>
            <td>
                <input type="text" name="parent" value="${PARAMS.parent!}" tabindex="2">
                <@lib.showError key="parent" />
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input tabindex="3" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="id" value="${PARAMS.id}">
</form>

<#include "../footer.ftl">
