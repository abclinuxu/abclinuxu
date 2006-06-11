<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<p>
    Na t�to str�nce m��ete nahr�t obr�zek. Maxim�ln� velikost je omezena
    na p�l megabajtu, podporov�ny jsou form�ty JPG, PNG a GIF. Pokud obr�zek
    p�esahuje ���ku �i d�lku 200 pixel�, bude z�rove� vytvo�ena a zobrazena
    jeho miniatura.
</p>

<form action="${URL.noPrefix("/inset/"+RELATION.id)}" method="POST" enctype="multipart/form-data">
    <table width="100%" border="0" cellpadding="5">
        <tr>
            <td class="required">Jm�no souboru</td>
            <td>
                <input type="file" name="screenshot" size="40" tabindex="1">
                <div class="error">${ERRORS.screenshot?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <input tabindex="2" type="submit" name="finish" value="Dokon�i">
            </td>
        </tr>
    </table>

    <input type="hidden" name="action" value="addScreenshot2">
</form>

<#include "../footer.ftl">
