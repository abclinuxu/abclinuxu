<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <#if USER?exists && USER.hasRole("category admin")>
                <li>
                    <a href="${URL.make("/EditCategory/"+RELATION.id+"?action=add")}">mkdir</a>,
                    <a href="${URL.make("/EditCategory/"+RELATION.id+"?action=edit")}">edit</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=setURL2")}">url</a>,
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a>
                </li>
            </#if>
            <#if USER?exists && USER.hasRole("move relation")>
                <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Přesunout</a></li>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Přesuň obsah</a></li>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1>Sekce ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<@lib.showMessages/>

<#if USER?exists && USER.hasRole("category admin")>
    <table>
        <tr>
            <th>Typ</th>
            <td>
                <#switch CATEGORY.type>
                    <#case 0>nedefinován <#break>
                    <#case 1>sekce hardware <#break>
                    <#case 2>fórum <#break>
                    <#case 3>blog <#break>
                    <#case 4>rubrika <#break>
                    <#case 5>sekce FAQ <#break>
                    <#case 6>sekce software <#break>
                </#switch>
            </td>
        </tr>
        <tr>
            <th>Podtyp</th>
            <td>${CATEGORY.subType?default('NULL")}</td>
        </tr>
        <tr>
            <th>Upravil</th>
            <td><@lib.showUser who CATEGORY.owner /></td>
        </tr>
        <tr>
            <th>Vytvořeno</th>
            <td>${DATE.render(CATEGORY.created, "SMART")}</td>
        </tr>
        <tr>
            <th>Poslední změna</th>
            <td>${DATE.render(CATEGORY.created, "SMART")}</td>
        </tr>
    </table>
</#if>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<table class="hw-sekce">
    <#list SORT.byName(CHILDREN) as obj>
        <#if obj_index%3==0><tr></#if>
        <td>
            <#if obj.child.class?contains("Category")>
                <a href="${URL.make("/dir/"+obj.id+"?raw=true")}">
            <#else>
                <a href="${URL.make("/show/"+obj.id)}">
            </#if>
            <#if TOOL.childIcon(obj)?exists>
                <img src="${TOOL.childIcon(obj)}" class="ikona" alt="${TOOL.childName(obj)}">
            </#if>
            ${TOOL.childName(obj)}</a>
        </td>
        <#if obj_index%3==2></tr></#if>
    </#list>
</table>

<#include "../footer.ftl">
