<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vytvořit štítek</h1>

<p>
    Štítky slouží ke kategorizaci dokumentů na tomto portále. Návštěvníci mohou
    oštítkovat různé dokumenty a následně podle těchto štítků vyhledávat související
    dokumenty. Každý štítek musí mít unikátní jméno, které smí obsahovat pouze znaky
    české abecedy, číslice, podtržítko, plus a pomlčku. Nadřazený štítek můžete
    definovat pro vztah mezi konkrétnějším a obecnějším štítkem, například
    mysql&nbsp;-&nbsp;databáze či java&nbsp;-&nbsp;programování.
</p>

<form action="${URL.make("/stitky/edit")}" method="POST" name="form">
    <table cellpadding="5" class="siroka">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input tabindex="1" type="text" name="title" size="30" value="${PARAMS.title?if_exists?html}">
                <@lib.showError key="title" />
            </td>
        </tr>
        <tr>
            <td>Nadřazený štítek</td>
            <td>
                <input type="text" name="parent" value="${PARAMS.parent?if_exists}" tabindex="2">
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input tabindex="3" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
</form>

<#include "../footer.ftl">
