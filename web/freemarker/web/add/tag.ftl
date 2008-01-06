<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vytvořit štítek</h1>

<p>
    Štítky slouží ke kategorizaci dokumentů na tomto portále. Návštěvníci mohou
    oštítkovat různé dokumenty a následně podle těchto štítků vyhledávat související
    dokumenty. Každý štítek musí mít unikátní jméno, které smí obsahovat pouze znaky
    české abecedy, číslice, podtržítko, plus a pomlčku.
</p>

<form action="${URL.make("/stitky/edit")}" method="POST" name="form">
    <table cellpadding="5" class="siroka">
        <tr>
            <td class="required">
                Titulek
                <input tabindex="1" type="text" name="title" size="30" value="${PARAMS.title?if_exists?html}">
                <input tabindex="2" type="submit" name="submit" value="Dokonči">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
</form>

<#include "../footer.ftl">
