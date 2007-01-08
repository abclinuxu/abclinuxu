<#include "../header.ftl">

<@lib.showMessages/>

<h1>Spr�va reklamn�ch pozic</h1>

<p>
    Nach�z�te se na str�nce, kde m��ete zakl�dat nov� reklamn� pozice,
    zap�nat �i vyp�nat jednotliv� existuj�c� pozice dle pot�eby
    nebo p�id�vat k pozic�m nov� reklamn� k�dy. Reklamn� pozice je p�esn�
    definovan� prostor na str�nk�ch, kde se m� zobrazit reklama. Typick�m
    p��kladem je nap��klad banner na vr�ku str�nky. Ke ka�d� pozici je
    t�eba nadefinovat jeden �i v�ce reklamn�ch k�du, kter� maj� za �kol
    zobrazit vlastn� reklamu. V p��pad� v�ce k�du pro jednu pozici
    je t�eba k�dy rozli�it podle URL adresy.
</p>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <#if POSITIONS?exists>
        <table border="0">
		<tr>
			<td><b>&nbsp;</b></td>
			<td><b>n�zev</b></td>
			<td><b>identifik�tor</b></td>
			<td><b>stav</b></td>
		</tr>
		<#list POSITIONS as position>
                <#assign id = TOOL.xpath(position,"@id")>
                <tr>
                    <td>
                        <input type="checkbox" name="identifier" value="${id}">
                    </td>
                    <td>
                        <a href="${URL.noPrefix("/EditAdvertisement?action=showPosition&amp;identifier="+id)}">
                            ${TOOL.xpath(position, "name/text()")}
                        </a>
                    </td>
                    <td>
			 <code>${id}</code>
                    </td>
                    <td>
                        <#if TOOL.xpath(position, "@active")=="yes">
                            <span class="ad_active">aktivn�</span>
                        <#else>
                            <span class="ad_inactive">neaktivn�</span>
                        </#if>
                    </td>
                </tr>
            </#list>
            <tr>
                <td>
                    <!-- select none/all checkbox -->
                </td>
                <td colspan="2">
                    <input type="submit" name="activatePosition" value="Zapnout">
                    <input type="submit" name="deactivatePosition" value="Vypnout">
                    <input type="submit" name="rmPosition" value="Smazat" onclick="return confirm('Opravdu chcete smazat zvolen� pozice?')">
                    vybran� pozice
                </td>
            </tr>
        </table>
    </#if>
    <input type="submit" name="addPosition" value="Vlo�it novou pozici">
</form>

<#include "../footer.ftl">
