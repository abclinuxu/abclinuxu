<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>


<p>Na t�to str�nce m��ete vytvo�it novou anketu �i hodnocen�.
Jsou povoleny z�kladn� html zna�ky ( nov� ��dek, odkaz ).
M��ete tak� povolit sou�asn� vybr�n� v�ce mo�nost�.</p>

<form action="${URL.make("/EditPoll")}" method="POST">
 <#global choices=PARAMS.choices?if_exists>
 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Ot�zka</td>
   <td>
    <textarea name="question" cols="80" rows="3" tabindex="1">${PARAMS.question?if_exists?html}</textarea>
    <div class="error">${ERRORS.question?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">V�ce mo�nost�</td>
   <td>
    <select name="multichoice" tabindex="2">
     <#global multi=PARAMS.multichoice?if_exists>
     <option value="yes"<#if multi=="yes"> SELECTED</#if>>Ano</option>
     <option value="no"<#if multi!="yes"> SELECTED</#if>>Ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td class="required">Volba 1</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 0>${choices[0]}</#if>">
    <div class="error">${ERRORS.choices?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Volba 2</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 1>${choices[1]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 3</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 2>${choices[2]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 4</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 3>${choices[3]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 5</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 4>${choices[4]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 6</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 5>${choices[5]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 7</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 6>${choices[6]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 8</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 7>${choices[7]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 9</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 8>${choices[8]}</#if>">
   </td>
  </tr>
  <tr>
   <td>Volba 10</td>
   <td>
    <input type="text" name="choices" size="60" maxlength="255" tabindex="3"
    value="<#if choices?size gt 9>${choices[9]}</#if>">
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
