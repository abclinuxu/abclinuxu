<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h2>Smazání zprávièky</h2>

<p>Chystáte se smazat zprávièku. Dùvodem mù¾e napøíklad
být, ¾e jde o duplicitu, nepovolenou inzerci komerèních
firem èi soukromých osob, nepovolené osobní oznámení,
offtopic pøíspìvek a podobnì. Zprávièky by se mìly zamìøovat
hlavnì na Linux a Open Source, IT zprávy obecnì nebudeme
zveøejòovat, jen ty nejdùle¾itìj¹í mají ¹anci.
</p>

<p>V tomto formuláøi máte kromì smazání zprávièky mo¾nost
zaslat jejímu autorovi i email s vysvìtlením, proè jeho
zprávièka nevyhovuje. Je slu¹ností tak uèinit.
</p>

<h2>Mazaná zprávièka</h2>

<p>${TOOL.xpath(RELATION.child,"data/content")}</p>

<form action="${URL.make("/EditItem")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Vá¹ vzkaz</td>
   <td>
    <textarea name="message" cols="80" rows="15" tabindex="1"></textarea>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td><input type="submit" value="Smazat"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="remove2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
