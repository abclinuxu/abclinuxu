<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se pøidat novou polo¾ku do databáze.
Tato akce se skládá ze tøí krokù. V tomto formuláøi
vyplníte název polo¾ky. V druhém kroku zvolíte ikonku
reprezentující polo¾ku. Posledním krokem bude vyplnìní
samotného záznamu.</p>

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
   <td width="120" class="required">Jméno polo¾ky</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="16" maxlength="40" tabindex="1" class="pole">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokraèuj" TABINDEX="4" class="submit"></td>
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
