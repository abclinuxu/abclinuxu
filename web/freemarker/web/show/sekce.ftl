<#include "../header.ftl">

<h1>Sekce ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<@lib.showMessages/>

<p>
<a href="/clanky/show/3500?text=sekce+${RELATION.id}">Po¾ádejte o vytvoøení podsekce</a>
<#if CATEGORY.isOpen()>
 <a href="${URL.make("/edit?action=addItem&rid="+RELATION.id)}">Vlo¾ novou polo¾ku</a>
</#if>
</p>

<#if USER?exists && USER.hasRole("category admin")>
 <#assign toolbar=true>
 <a href="${URL.make("/EditCategory?action=add&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}">mkdir</a>
 <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}">edit</a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}">rmdir</a>
 <a href="${URL.noPrefix("/SelectRelation?url=/EditRelation&action=add&prefix="+URL.prefix+"&rid="+RELATION.id)}">ln -s</a>
</#if>
<#if USER?exists && (USER.hasRole("move relation"))>
 <#assign toolbar=true>
 <a href="/SelectRelation?url=/EditRelation&action=move&rid=${RELATION.id}&prefix=${URL.prefix}">mv</a>
 <a href="/EditRelation?action=moveAll&rid=${RELATION.id}&prefix=${URL.prefix}">Pøesuò obsah</a>
</#if>
<#if USER?exists && USER.hasRole("root")>
 <#assign toolbar=true>
 <a href="${URL.noPrefix("/EditRelation?action=showACL&rid="+RELATION.id)}">ACL</a>
</#if>
<#if toolbar?exists><p></p></#if>

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
 <tr><td colspan="3" class="cerna3"><strong>Sekce</strong></td></tr>
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
 <tr><td colspan="3" class="cerna3"><strong>Polo¾ky</strong></td></tr>
 <#list SORT.byName(map.make) as polozka>
  <#if polozka_index%3==0><tr></#if>
  <td width="33%">
  <#if TOOL.childIcon(polozka)?exists><img src="${TOOL.childIcon(polozka)}" class="ikona" alt=""></#if>
  <a href="${URL.make(polozka.url?default("/show/"+polozka.id))}">${TOOL.childName(polozka)}</a>
  </td>
  <#if polozka_index%3==2></tr></#if>
 </#list>
 </table>
 <br>
</#if>
</div>

<#include "../footer.ftl">
