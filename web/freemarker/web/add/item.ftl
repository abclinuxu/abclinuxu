<#include "../header.ftl">

<@lib.showMessages/>

<p>Chyst�te se p�idat novou polo�ku do datab�ze.
Tato akce se skl�d� ze t�� krok�. V tomto formul��i
vypln�te n�zev polo�ky. V druh�m kroku zvol�te ikonku
reprezentuj�c� polo�ku. Posledn�m krokem bude vypln�n�
samotn�ho z�znamu.</p>

<#if PARAMS.icon?exists>
 <form action="${URL.make("/edit")}" method="POST">
 <input type="hidden" name="icon" value="${PARAMS.icon}">
<#else>
 <form action="${URL.noPrefix("/SelectIcon")}" method="POST">
 <input type="hidden" name="dir" value="${TOOL.substring(URL.prefix,1)}">
 <input type="hidden" name="url" value="${URL.make("/edit")}">
</#if>


 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="120" class="required">Jm�no polo�ky</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="16" maxlength="40" tabindex="1" class="pole">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokra�uj" TABINDEX="4" class="submit"></td>
  </tr>
 </table>

 <#assign action=PARAMS.action?if_exists>
 <#if action.endsWith("Item")>
  <#assign action=PARAMS.action+"2">
 </#if>

 <input type="hidden" name="action" value="${action}">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 ${TOOL.saveParams(PARAMS, ["rid","name","finish","action"])}
</form>


<#include "../footer.ftl">
