<#include "../header.ftl">

<@lib.showMessages/>

<h1>Historie</h1>

<p>Nacházíte se na stránce obsahující historii objektu
${TOOL.childName(RELATION)}. Pokud si chcete prohlédnout starší verzi, zvolte odkaz
v prvním sloupečku.
</p>

<#if HISTORY?size==0>
    <p class="error">Tento objekt nemá zaznamenanou žádnou historii!</p>
<#else>
    <br>
    <table border="1">
        <tr>
            <th>Verze</th>
            <th>Autor</th>
            <th>Datum</th>
            <th>Popis změn</th>
        </tr>
        <#list HISTORY as info>
            <tr>
                <td align="center">
                <a href="${OBJECT_URL}<#if info_index!=0>${REVISION_PARAM}${info.version}</#if>">${info.version}</a>
                </td>
                <td>
                    <#assign who=TOOL.createUser(info.user)>
                    <@lib.showUser who/>
                </td>
                <td align="right">${DATE.show(info.commited,"SMART")}</td>
                <td>${info.description?if_exists}</td>
            </tr>
        </#list>
    </table>
</#if>

<#include "../footer.ftl">
