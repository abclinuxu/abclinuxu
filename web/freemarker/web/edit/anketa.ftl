<#include "../header.ftl">

<h2>Úprava ankety</h2>

<p>
    Chystáte se upravit anketu. Můžete měnit texty otázky či jednotlivých
    voleb. Můžete dokonce i přidat nové volby. Tento formulář ale není určen
    pro mazání či změnu pořadí voleb. Změna počtu hlasů je možná jen na úrovni
    databáze.
</p>

<@lib.showMessages/>

<form action="${URL.make("/EditPoll")}" method="POST">
 <table class="siroka" border=0 cellpadding=5>
  <tr>
   <td class="required">Otázka</td>
   <td>
    <textarea name="question" class="siroka" rows="3" tabindex="1">${POLL.text?html}</textarea>
   </td>
  </tr>
  <tr>
   <td class="required">Více možností</td>
   <td>
    <select name="multichoice" tabindex="2">
     <#assign multi=POLL.multiChoice>
     <option value="yes"<#if multi> SELECTED</#if>>Ano</option>
     <option value="no"<#if ! multi> SELECTED</#if>>Ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td class="required">Uzavřená</td>
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
   <td>&nbsp;</td>
   <td><input type="submit" value="Dokonči"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="pollId" value="${POLL.id}">
</form>


<#include "../footer.ftl">
