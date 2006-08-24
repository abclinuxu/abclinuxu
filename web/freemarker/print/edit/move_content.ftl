<#include "../header.ftl">

<@lib.showMessages/>

<p>Tato stránka slou¾í k pøesunutí obsahu objektu ${TOOL.childName(CURRENT)}
do objektu, který si zvolíte na dal¹í stránce. Ve¹kerý obsah
zvoleného typu bude pøesunut, napøíklad v¹echny diskuse.
</p>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">
 <@lib.showOption "type", "discussions", "Diskuse", "radio"/><br>
 <@lib.showOption "type", "makes", "Polo¾ky", "radio"/><br>
 <@lib.showOption "type", "articles", "Èlánky", "radio"/><br>
 <@lib.showOption "type", "categories", "Sekce", "radio"/><br>

 <input type="submit" value="Vyber cíl" tabindex="5">

 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${CURRENT.id}">
 <input type="hidden" name="url" value="/EditRelation">
 <input type="hidden" name="action" value="moveAll2">
</form>

<#include "../footer.ftl">
