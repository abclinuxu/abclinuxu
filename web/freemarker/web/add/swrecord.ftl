<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Nápovìda</h1>

<p>Vyplòte údaje záznamu. Sna¾te se pøitom zapsat co nejvíce podrobností.
Mnohé detaily, které vám mohou pøipadat samozøejmé, jsou pro zaèáteèníky
noèní mùrou.</p>

<h1 class="st_nadpis">Formátování</h1>

<p>Smíte pou¾ívat základní HTML znaèky. Pokud je nepou¾ijete,
prázdné øádky budou nahrazeny novým odstavcem. Nedoporuèujeme
pou¾ívat znaèku ${"<pre>"?html}, vìt¹ina prohlí¾eèù pak nesprávnì
roztáhne vá¹ záznam pøes celou stránku, tak¾e je nutné scrollovat.
</p>

<form action="${URL.make("/edit")}" method="POST">

 <table width=100 border=0 cellpadding=5>

  <tr>
   <td>Verze softwaru</td>
   <td>
    <input type="text" name="version" size="20" value="${PARAMS.version?if_exists}">
   </td>
  </tr>

  <tr>
   <td>URL softwaru</td>
   <td>
    <input type="text" name="url" size="40" value="${PARAMS.url?if_exists}">
   </td>
  </tr>

  <tr>
   <td class="required">Návod èi poznámka</td>
   <td>
    <textarea name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="4"></td>
  </tr>

 </table>

 <#assign action=PARAMS.action?if_exists>
 <#if action.startsWith("addItem")>
  <input type="hidden" name="action" value="addItem3">
  <input type="hidden" name="name" value="${PARAMS.name?if_exists}">
  <input type="hidden" name="icon" value="${PARAMS.icon?if_exists}">
 <#else>
  <input type="hidden" name="action" value="${action}">
 </#if>

 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <#if PARAMS.recordId?exists>
  <input type="hidden" name="recordId" value="${PARAMS.recordId}">
 </#if>

</form>


<#include "../footer.ftl">
