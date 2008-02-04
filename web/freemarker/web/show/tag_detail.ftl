<#if USER?exists && USER.hasRole("tag admin")>
    <#assign plovouci_sloupec>
        <div class="s_sekce">
            <ul>
                <li>
                    <a href="${URL.make("/edit?id="+TAG.id+"&amp;action=edit")}">Upravit</a>
                </li>
                <li>
                    <a href="${URL.make("/edit?id="+TAG.id+"&amp;action=rm2"+TOOL.ticket(USER, false))}"  onclick="return confirm('Opravdu chcete smazat tento štítek?')">Smazat</a>
                </li>
            </ul>
        </div>
    </#assign>
</#if>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Štítek ${TAG.title}</h1>

<p>
    Počet dokumentů: ${TAG.usage}
    <#assign PARENT = TOOL.findTag(TAG.parent)?default("UNDEFINED")>
    <#if (PARENT?string != "UNDEFINED")>
        <br>
        Nadřazený štítek: <a href="/stitky/${PARENT.id}">${PARENT.title}</a>
    </#if>
</p>

<ul>
    <#list DOCUMENTS.data as relation>
        <li>
            <a href="${relation.url?default("/show/"+relation.id)}">${TOOL.childName(relation)}</a>
        </li>
    </#list>
</ul>

<#include "../footer.ftl">
