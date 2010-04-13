<#assign plovouci_sloupec>
    <@lib.advertisement id="hypertext2nahore" />
    <div class="s_sekce">
        <ul>
            <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify() >
            <#assign has_mrights=true>
                <li>
                    <a href="${URL.make("/EditCategory/"+RELATION.id+"?action=add")}">mkdir</a>,
                    <a href="${URL.make("/EditCategory/"+RELATION.id+"?action=edit")}">edit</a>,
                    <#if TOOL.permissionsFor(USER, RELATION.upper).canModify() >
                        <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>,
                    </#if>
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=setURL2")}">url</a>,
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a>
                </li>
                <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Přesunout</a></li>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Přesuň obsah</a></li>
            </#if>
        </ul>
    </div>

    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">

<@lib.advertisement id="square" />

<h1>Sekce ${CATEGORY.title}</h1>

<@lib.showMessages/>

<#if has_mrights??>
    <table>
        <tr>
            <th align="left">Typ</th>
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
            <th align="left">Podtyp</th>
            <td>${CATEGORY.subType!"NULL"}</td>
        </tr>
        <tr>
            <th align="left">Upravil</th>
            <td><@lib.showUserFromId CATEGORY.owner!1 /></td>
        </tr>
        <tr>
            <th align="left">Vytvořeno</th>
            <td>${DATE.show(CATEGORY.created, "SMART")}</td>
        </tr>
        <tr>
            <th align="left">Poslední změna</th>
            <td>${DATE.show(CATEGORY.created, "SMART")}</td>
        </tr>
        <#if GROUP??>
        <tr>
            <th align="left">Skupina</th>
            <td>${GROUP.title}</td>
        </tr>
        </#if>
    </table>
</#if>

<#if TOOL.xpath(CATEGORY,"data/note")??>
    <div style="margin: 1em">
        ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER!)}
    </div>
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
            ${TOOL.childName(obj)}</a>
        </td>
        <#if obj_index%3==2></tr></#if>
    </#list>
</table>

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
