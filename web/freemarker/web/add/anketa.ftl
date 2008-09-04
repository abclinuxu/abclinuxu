<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Tato stránka je určena pro vytváření nových anket. Zadejte
    text otázky a nejméně dvě volby. Z HTML značek jsou povoleny
    jen nový řádek a odkaz. Dále můžete určit, zda jeden hlasující
    může vybrat více voleb, nebo si musí zvolit jednu jedinou.
</p>

<#if POLL?exists>
    <fieldset>
    <legend>Náhled</legend>
        <@lib.showPoll POLL/>
    </fieldset>
</#if>

<form action="${URL.make("/EditPoll")}" method="POST">
 <#assign choices=PARAMS.choices?if_exists>
 <table class="siroka" border=0 cellpadding=5>
  <tr>
   <td class="required">Otázka</td>
   <td>
    <textarea name="question" class="siroka" rows="3" tabindex="1">${PARAMS.question?if_exists?html}</textarea>
    <div class="error">${ERRORS.question?if_exists}</div>
   </td>
  </tr>
  <#if RELATION.id==250>
      <tr>
       <td>URL</td>
       <td>
        /ankety/<input type="text" name="url" size="20" value="${PARAMS.url?if_exists}" tabindex="2">
        <div class="error">${ERRORS.url?if_exists}</div>
       </td>
      </tr>
  </#if>
  <tr>
   <td class="required">Více možností</td>
   <td>
    <select name="multichoice" tabindex="3">
     <#assign multi=PARAMS.multichoice?if_exists>
     <option value="yes"<#if multi=="yes"> SELECTED</#if>>Ano</option>
     <option value="no"<#if multi!="yes"> SELECTED</#if>>Ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td class="required">Volba 1</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="4"
    value="<#if choices?size gt 0>${choices[0]}</#if>">
    <div class="error">${ERRORS.choices?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Volba 2</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="5"
    value="<#if choices?size gt 1>${choices[1]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 3</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="6"
    value="<#if choices?size gt 2>${choices[2]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 4</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="7"
    value="<#if choices?size gt 3>${choices[3]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 5</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="8"
    value="<#if choices?size gt 4>${choices[4]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 6</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="9"
    value="<#if choices?size gt 5>${choices[5]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 7</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="10"
    value="<#if choices?size gt 6>${choices[6]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 8</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="11"
    value="<#if choices?size gt 7>${choices[7]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 9</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="12"
    value="<#if choices?size gt 8>${choices[8]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 10</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="13"
    value="<#if choices?size gt 9>${choices[9]}</#if>">
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
       <input type="submit" name="preview" value="Náhled" tabindex="14">
       <input type="submit" value="Dokonči" tabindex="14">
   </td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
