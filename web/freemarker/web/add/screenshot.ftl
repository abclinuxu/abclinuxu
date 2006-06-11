<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<p>
    Na této stránce mù¾ete nahrát obrázek. Maximální velikost je omezena
    na pùl megabajtu, podporovány jsou formáty JPG, PNG a GIF. Pokud obrázek
    pøesahuje ¹íøku èi délku 200 pixelù, bude zároveò vytvoøena a zobrazena
    jeho miniatura.
</p>

<form action="${URL.noPrefix("/inset/"+RELATION.id)}" method="POST" enctype="multipart/form-data">
    <table width="100%" border="0" cellpadding="5">
        <tr>
            <td class="required">Jméno souboru</td>
            <td>
                <input type="file" name="screenshot" size="40" tabindex="1">
                <div class="error">${ERRORS.screenshot?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <input tabindex="2" type="submit" name="finish" value="Dokonèi">
            </td>
        </tr>
    </table>

    <input type="hidden" name="action" value="addScreenshot2">
</form>

<#include "../footer.ftl">
