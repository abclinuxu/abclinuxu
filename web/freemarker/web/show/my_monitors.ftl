<#include "../header.ftl">

<@lib.showMessages/>

<h1>Seznam sledovaných dokumentů</h1>

<p>
    Na této stránce najdete seznam dokumentů, které jste nechali sledovat. Pokud je chcete přestat sledovat všechny
    najednou, zvolte tlačítko <code>Odstranit vše</code>. Tato operace je nevratná.
</p>

<#if (MONITORED.getTotal() == 0)>
    <p>
        Nesledujete žádné dokumenty. Monitor můžete zapnout u dokumentu (například diskuse) v liště <code>Nástroje</code>
        pomocí odkazu <code>Začni sledovat</code>.
    </p>
<#else>
    <form action="/EditMonitor" method="POST">
        <input type="hidden" name="action" value="removeAll" />
        <input type="hidden" name="uid" value="${PROFILE.id}" />
        <input type="submit" value="Odstranit vše">
    </form>

    <table class="siroka">
        <tr>
            <th>Název</th>
            <th>Poslední změna</th>
        </tr>
        <#list MONITORED.data as relation>
            <tr>
                <td>
                    <a href="${relation.url!("/show/"+relation.id)}">${TOOL.childName(relation)}</a>
                </td>
                <td>
                    ${DATE.show(relation.child.updated, "SMART")}
                </td>
                <td>
                    <@lib.showUserFromId relation.child.owner/>
                </td>
            </tr>
        </#list>
    </table>

    <#if MONITORED.prevPage??>
        <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
        <a href="${URL_BEFORE_FROM}${MONITORED.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
    <#else>
        0 &lt;&lt;
    </#if>

    ${MONITORED.thisPage.row}-${MONITORED.thisPage.row+MONITORED.thisPage.size}

    <#if MONITORED.nextPage??>
        <a href="${URL_BEFORE_FROM}${MONITORED.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
        <a href="${URL_BEFORE_FROM}${(MONITORED.total - MONITORED.pageSize)?string["#"]}${URL_AFTER_FROM}">${MONITORED.total}</a>
    <#else>
        &gt;&gt; ${MONITORED.total}
    </#if>
</#if>

<#include "../footer.ftl">
