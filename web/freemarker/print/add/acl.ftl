<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Na t�to str�nce m��ete vytvo�it nov� p��stupov�
pr�vo pro zvolen�ho u�ivatele nebo skupinu. Nejd��ve
ur��te typ pr�va a jeho hodnotu. Z�rove� mus�te ur�it,
zda se toto pr�vo bude t�kat skupiny �i u�ivatele.
Pokud skupiny, vyberte ji ze seznamu, v p��pad� u�ivatele
budete p�eneseni na str�nku, kde m��ete vyhledat u�ivatele.
</p>

<form action="${URL.make("/EditRelation")}" method="POST">
 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">P��stupov� pr�vo</td>
   <td>
    <select name="right" tabindex="1">
     <option value="read" selected>ke �ten�</option>
    </select>
    <select name="value" tabindex="2">
     <option value="yes" selected>ano</option>
     <option value="no">ne</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Pro</td>
   <td>
    <select name="who" tabindex="3">
     <option value="user" selected>u�ivatele</option>
     <option value="group">skupinu</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="90">Skupina</td>
   <td>
    <#list GROUPS as group>
     <input type="radio" name="gid" value="${group.id}" <#if group_index==0>checked</#if>>${TOOL.xpath(group.data,"/data/name")}<br>
    </#list>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Pokra�uj" tabindex="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="addACL2">
 <input type="hidden" name="rid" value="${CURRENT.id}">
</form>


<#include "../footer.ftl">
