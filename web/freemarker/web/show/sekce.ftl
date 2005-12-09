<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="/clanky/show/3500?text=sekce+${RELATION.id}">Po¾ádejte o vytvoøení podsekce</a></li>
            <#if CATEGORY.isOpen()>
                <li>
                    <a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Vlo¾ novou polo¾ku</a>
                </li>
            </#if>
            <#if USER?exists && USER.hasRole("category admin")>
                <li>
                    <a href="${URL.make("/EditCategory?action=add&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">mkdir</a>
                </li>
                <li>
                    <a href="${URL.make("/EditCategory?action=edit&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">edit</a>
                </li>
                <li>
                    <a href="${URL.noPrefix("/EditRelation"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>
                </li>
                <li>
                    <a href="${URL.noPrefix("/SelectRelation"+RELATION.id+"?url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a>
                <li>
            </#if>
            <#if USER?exists && USER.hasRole("move relation")>
                <li>
                    <a href="${URL.noPrefix("/SelectRelation/"+RELATION.id+"?prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}>Pøesunout</a>
                </li>
                <li>
                    <a href="${URL.noPrefix("/EditRelation"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Pøesuò obsah</a>
                </li>
            </#if>
            <#if USER?exists && USER.hasRole("root")>
                <li>
                    <a href="${URL.noPrefix("/EditRelation?action=showACL&rid="+RELATION.id)}">ACL</a>
                </li>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1>Sekce ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#assign map=TOOL.groupByType(CHILDREN)>

<div class="hw">

<#if map.article?exists>
    <#list SORT.byDate(map.article, "DESCENDING") as clanek>
        <@lib.showArticle clanek, "CZ_FULL" />
        <hr>
    </#list>
    <br>
</#if>

<#if map.category?exists>
    <table class="siroka" border="0" cellpadding="2">
        <tr>
            <td colspan="3" class="cerna3"><strong>Sekce</strong></td>
        </tr>
        <#list SORT.byName(map.category) as sekce>
            <#if sekce_index%3==0><tr></#if>
            <td width="33%">
                <#if TOOL.childIcon(sekce)?exists><img src="${TOOL.childIcon(sekce)}" class="ikona" alt=""></#if>
                <a href="${URL.make(sekce.url?default("/dir/"+sekce.id))}">${TOOL.childName(sekce)}</a>
            </td>
            <#if sekce_index%3==2></tr></#if>
        </#list>
    </table>
    <br>
</#if>

<#if map.make?exists>
    <table border="0" cellpadding="2" class="siroka">
        <tr>
            <td class="cerna3"><strong>Jméno</strong></td>
            <td class="cerna3"><strong>Podpora</strong></td>
            <td class="cerna3"><strong>Zastaralý</strong></td>
            <td class="cerna3" align="right"><strong>Poslední úprava</strong></td>
        </tr>
        <#list SORT.byName(map.make) as polozka>
            <tr>
                <td>
                    <a href="${URL.make(polozka.url?default("/show/"+polozka.id))}">${TOOL.childName(polozka)}</a>
                </td>
                <td>
                    <#assign support=TOOL.xpath(polozka.child,"/data/support")?default("UNDEFINED")>
                    <#switch support>
                        <#case "complete">kompletní<#break>
                        <#case "partial">èásteèná<#break>
                        <#case "none">¾ádná<#break>
                        <#default>&nbsp;
                    </#switch>
                </td>
                <td>
                    <#if TOOL.xpath(polozka.child,"/data/outdated")?exists>ano</#if>
                </td>
                <td align="right">
                    ${DATE.show(polozka.child.updated,"CZ_FULL")}
                </td>
            </tr>
        </#list>
    </table>
    <br>
</#if>

</div>

<#include "../footer.ftl">
