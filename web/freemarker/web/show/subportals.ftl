<#import "../macros.ftl" as lib>

<#if USER?? && USER.hasRole("root")>
    <#assign plovouci_sloupec>
    <div class="s_sekce">
    <ul>
        <li><a href="/skupiny/edit?action=add">Vytvořit nový</a></li>
    </ul>
    </div>
    </#assign>
</#if>

<#include "../header.ftl">

<h1>Seznam skupin</h1>

${TOOL.xpath(RELATION.child,"/data/note")}

<hr />

<#list SUBPORTALS.data as relation>
    <#assign cat=relation.child,
        icon=TOOL.xpath(cat,"/data/icon")?default("UNDEF"),
        url=relation.url,
        desc=TOOL.xpath(cat,"/data/descriptionShort")?default("UNDEF"),
        members = cat.getProperty("member"),
        score=cat.getIntProperty("score")?default(-1)>
    <#if icon!="UNDEF"><div style="float:right; padding: 5px"><img src="${icon}" alt="${cat.title}" /></div></#if>

    <h2 class="st_nadpis"><a href="${url}">${cat.title}</a></h2>
    <p>${desc}</p>
    <p class="meta-vypis"><a href="${url}?action=members">Členů</a>: ${members?size}
    | Vznik: ${DATE.show(cat.created,"CZ_DMY")}
    | Poslední změna: ${DATE.show(cat.updated,"CZ_DMY")}
    <#if score != -1>| Skóre: ${score}</#if>
    </p>
    <hr style="clear:right" />
</#list>

<form action="/skupiny">
    <table border="0">
        <tr>
            <th>Pozice</th>
            <th>Počet</th>
            <th>Řadit podle</th>
            <th>Směr</th>
            <td></td>
        </tr>
        <tr>
            <td><input type="text" size="4" value="${SUBPORTALS.thisPage.row}" name="from" tabindex="1"></td>
            <td><input type="text" size="3" value="${SUBPORTALS.pageSize}" name="count" tabindex="2"></td>
            <td>
                <select name="orderBy" tabindex="3">
                    <option value="score"<#if PARAMS.orderBy! == "score"> selected</#if>>skóre</option>
                    <option value="updated"<#if PARAMS.orderBy! == "updated"> selected</#if>>poslední aktivity</option>
                    <option value="title"<#if PARAMS.orderBy! == "title"> selected</#if>>titulku</option>
                    <option value="created"<#if PARAMS.orderBy! == "created"> selected</#if>>data vytvoření</option>
                    <option value="members"<#if PARAMS.orderBy! == "members"> selected</#if>>počtu členů</option>
                </select>
            </td>
            <td>
                <select name="orderDir" tabindex="4">
                    <option value="desc"<#if PARAMS.orderDir! == "desc"> selected</#if>>sestupně</option>
                    <option value="asc"<#if PARAMS.orderDir! == "asc"> selected</#if>>vzestupně</option>
                </select>
            </td>
            <td><input type="submit" value="Zobrazit"></td>
        </tr>
    </table>
</form>

<ul>
    <#if (SUBPORTALS.currentPage.row > 0) >
        <#assign start=SUBPORTALS.currentPage.row-SUBPORTALS.pageSize><#if (start<0)><#assign start=0></#if>
        <li>
            <a href="/skupiny?from=${start}&amp;count=${SUBPORTALS.pageSize}">Předchozí skupiny</a>
        </li>
    </#if>
    <#assign start=SUBPORTALS.currentPage.row + SUBPORTALS.pageSize>
    <#if (start < SUBPORTALS.total) >
        <li>
            <a href="/skupiny?from=${start}&amp;count=${SUBPORTALS.pageSize}">Další skupiny</a>
        </li>
    </#if>
</ul>

<#include "../footer.ftl">
