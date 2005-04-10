<#include "../header.ftl">

<@lib.showMessages/>

<#if USER?exists && USER.hasRole("category admin")>
 <p>
 <a href="${URL.make("/edit?action=add&rid="+RELATION.id)}"
 title="Vytvoø podkategorii"><img src="/images/actions/attach.png" ALT="Pøidej èlánek" class="ikona22"></a>
 <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}"
 title="Uprav kategorii"><img src="/images/actions/pencil.png" class="ikona22" ALT="Uprav sekci"></a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}"
 title="Sma¾ kategorii"><img src="/images/actions/delete.png" ALT="Sma¾ sekci" class="ikona"></a>
 </p>
</#if>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 <p class="note">${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}</p>
</#if>

<h1 class="st_nadpis">Seznam rubrik</h1>

<ul>
 <#list SORT.byName(CHILDREN) as relation>
  <li>
   <a href="${URL.make("/dir/"+relation.id)}">${TOOL.childName(relation)}</a>
  </li>
 </#list>
</ul>

<#include "../footer.ftl">
