<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Tato str�nka slou�� k p�esunut� obsahu objektu ${TOOL.childName(CURRENT)}
do objektu, kter� si zvol�te na dal�� str�nce. Ve�ker� obsah
zvolen�ho typu bude p�esunut, nap��klad v�echny diskuse.
</p>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">
 <input type="radio" name="type" value="discussions" tabindex="1"> Diskuse <br>
 <input type="radio" name="type" value="makes" tabindex="2"> Polo�ky<br>
 <input type="radio" name="type" value="articles" tabindex="3"> �l�nky<br>
 <input type="radio" name="type" value="categories" tabindex="4"> Sekce<br>

 <input type="submit" value="Vyber c�l" tabindex="5">

 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${CURRENT.id}">
 <input type="hidden" name="url" value="/EditRelation">
 <input type="hidden" name="action" value="moveAll2">
</form>

<#include "../footer.ftl">
