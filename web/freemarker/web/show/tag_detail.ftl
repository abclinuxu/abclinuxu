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

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Štítek ${TAG.title}</h1>

<p>
    Počet dokumentů: ${TAG.usage}
</p>

<#include "../footer.ftl">
