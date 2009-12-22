<#include "../../header.ftl">

<@lib.showMessages/>

<h1>Souhlasy se smlouvou</h1>

<#if CONTRACTS?has_content>
    <p>
        Seznam autorů, kteří již smlouvu odsouhlasili.
    </p>

    <table class="siroka">
        <tr>
            <th>Autor</th>
            <th>Datum přijetí</th>
            <th>&nbsp;</th>
        </tr>
        <#list CONTRACTS as contract>
            <tr>
                <td>
                    <@lib.showAuthor contract.author />
                </td>
                <td>
                    ${DATE.show(contract.signed,"SMART")}
                </td>
                <td>
                    <a href="${URL.make("/redakce/smlouvy/show/"+contract.relationId)}">detail</a>
                </td>
            </tr>
        </#list>
    </table>
<#else>
    <p>
        Tento návrh smlouvy ještě nikdo neodsouhlasil.
    </p>
</#if>

<#include "../../footer.ftl">