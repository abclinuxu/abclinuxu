<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h2>Smaz�n� zpr�vi�ky</h2>

<p>Chyst�te se smazat zpr�vi�ku. D�vodem m��e nap��klad
b�t, �e jde o duplicitu, nepovolenou inzerci komer�n�ch
firem �i soukrom�ch osob, nepovolen� osobn� ozn�men�,
offtopic p��sp�vek a podobn�. Zpr�vi�ky by se m�ly zam��ovat
hlavn� na Linux a Open Source, IT zpr�vy obecn� nebudeme
zve�ej�ovat, jen ty nejd�le�it�j�� maj� �anci.
</p>

<p>V tomto formul��i m�te krom� smaz�n� zpr�vi�ky mo�nost
zaslat jej�mu autorovi i email s vysv�tlen�m, pro� jeho
zpr�vi�ka nevyhovuje. Je slu�nost� tak u�init.
</p>

<h2>Mazan� zpr�vi�ka</h2>

<p>${TOOL.xpath(RELATION.child,"data/content")}</p>

<form action="${URL.make("/EditItem")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">V� vzkaz</td>
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
