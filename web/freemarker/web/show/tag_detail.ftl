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

<form action="/stitky/${TAG.id}">
    <table border="0">
        <tr>
            <th>Pozice</th>
            <th>Počet</th>
            <th>Řadit podle</th>
            <th>Směr</th>
            <td></td>
        </tr>
        <tr>
            <td><input type="text" size="4" value="${DOCUMENTS.thisPage.row}" name="from" tabindex="1"></td>
            <td><input type="text" size="3" value="${DOCUMENTS.pageSize}" name="count" tabindex="2"></td>
            <td>
                <select name="orderBy" tabindex="3">
                    <option value="title"<#if PARAMS.orderBy?if_exists=="title"> selected</#if>>titulku</option>
                    <option value="update"<#if PARAMS.orderBy?if_exists=="update"> selected</#if>>data poslední změny</option>
                    <option value="create"<#if PARAMS.orderBy?if_exists=="create"> selected</#if>>data vytvoření</option>
                </select>
            </td>
            <td>
                <select name="orderDir" tabindex="4">
                    <option value="asc"<#if PARAMS.orderDir?if_exists=="asc"> selected</#if>>vzestupně</option>
                    <option value="desc"<#if PARAMS.orderDir?if_exists=="desc"> selected</#if>>sestupně</option>
                </select>
            </td>
            <td><input type="submit" value="Zobrazit"></td>
        </tr>
    </table>
</form>

<#if DOCUMENTS.prevPage?exists>
    <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
    <a href="${URL_BEFORE_FROM}${DOCUMENTS.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>
    0 &lt;&lt;
</#if>
${DOCUMENTS.thisPage.row}-${DOCUMENTS.thisPage.row+DOCUMENTS.thisPage.size}
<#if DOCUMENTS.nextPage?exists>
    <a href="${URL_BEFORE_FROM}${DOCUMENTS.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
    <a href="${URL_BEFORE_FROM}${(DOCUMENTS.total - DOCUMENTS.pageSize)?string["#"]}${URL_AFTER_FROM}">${DOCUMENTS.total}</a>
<#else>
    &gt;&gt; ${DOCUMENTS.total}
</#if>

<#include "../footer.ftl">
