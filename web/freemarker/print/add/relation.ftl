<#include "../header.ftl">

<@lib.showMessages/>

<p>
Chystáte se nalinkovat ${TOOL.childName(SELECTED)}
do ${TOOL.childName(CURRENT)}.
</p>
<p>
Pokud chcete změnit jméno relace, zde máte možnost.
Nechte tento formulář prázdný, pokud si přejete ponechat
původní jméno.
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
   <td><input type="submit" VALUE="Dokonči" tabindex="2"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="prefix" value="${PARAMS.prefix}">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="selectedId" value="${PARAMS.selectedId}">
</form>


<#include "../footer.ftl">
