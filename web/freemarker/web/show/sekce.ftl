<#include "../header.ftl">

<h1 align="center">Sekce ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<@lib.showParents PARENTS />

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
<#if USER?exists && (USER.hasRole("move relation") || USER.hasRole("email invalidator"))>
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

<#if map.article?exists>
 <#list SORT.byDate(map.article, "DESCENDING") as clanek>
  <@lib.showArticle clanek, "CZ_FULL" />
  <@lib.separator double=!clanek_has_next />
 </#list>
 <br>
</#if>

<!-- novy obsah -->


<#if map.category?exists>

  <div class="hpboxy">

  <h2>Sekce</h2>

  <#list SORT.byName(map.category) as sekce>
    <div class="hpbox5">
	  <div class="hptit"><a href="${URL.make("/dir/"+sekce.id)}">${TOOL.childName(sekce)}</a></div>
      <div class="hpbody">
	   <center>
		  <a href="${URL.make("/dir/"+sekce.id)}"><#if TOOL.childIcon(sekce)?exists><img src="${TOOL.childIcon(sekce)}" alt=""><br></#if>
		  ${TOOL.childName(sekce)}</a>
 	   </center>
      </div>
    </div>
    </#list>

  <br class="ac">

  </div>

</#if>

<#if map.make?exists>

  <div class="hpboxy">
  <h2>Polo¾ky</h2>

  <#list SORT.byName(map.make) as polozka>
    <div class="hpbox5">
	  <div class="hptit"><a href="${URL.make("/dir/"+RELATION.id)}">${TOOL.xpath(CATEGORY,"/data/name")}</a></div>
      <div class="hpbody">
	   <center>
  		<a href="${URL.make("/show/"+polozka.id)}"><#if TOOL.childIcon(polozka)?exists><img src="${TOOL.childIcon(polozka)}" alt=""><br></#if>
  		${TOOL.childName(polozka)}</a>
 	   </center>
      </div>
    </div>
    </#list>

  <br class="ac">

  </div>

</#if>


<!-- konec noveho obsahu -->

<#include "../footer.ftl">
