<#include "../../header.ftl">

<#if CONTRACTS?has_content >
    <h2>Odsouhlasené smlouvy</h2>

    <p>
        Seznam smluv, které jste odsouhlasili.
    </p>

    <table>
        <thead>
            <tr>
                <th>Titulek</th>
                <th>Datum přijetí</th>
            </tr>
        </thead>
        <tbody>
            <#list CONTRACTS as contract>
                <tr>
                    <td>
                        <a href="${URL.make("/redakce/smlouvy/show/"+contract.relationId)}">${contract.title}</a>
                    </td>
                    <td>${DATE.show(contract.signed, "ISO_DMY")}</td>
                </tr>
            </#list>
        </tbody>
    </table>
</#if>

<#if CONTRACT_TEMPLATE??>
    <h2>Čekající smlouva</h2>

    <#if CONTRACT_TEXT??>
        <p>
            Na odsouhlasení čeká následující smlouva. Případné dotazy směřujte na šefredaktora.
        </p>


        <form action="${URL.noPrefix("/sprava/redakce/smlouvy/edit")}" method="POST">
            <table>
                <tr>
                    <th>Název</th>
                    <td>${CONTRACT_TEMPLATE.title}</td>
                </tr>
                <tr>
                    <th>Popis</th>
                    <td>${CONTRACT_TEMPLATE.description}</td>
                </tr>
                <tr>
                    <th>Text</th>
                    <td>${CONTRACT_TEXT}</td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td><input type="submit" value="Přijmout"/></td>
                </tr>
            </table>

            <input type="hidden" name="action" value="sign" />
            <input type="hidden" name="rid" value="${CONTRACT_TEMPLATE.relationId}" />
        </form>
    <#else>
        <p>${CONTRACT_ERROR!}</p>
    </#if>
</#if>
<#include "../../footer.ftl">
