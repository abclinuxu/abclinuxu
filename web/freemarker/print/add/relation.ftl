<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>
Chystáte se nalinkovat ${TOOL.childName(SELECTED)}
do ${TOOL.childName(CURRENT)}.
</p>
<p>
Pokud chcete zmìnit jméno relace, zde máte mo¾nost.
Nechte tento formuláø prázdný, pokud si pøejete ponechat
pùvodní jméno.
</p>

<form action="${URL.noPrefix("/EditRelation")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="120">Nové jméno</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Dokonèi" tabindex="2"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="relationId" value="${PARAMS.relationId}">
 <input type="hidden" name="selectedId" value="${PARAMS.selectedId}">
</form>


<#include "../footer.ftl">
