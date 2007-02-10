<#include "../header.ftl">

<@lib.showMessages/>

<p>Tato stránka slouží k přesunutí obsahu objektu ${TOOL.childName(CURRENT)}
do objektu, který si zvolíte na další stránce. Veškerý obsah
zvoleného typu bude přesunut, například všechny diskuse.
</p>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">
 <@lib.showOption "type", "discussions", "Diskuse", "radio"/><br>
 <@lib.showOption "type", "makes", "Položky", "radio"/><br>
 <@lib.showOption "type", "articles", "Články", "radio"/><br>
 <@lib.showOption "type", "categories", "Sekce", "radio"/><br>

 <input type="submit" value="Vyber cíl" tabindex="5">

 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${CURRENT.id}">
 <input type="hidden" name="url" value="/EditRelation">
 <input type="hidden" name="action" value="moveAll2">
</form>

<#include "../footer.ftl">
