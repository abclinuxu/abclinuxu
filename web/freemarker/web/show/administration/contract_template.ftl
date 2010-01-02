<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
    <#if CONTRACT_TEMPLATE.draft>
        <li>
            <a href="${URL.make("/sprava/redakce/smlouvy/edit/"+CONTRACT_TEMPLATE.relationId)+"?action=publish"+TOOL.ticket(USER, false)}">Publikovat</a>
        </li>
    </#if>
    <li>
        <a href="${URL.make("/sprava/redakce/smlouvy/edit/"+CONTRACT_TEMPLATE.relationId)+"?action=edit"}">Upravit</a>
    </li>
    <li>
        <a href="${URL.make("/sprava/redakce/smlouvy/edit/"+CONTRACT_TEMPLATE.relationId)+"?action=clone"}">Duplikovat</a>
    </li>
    <#if (CONTRACT_TEMPLATE.signedContracts = 0)>
        <li>
            <a href="${URL.make("/sprava/redakce/smlouvy/edit/"+CONTRACT_TEMPLATE.relationId)+"?action=rm"}">Smazat</a>
        </li>
    </#if>
</ul>
</@lib.showSignPost>

<@lib.showMessages/>

<h1>${CONTRACT_TEMPLATE.title}</h1>

<table>
    <tr>
        <td>Stav</td>
        <td>
            <#if CONTRACT_TEMPLATE.obsolete>Stará<#elseif CONTRACT_TEMPLATE.draft>Koncept<#else>Aktuální</#if>            
        </td>
    </tr>
    <tr>
        <td>Publikována</td>
        <td>
            <#if ! CONTRACT_TEMPLATE.draft>${DATE.show(CONTRACT_TEMPLATE.published, "SMART")}<#else>N/A</#if>

        </td>
    </tr>
    <tr>
        <td>Podpisy</td>
        <td>
            <#if ! CONTRACT_TEMPLATE.draft>
                <a href="${URL.make("/sprava/redakce/smlouvy/show/"+CONTRACT_TEMPLATE.relationId)+"?action=contracts"}">${CONTRACT_TEMPLATE.signedContracts}</a>
            <#else>
                N/A
            </#if>
        </td>
    </tr>
    <tr>
        <td>Popis</td>
        <td>${CONTRACT_TEMPLATE.description!}</td>
    </tr>
</table>

<div class="contract-content">
    ${CONTRACT_TEMPLATE.content}
</div>

<#include "../../footer.ftl">