<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Tato stránka slou¾í k pøesunutí obsahu objektu ${TOOL.childName(CURRENT)}
do objektu, který si zvolíte na dal¹í stránce. Ve¹kerý obsah
zvoleného typu bude pøesunut, napøíklad v¹echny diskuse.
</p>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">
 <input type="radio" name="type" value="discussions" tabindex="1"> Diskuse <br>
 <input type="radio" name="type" value="makes" tabindex="2"> Polo¾ky<br>
 <input type="radio" name="type" value="articles" tabindex="3"> Èlánky<br>
 <input type="radio" name="type" value="categories" tabindex="4"> Sekce<br>

 <input type="submit" value="Vyber cíl" tabindex="5">

 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${CURRENT.id}">
 <input type="hidden" name="url" value="/EditRelation">
 <input type="hidden" name="action" value="moveAll2">
</form>

<#include "../footer.ftl">
