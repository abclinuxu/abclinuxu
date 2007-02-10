<#include "../header.ftl">

<@lib.showMessages/>

<h2>Smazání zprávičky</h2>

<p>Chystáte se smazat zprávičku. Důvodem může například
být, že jde o duplicitu, nepovolenou inzerci komerčních
firem či soukromých osob, nepovolené osobní oznámení,
offtopic příspěvek a podobně. Zprávičky by se měly zaměřovat
hlavně na Linux a Open Source, IT zprávy obecně nebudeme
zveřejňovat, jen ty nejdůležitější mají šanci.
</p>

<p>V tomto formuláři máte kromě smazání zprávičky možnost
zaslat jejímu autorovi i email s vysvětlením, proč jeho
zprávička nevyhovuje. Je slušností tak učinit.
</p>

<h2>Mazaná zprávička</h2>

<p>${TOOL.xpath(RELATION.child,"data/content")}</p>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Váš vzkaz</td>
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
