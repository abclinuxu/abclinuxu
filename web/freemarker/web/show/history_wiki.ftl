<#include "../header.ftl">
<#if FOUND.isQualifierSet("SORT_BY_CREATED")>
    <#assign sortByCreated = "true">
<#elseif FOUND.isQualifierSet("SORT_BY_TITLE")>
    <#assign sortByTitle = "true">
<#else>
    <#assign sortByUpdated = "true">
</#if>

<table class="siroka">
    <tr>
        <th>Název</th>
        <th>
            <#if sortByCreated??>
                Vytvořeno
            <#else>
                Poslední změna
            </#if>
        </th>
        <th>Autor</th>
    </tr>
    <#list FOUND.data as relation>
        <tr>
            <td>
                <a href="${relation.url?default("/show/"+relation.id)}">${TOOL.childName(relation)}</a>
            </td>
            <td>
                <#if sortByCreated??>
                    ${DATE.show(relation.child.created, "SMART")}
                <#else>
                    ${DATE.show(relation.child.updated, "SMART")}
                </#if>
            </td>
            <td>
                <#if sortByCreated??>
                    <#assign revInfo = TOOL.getRevisionInfo(relation.child)>
                    <@lib.showUser revInfo.creator/>
                <#else>
                    <@lib.showUser TOOL.createUser(relation.child.owner)/>
                </#if>
            </td>
        </tr>
    </#list>
</table>

<form action="/History">
    <table border="0">
        <tr>
            <th>Pozice</th>
            <th>Počet</th>
            <th>Řadit podle</th>
            <th>Směr</th>
            <td></td>
        </tr>
        <tr>
            <td><input type="text" size="4" value="${FOUND.thisPage.row}" name="from" tabindex="1"></td>
            <td><input type="text" size="3" value="${FOUND.pageSize}" name="count" tabindex="2"></td>
            <td>
                <select name="orderBy" tabindex="3">
                    <@lib.showOption5 "update", "data poslední změny", sortByUpdated??/>
                    <@lib.showOption5 "create", "data vytvoření", sortByCreated??/>
                    <@lib.showOption5 "title", "názvu", sortByTitle??/>
                </select>
            </td>
            <td>
                <select name="orderDir" tabindex="4">
                    <@lib.showOption5 "desc", "sestupně", FOUND.isQualifierSet("ORDER_DESCENDING")/>
                    <@lib.showOption5 "asc", "vzestupně", FOUND.isQualifierSet("ORDER_ASCENDING")/>
                </select>
            </td>
            <td><input type="submit" value="Zobrazit"></td>
        </tr>
    </table>
    <input type="hidden" name="type" value="${PARAMS.type}">
</form>

<#if FOUND.prevPage?exists>
 <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
 <a href="${URL_BEFORE_FROM}${FOUND.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>0 &lt;&lt;
</#if>
${FOUND.thisPage.row}-${FOUND.thisPage.row+FOUND.thisPage.size}
<#if FOUND.nextPage?exists>
 <a href="${URL_BEFORE_FROM}${FOUND.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
 <a href="${URL_BEFORE_FROM}${(FOUND.total - FOUND.pageSize)?string["#"]}${URL_AFTER_FROM}">${FOUND.total}</a>
<#else>&gt;&gt; ${FOUND.total}
</#if>

<#include "../footer.ftl">
