<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>
Chyst�te se nalinkovat ${TOOL.childName(SELECTED)}
do ${TOOL.childName(CURRENT)}.
</p>
<p>
Pokud chcete zm�nit jm�no relace, zde m�te mo�nost.
Nechte tento formul�� pr�zdn�, pokud si p�ejete ponechat
p�vodn� jm�no.
</p>

<form action="${URL.noPrefix("/EditRelation")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="120">Nov� jm�no</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Dokon�i" tabindex="2"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="relationId" value="${PARAMS.relationId}">
 <input type="hidden" name="selectedId" value="${PARAMS.selectedId}">
</form>


<#include "../footer.ftl">
