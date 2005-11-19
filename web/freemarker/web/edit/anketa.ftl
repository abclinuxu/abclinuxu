<#include "../header.ftl">

<p>
    Chystáte se upravit anketu. Mù¾ete mìnit texty otázky èi jednotlivých
    voleb. Mù¾ete dokonce i pøidat nové volby. Tento formuláø ale není urèen
    pro mazání èi zmìnu poøadí voleb. Zmìna poètu hlasù je mo¾ná jen na úrovni
    databáze.
</p>

<@lib.showMessages/>

<form action="${URL.make("/EditPoll")}" method="POST">
 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Otázka</td>
   <td>
    <textarea name="question" cols="80" rows="3" tabindex="1">${POLL.text?html}</textarea>
   </td>
  </tr>
  <tr>
   <td class="required">Více mo¾ností</td>
   <td>
    <select name="multichoice" tabindex="2">
     <#assign multi=POLL.multiChoice>
     <option value="yes"<#if multi> SELECTED</#if>>Ano</option>
     <option value="no"<#if ! multi> SELECTED</#if>>Ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td class="required">Uzavøená</td>
   <td>
    <select name="closed" tabindex="3">
     <#assign closed=POLL.isClosed()>
     <option value="yes"<#if closed> SELECTED</#if>>Ano</option>
     <option value="no"<#if ! closed> SELECTED</#if>>Ne</option>
    </select>
   </td>
  </tr>
  <#list POLL.choices as choice>
   <tr>
    <td class="required">Volba ${choice_index+1}</td>
    <td>
     <input type="text" name="choices" size="60" maxlength="255" value="${choice.text?html}">
    </td>
   </tr>
  </#list>
  <#list [POLL.choices?size..10] as index>
    <tr>
     <td>Volba ${index+1}</td>
     <td>
      <input type="text" name="choices" size="60" maxlength="255" value="">
     </td>
    </tr>
  </#list>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokonèi"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="pollId" value="${POLL.id}">
</form>


<#include "../footer.ftl">
