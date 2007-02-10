<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stránce můžete vytvořit nové přístupové
právo pro zvoleného uživatele nebo skupinu. Nejdříve
určíte typ práva a jeho hodnotu. Zároveň musíte určit,
zda se toto právo bude týkat skupiny či uživatele.
Pokud skupiny, vyberte ji ze seznamu, v případě uživatele
budete přeneseni na stránku, kde můžete vyhledat uživatele.
</p>

<form action="${URL.make("/EditRelation")}" method="POST">
 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Přístupové právo</td>
   <td>
    <select name="right" tabindex="1">
     <option value="read" selected>ke čtení</option>
     <option value="save" selected>k zápisu</option>
     <option value="delete" selected>k mazání</option>
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
     <option value="user" selected>uživatele</option>
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
   <td><input type="submit" value="Pokračuj" tabindex="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="addACL2">
 <input type="hidden" name="rid" value="${CURRENT.id}">
</form>


<#include "../footer.ftl">
