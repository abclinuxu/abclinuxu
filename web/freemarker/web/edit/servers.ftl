<#assign plovouci_sloupec>
    <div class="s_sekce">
    <ul>
        <li><a href="${URL.make("/EditServers/"+RELATION.id+"?action=add")}">Přidat nový</a></li>
    </ul>
    </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Servery rozcestníku</h1>

<p>
Zde můžete upravit RSS kanály, které se mají zobrazovat v rozcestníku (boxíky vespod úvodní stránky).
</p>

<#if SERVERS?size gt 0>
    <form action="${URL.make("/EditServers")}" method="POST">
    <table border="1">
    <tr><th></th><th>Název</th><th>URL webu</th></tr>
    
    <#list SERVERS as server>
        <tr>
            <td><input type="checkbox" name="server" value="${server.id}"></td>
            <td><a href="${URL.make("/EditServers/"+server.id+"?action=edit")}">${server.child.name}</a></td>
            <td><a href="${server.child.url}">${server.child.url}</a></td>
        </tr>
    </#list>
    </table>
    
    <input type="hidden" name="rid" value="${RELATION.id}">
    <input type="hidden" name="action" value="remove">
    <input type="submit" value="Odstranit vybrané">
    </form>
<#else>
    <h3>Žádné servery!</h3>
</#if>

<#include "../footer.ftl">

