<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="/clanky/show/3500?text=sekce+${RELATION.id}">Po¾ádat o vytvoøení podsekce</a></li>
            <#if CATEGORY.isOpen()>
                <li><a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Vlo¾it novou polo¾ku</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("category admin")>
                <hr />
                <li><a href="${URL.make("/EditCategory?action=add&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">mkdir</a>,
                    <a href="${URL.make("/EditCategory?action=edit&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">edit</a>,
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix="+URL.prefix)}">rmdir</a>,
                    <a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;url=/EditRelation&amp;action=add&amp;prefix="+URL.prefix)}">link</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("move relation")>
                <li><a href="${URL.noPrefix("/SelectRelation?rid="+RELATION.id+"&amp;prefix="+URL.prefix+"&amp;url=/EditRelation&amp;action=move")}">Pøesunout sekci</a></li>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=moveAll&amp;prefix="+URL.prefix)}">Pøesunout obsah</a></li>
            </#if>
            <#if USER?exists && USER.hasRole("root")>
                <li><a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=showACL")}">ACL</a></li>
            </#if>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<div class="hw">

<h1>${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#assign map=TOOL.groupByType(CHILDREN)>


<#if map.article?exists>
    <#list SORT.byDate(map.article, "DESCENDING") as clanek>
        <@lib.showArticle clanek, "CZ_FULL" />
        <hr />
    </#list>
    <br />
</#if>

<#if map.category?exists>
    <table class="hw-sekce">
      <thead>
        <tr>
	  <td colspan="3">Sekce</td>
	</tr>
      </thead>
      <tbody>
        <#list SORT.byName(map.category) as sekce>
            <#if sekce_index%3==0><tr></#if>
	    <td>
		<a href="${URL.make(sekce.url?default("/dir/"+sekce.id))}">
		<#if TOOL.childIcon(sekce)?exists><img src="${TOOL.childIcon(sekce)}" class="ikona" alt="${TOOL.childName(sekce)}"></#if>${TOOL.childName(sekce)}</a>
	    </td>
	    <#if sekce_index%3==2></tr></#if>
	</#list>
      </tbody>
    </table>
</#if>

<#if map.make?exists>
    <table class="hw-polozky">
      <thead>
        <tr>
            <td class="td01">Jméno</td>
            <td class="td02">Podpora</td>
            <td class="td03">Zastaralý</td>
            <td class="td04">Poslední úprava</td>
        </tr>
      </thead>
      <tbody>
        <#list SORT.byName(map.make) as polozka>
            <tr>
                <td class="td01">
		    <a href="${URL.make(polozka.url?default("/show/"+polozka.id))}">${TOOL.childName(polozka)}</a>
		</td>
                <td class="td02">
		  <#assign support=TOOL.xpath(polozka.child,"/data/support")?default("UNDEFINED")>
                    <#switch support>
                        <#case "complete">kompletní<#break>
                        <#case "partial">èásteèná<#break>
                        <#case "none">¾ádná<#break>
                        <#default>
                    </#switch>
		  </td>
                <td class="td03"><#if TOOL.xpath(polozka.child,"/data/outdated")?exists>ano</#if></td>
                <td class="td04">${DATE.show(polozka.child.updated,"CZ_FULL")}</td>
            </tr>
        </#list>
      </tbody>
    </table>
</#if>

</div>

<#include "../footer.ftl">
