<#include "../header.ftl">
<@lib.showMessages/>

<h1 class="st_nadpis">Ovladaèe</h1>

<p>V této èásti si mù¾ete prohlí¾et nejaktuálnìj¹í verze rùzných ovladaèù.
Ovladaèe jsou setøízeny podle èasu vytvoøení èi poslední úpravy, nejnovìj¹ími
zaèínaje.</p>

<p>Polo¾ka ovladaèe slou¾í pro shroma¾ïování informací ohlednì ovladaèù,
které nejsou standardní souèástí jádra. Typicky jde buï o Open Source
projekty, kdy se nad¹enci sna¾í vytvoøit podporu pro daný hardware
(typicky ovladaèe scannerù) nebo výrobce odmítá uvolnit specifikaci
svých produktù komunitì a místo toho vyrábí vlastní ovladaèe (napøíklad
nVidia).
</p>

<p>Pokud chcete pøidat nový ovladaè, zkuste jej nejdøíve najít
v této èásti. Pokud budete úspì¹ní, otevøete jej a zvolte odkaz
<i>Vlo¾ novou verzi</i>. Pokud zde vá¹ ovladaè není uveden,
<a href="${URL.make("/edit?action=add")}">zde
jej mù¾ete vytvoøit</a>.<p>

<#if USER?exists && USER.hasRole("category admin")>
 <a href="${URL.make("/EditCategory?action=edit&amp;rid="+RELATION.id+"&amp;categoryId="+CATEGORY.id)}">
 <img src="/images/actions/pencil.png" class="ikona22" ALT="Uprav sekci"></a>
</#if>

<ol>
<#list SORT.byDate(CHILDREN,"DESCENDING") as relation>
 <li>
  <a href="${URL.make("/show/"+relation.id)}">
  ${TOOL.xpath(relation.child,"data/name")}, verze ${TOOL.xpath(relation.child,"data/version")}</a>
 </li>
</#list>
</ol>

<#include "../footer.ftl">
