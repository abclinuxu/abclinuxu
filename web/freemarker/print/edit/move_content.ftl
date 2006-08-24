<#include "../header.ftl">

<@lib.showMessages/>

<p>Tato str�nka slou�� k p�esunut� obsahu objektu ${TOOL.childName(CURRENT)}
do objektu, kter� si zvol�te na dal�� str�nce. Ve�ker� obsah
zvolen�ho typu bude p�esunut, nap��klad v�echny diskuse.
</p>

<form action="${URL.noPrefix("/SelectRelation")}" method="POST">
 <@lib.showOption "type", "discussions", "Diskuse", "radio"/><br>
 <@lib.showOption "type", "makes", "Polo�ky", "radio"/><br>
 <@lib.showOption "type", "articles", "�l�nky", "radio"/><br>
 <@lib.showOption "type", "categories", "Sekce", "radio"/><br>

 <input type="submit" value="Vyber c�l" tabindex="5">

 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${CURRENT.id}">
 <input type="hidden" name="url" value="/EditRelation">
 <input type="hidden" name="action" value="moveAll2">
</form>

<#include "../footer.ftl">
