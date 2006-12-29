<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pozice: ${TOOL.xpath(POSITION, "name/text()")}</h1>

<#assign desc = TOOL.xpath(POSITION, "description/text()")?default("UNDEF"), id = TOOL.xpath(POSITION,"@id")>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table border="0">
        <tr>
            <td>Identifik�tor:</td>
            <td>${id}</td>
        </tr>
        <tr>
            <td>Stav:</td>
            <td>
                <#if TOOL.xpath(POSITION, "@active")=="yes">
                    <span class="ad_active">aktivn�</span>
                    <input type="submit" name="deactivatePosition" value="Vypnout">
                <#else>
                    <span class="ad_inactive">neaktivn�</span>
                    <input type="submit" name="activatePosition" value="Zapnout">
                </#if>
            </td>
        </tr>
        <#if desc != "UNDEFINED">
            <tr>
                <td>Popis:</td>
                <td>${desc}</td>
            </tr>
        </#if>
        <tr>
            <td>Defaultn� k�d:</td>
            <td>
                <textarea disabled rows="5" class="siroka">${DEFAULT_CODE.getText()?html}</textarea>
            </td>
        </tr>
    </table>

    <#if (CODES?size > 0)>
        <h2>Reklamn� k�dy pro ur�it� URL adresy</h2>
        <p>
            Tyto k�dy se porovnaj� s aktu�ln� URL adresou v tomto po�ad�, p�i�em� prvn� shoda
            ur�� k�d, kter� bude pou�it. Nebude-li nalezena ��dn� shoda, pou�ije se hlavn� k�d
            pozice.
        </p>
        <#list CODES as code>
            <#assign desc = TOOL.xpath(code,"@description")?default("UNDEFINED")>
            <div style="border: solid 1px black">
                <table border="0">
                    <tr>
                        <td>Regul�rn� v�raz:</td>
                        <td>${TOOL.xpath(code,"@regexp")}</td>
                    </tr>
                    <#if desc != "UNDEFINED">
                        <tr>
                            <td>Popis:</td>
                            <td>${desc}</td>
                        </tr>
                    </#if>
                    <tr>
                        <td>Reklamn� k�d:</td>
                        <td>
                            <textarea disabled rows="5" class="siroka">${code.getText()?html}</textarea>
                        </td>
                    </tr>
                </table>
                <input type="submit" name="editCode${code_index}" value="Upravit">
                <input type="submit" name="rmCode${code_index}" value="Smazat" onclick="return confirm('Opravdu chcete smazat tento k�d?')">
            </div>
        </#list>
    </#if>

    <br>
    <input type="submit" name="editPosition" value="Upravit">
    <input type="submit" name="addCode" value="P�idat k�d">
    <input type="submit" name="rmPosition" value="Smazat" onclick="return confirm('Opravdu chcete smazat tuto pozici?')">
    <input type="submit" name="nothing" value="Zp�t">
    <input type="hidden" name="identifier" value="${id}">
</form>

<#include "../footer.ftl">
