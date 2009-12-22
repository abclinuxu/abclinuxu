<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
    <li><a href="${URL.make("/redakce/smlouvy/edit/?action=add")}" title="Přidat šablonu smlouvy">Přidat smlouvu</a></li>
</ul>
</@lib.showSignPost>

<@lib.showMessages/>

<h3>Autorské smlouvy</h3>

<p>
    Seznam autorských smluv pro autory. Pokud v systému existuje novější publikována smlouva, tak všechny dřívější
    smlouvy jsou označeny jako staré a je nabízena k souhlasu všem autorům.
</p>

<table class="siroka list">
    <thead>
        <tr>
            <th style="text-align: left">Název</th>
            <th style="text-align: left">Stav</th>
            <th style="text-align: left">Publikována</th>
            <th style="text-align: left">Podpisy</th>
            <th style="text-align: left">Akce</th>
        </tr>
    </thead>
    <tbody>
        <#if ! TEMPLATES?has_content>
            <tr>
                <td colspan="5" style="text-align: left">Nebyla nalezena žádná šablona smlouvy</td>
            </tr>
        </#if>
        <#list TEMPLATES as template>
            <tr>
                <td style="text-align: left">
                    <a href="${URL.make("/redakce/smlouvy/show/"+template.relationId)}">${template.title?html}</a>
                </td>
                <td style="text-align: left">
                    <#if template.obsolete>Stará<#elseif template.draft>Koncept<#else>Aktuální</#if>
                </td>
                <td style="text-align: left">
                    <#if ! template.draft>${DATE.show(template.published, "SMART")}<#else>N/A</#if>                    
                </td>
                <td style="text-align: left">
                    <a href="${URL.make("/redakce/smlouvy/show/"+template.relationId)+"?action=contracts"}">${template.signedContracts}</a>
                </td>
                <td style="text-align: left">
                    <a href="${URL.make("/redakce/smlouvy/edit/"+template.relationId)+"?action=edit"}">upravit</a>
                    <#if (template.signedContracts = 0)>
                        <a href="${URL.make("/redakce/smlouvy/edit/"+template.relationId)+"?action=rm"}">smazat</a>
                    </#if>
                </td>
            </tr>
        </#list>
    </tbody>
</table>

<#include "../../footer.ftl">