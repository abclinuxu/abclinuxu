<#assign plovouci_sloupec>
    <div class="s_sekce">
        <a href="${URL.make("/edit/?action=add")}">Vytvořit štítek</a>
    </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Štítky</h1>

<#if TAGS.thisPage.row == 0>
    <#assign cloudTags=VARS.getFreshCloudTags(USER!) />
    <@lib.showTagCloud list=cloudTags title="Oblíbené štítky" cssStyle="width: 500px"/>
</#if>

<ul>
    <#list TAGS.data as tag>
        <li>
            <a href="/stitky/${tag.id}">${tag.title}</a>
            (${tag.usage})
            <#if tag.parent?? && USER?? && USER.hasRole("tag admin")>
                - ${TOOL.findTag(tag.parent).title}
            </#if>
        </li>
    </#list>
</ul>

<form action="/stitky">
    <table border="0">
        <tr>
            <th>Pozice</th>
            <th>Počet</th>
            <th>Řadit podle</th>
            <th>Směr</th>
            <td></td>
        </tr>
        <tr>
            <td><input type="text" size="4" value="${TAGS.thisPage.row}" name="from" tabindex="1"></td>
            <td><input type="text" size="3" value="${TAGS.pageSize}" name="count" tabindex="2"></td>
            <td>
                <select name="orderBy" tabindex="3">
                    <option value="title"<#if PARAMS.orderBy! == "title"> selected</#if>>titulku</option>
                    <option value="count"<#if PARAMS.orderBy! == "count"> selected</#if>>počtu dokumentů</option>
                    <option value="create"<#if PARAMS.orderBy! == "create"> selected</#if>>data vytvoření</option>
                </select>
            </td>
            <td>
                <select name="orderDir" tabindex="4">
                    <option value="asc"<#if PARAMS.orderDir! == "asc"> selected</#if>>vzestupně</option>
                    <option value="desc"<#if PARAMS.orderDir! == "desc"> selected</#if>>sestupně</option>
                </select>
            </td>
            <td><input type="submit" value="Zobrazit"></td>
        </tr>
    </table>
</form>

<#if TAGS.prevPage??>
 <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
 <a href="${URL_BEFORE_FROM}${TAGS.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>0 &lt;&lt;
</#if>
${TAGS.thisPage.row}-${TAGS.thisPage.row+TAGS.thisPage.size}
<#if TAGS.nextPage??>
 <a href="${URL_BEFORE_FROM}${TAGS.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
 <a href="${URL_BEFORE_FROM}${(TAGS.total - TAGS.pageSize)?string["#"]}${URL_AFTER_FROM}">${TAGS.total}</a>
<#else>&gt;&gt; ${TAGS.total}
</#if>

<#include "../footer.ftl">
