<#include "../header.ftl">

<@lib.showMessages/>

<p>D�kujeme v�m za va�e rozhodnut� pomoci n�m se spr�vou
diskus�. V��me si toho. Spole�n� m��eme ud�lat z Ab��ka
nejlep�� zdroj linuxov�ch informac� v�bec.
</p>

<p>Nyn� pros�m vyberte vhodn�j�� f�rum pro tuto diskusi,
Pak zadejte do formul��e sv� jm�no, emailovou adresu
a p��padn� vzkaz.
</p>

<form action="${URL.make("/EditRequest")}" method="POST">
 <table border=0 cellpadding=5>
  <#list FORUMS?keys as name>
   <#assign FORUM=FORUMS[name]>
   <tr><td colspan="4" align="center">${name}</td></tr>
   <#list FORUM as i>
    <#if i_index%4==0><tr></#if>
    <td>
     <input type="radio" name="forumId" value="${i.id}"><a href="/forum/dir/${i.id}">${TOOL.childName(i)}</a>
    </td>
    <#if i_index%4==3></tr></#if>
   </#list>
  </#list>
  <tr>
   <td class="required">Va�e jm�no</td>
   <#if PARAMS.author?exists><#assign author=PARAMS.author><#elseif USER?exists><#assign author=USER.name></#if>
   <td colspan="3">
    <input type="text" name="author" value="${author?if_exists}" size="20" tabindex="1">
    <span class="error">${ERRORS.author?if_exists}</span>
   </td>
  </tr>
  <tr>
    <td class="required">V� email</td>
   <#if PARAMS.email?exists><#assign email=PARAMS.email><#elseif USER?exists><#assign email=USER.email></#if>
   <td colspan="3">
    <input type="text" name="email" value="${email?if_exists}" size="20" tabindex="2">
    <span class="error">${ERRORS.email?if_exists}</span>
   </td>
  </tr>
  <tr>
   <td>Vzkaz</td>
   <td colspan="3">
    <textarea name="text" cols="60" rows="5" tabindex="3">${PARAMS.text?if_exists?html}</textarea>
    <span class="error">${ERRORS.text?if_exists}</span>
   </td>
  </tr>
  <tr>
   <td></td>
   <td><input type="submit" value="OK" tabindex="4"></td>
  </tr>
 </table>
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="action" value="rightForum">
</form>

<#include "../footer.ftl">
