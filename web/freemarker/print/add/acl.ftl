<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Na této stránce mù¾ete vytvoøit nové pøístupové
právo pro zvoleného u¾ivatele nebo skupinu. Nejdøíve
urèíte typ práva a jeho hodnotu. Zároveò musíte urèit,
zda se toto právo bude týkat skupiny èi u¾ivatele.
Pokud skupiny, vyberte ji ze seznamu, v pøípadì u¾ivatele
budete pøeneseni na stránku, kde mù¾ete vyhledat u¾ivatele.
</p>

<form action="${URL.make("/EditRelation")}" method="POST">
 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Pøístupové právo</td>
   <td>
    <select name="right" tabindex="1">
     <option value="read" selected>ke ètení</option>
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
     <option value="user" selected>u¾ivatele</option>
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
   <td><input type="submit" value="Pokraèuj" tabindex="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="addACL2">
 <input type="hidden" name="rid" value="${CURRENT.id}">
</form>


<#include "../footer.ftl">
