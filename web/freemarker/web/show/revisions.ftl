<#include "../header.ftl">

<@lib.showMessages/>

<h1>Historie</h1>

<p>Nach�z�te se na str�nce obsahuj�c� historii objektu
${TOOL.childName(RELATION)}. Pokud si chcete prohl�dnout star�� verzi, zvolte odkaz
v prvn�m sloupe�ku.
</p>

<#if HISTORY?size==0>
    <p class="error">Tento objekt nem� zaznamenanou ��dnou historii!</p>
<#else>
    <br>
    <table border="1">
        <tr>
            <th>Verze</th>
            <th>Autor</th>
            <th>Datum</th>
        </tr>
        <#list HISTORY as info>
            <tr>
                <td align="center">
                <a href="${OBJECT_URL}<#if info_index!=0>${REVISION_PARAM}${info.version}</#if>">${info.version}</a>
                </td>
                <td>
                    <#assign who=TOOL.createUser(info.user)>
                    <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
                </td>
                <td align="right">${DATE.show(info.commited,"CZ_FULL")}</td>
            </tr>
        </#list>
    </table>
</#if>

<#include "../footer.ftl">
