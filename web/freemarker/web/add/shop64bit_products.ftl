<#include "../header.ftl">

<@lib.showMessages/>

<h1>Produkty z 64bit.cz</h1>

<p>
    Na této stránce zadejte seznam adres produktů z eshopu 64bit.cz, jejichž cena se má automaticky
    načítat do databáze portálu. Zkopírujte z internetového prohlížeče celou URL adresu detailu
    zvoleného produktu. Každou adresu dávejte na samostatný řádek.
</p>


<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90">Adresy</td>
            <td>
                <@lib.showError key="urls"/>
                <textarea name="urls" rows="30" class="siroka" tabindex="1">${PARAMS.urls?if_exists?html}</textarea>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="2" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="set64Bit2">
</form>
