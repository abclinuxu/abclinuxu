<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/edit")}" method="POST">

 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="120" class="required">Jméno polo¾ky</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="16" maxlength="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Ikona</td>
   <td>
    <input type="text" name="icon" value="${PARAMS.icon?if_exists}" size="40" tabindex="4">
    <input type="submit" name="iconChooser" value="Vybìr ikon">
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Dokonèi" TABINDEX="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="editItem2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="url" value="${URL.make("/edit")}">
 <input type="hidden" name="dir" value="${TOOL.substring(URL.prefix,1)}">
</form>


<#include "../footer.ftl">
