<#assign html_header>
    <script type="text/javascript" src="/data/site/search.js"></script>
    <script language="javascript1.2" type="text/javascript">
    <!--
        var doctypeSet = new MultipleChoiceState(false);
    // -->
    </script>
</#assign>

<#if USER?? && USER.hasRole("tag admin")>
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
    Autor štítku:
    <#if CREATOR??>
        <#if CREATOR.user??>
            <@lib.showUser CREATOR.user/>
        <#else>
            neregistrovaný uživatel
        </#if>
        <#if USER?? && USER.hasRole("tag admin")>
            (${CREATOR.ip?default("Neznámá IP")})
        </#if>
    <#else>
        neznámý
    </#if>
</p>

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

<form action="/stitky/${TAG.id}" id="tagPreciseForm">
    <table border="0" width="100%">
            <#list TYPES as type>
            <#if type_index%4==0><tr></#if>
            <td>
                <label>
                    <input type="checkbox" name="typ" value="${type.key}" <#if type.set>checked</#if>>
                ${type.label}</label>
            </td>
            <#if type_index%4==3></tr></#if>
            </#list>
            <tr>
                <td colspan="4" align="left"><button type="button" onclick="toggleCheckBoxes(this.form,doctypeSet)">Vše/nic</button></td>
            </tr>
    </table>

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
                    <@lib.showOption4 "title","titulku",PARAMS.orderBy!/>
                    <@lib.showOption4 "update","data poslední změny",PARAMS.orderBy!/>
                    <@lib.showOption4 "create","data vytvoření",PARAMS.orderBy!/>
                </select>
            </td>
            <td>
                <select name="orderDir" tabindex="4">
                    <@lib.showOption4 "asc","vzestupně",PARAMS.orderDir!/>
                    <@lib.showOption4 "desc","sestupně",PARAMS.orderDir!/>
                </select>
            </td>
            <td><input type="submit" value="Zobrazit"></td>
        </tr>
    </table>
</form>

<#if DOCUMENTS.prevPage??>
    <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
    <a href="${URL_BEFORE_FROM}${DOCUMENTS.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>
    0 &lt;&lt;
</#if>
${DOCUMENTS.thisPage.row}-${DOCUMENTS.thisPage.row+DOCUMENTS.thisPage.size}
<#if DOCUMENTS.nextPage??>
    <a href="${URL_BEFORE_FROM}${DOCUMENTS.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
    <a href="${URL_BEFORE_FROM}${(DOCUMENTS.total - DOCUMENTS.pageSize)?string["#"]}${URL_AFTER_FROM}">${DOCUMENTS.total}</a>
<#else>
    &gt;&gt; ${DOCUMENTS.total}
</#if>

<#include "../footer.ftl">
