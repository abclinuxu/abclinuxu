<#include "../header.ftl">
<@lib.showMessages/>

<h1>Ovladače</h1>

<p>V této části si můžete prohlížet nejaktuálnější verze různých ovladačů.
Ovladače jsou setřízeny podle času vytvoření či poslední úpravy, nejnovějšími
začínaje.</p>

<p>Položka ovladače slouží pro shromažďování informací ohledně ovladačů,
které nejsou standardní součástí jádra. Typicky jde buď o Open Source
projekty, v rámci kterých se nadšenci snaží vytvořit podporu pro daný hardware
(typicky ovladače scannerů), nebo výrobce odmítá uvolnit specifikace
svých produktů komunitě a místo toho vyrábí vlastní ovladače (například
nVidia).
</p>

<p>Pokud chcete přidat nový ovladač, zkuste jej nejdříve najít
v této části. Pokud budete úspěšní, otevřete jej a zvolte odkaz
<i>Vlož novou verzi</i>. Pokud zde váš ovladač není uveden,
<a href="${URL.make("/edit?action=add")}">zde
jej můžete vytvořit</a>.<p>

<ol>
<#list SORT.byDate(CHILDREN,"DESCENDING") as relation>
 <li>
  <a href="${relation.url?default("/ovladace/show/"+relation.id)}">
  ${TOOL.xpath(relation.child,"data/name")}, verze ${TOOL.xpath(relation.child,"data/version")}</a>
 </li>
</#list>
</ol>

<#include "../footer.ftl">
