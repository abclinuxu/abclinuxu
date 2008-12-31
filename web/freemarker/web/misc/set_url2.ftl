<#include "../header.ftl">

<@lib.showMessages/>

<h2>Nastavení textové adresy</h2>

<p>
    Na této stránce je možné nastavit textovou adresu pro zvolený dokument.
    Pokud má nadřazená relace (upper) textové url, je vloženo do textového
    políčka spolu se jménem vygenerovaným ze jména dokumentu. Odesláním
    formuláře bude zvolené jméno uloženo a případné původní url uloženo
    do tabulky přesměrování. Duplikátní url jsou zakázány.
</p>
<p>
    U diskusních fór je z důvodu zkrácení navigační cesty nastavena poradna
    jako nadřazená relace. Nicméně URL by mělo obsahovat sekci (například
    distribuce). Proto dávejte pozor a opravte URL, aby obsahovalo i cestu
    k nadřazené sekci.
</p>

<form action="${URL.noPrefix("/EditRelation")}" method="POST">
    <table>
        <tr>
            <td>Dokument</td>
            <td><a href="${URL.getRelationUrl(CURRENT)}">${TOOL.childName(CURRENT)}</a></td>
        </tr>
        <tr>
            <td>URL dokumentu</td>
            <td>${CURRENT.url?default("nedefinováno")}</td>
        </tr>
        <tr>
            <td>URL nadřazené relace</td>
            <td>
                <#if (CURRENT.upper > 0)>
                    <#assign upper = TOOL.createRelation(CURRENT.upper)>
                    ${upper.url?default("nedefinováno")}
                <#else>
                    nedefinováno
                </#if>
            </td>
        </tr>
        <tr>
            <td>Nové URL dokumentu</td>
            <td>
                <input type="text" name="url" size="80" value="${PARAMS.url!}">
                <div class="error">${ERRORS.url!}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="rid" value="${CURRENT.id}">
    <input type="hidden" name="action" value="setURL3">
</form>

<#include "../footer.ftl">
