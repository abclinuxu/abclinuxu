<#include "../header.ftl">
<@lib.showMessages/>

<h1>Administrativní po¾adavky</h1>

<p>Na tomto místì najdete seznam <b>administrativních</b> po¾adavkù
na správce serveru. Pokud máte nìjaký problém s portálem www.abclinuxu.cz,
potøebujete zalo¾it novou sekci, nebo jste zapomnìli pøihla¹ovací údaje,
pou¾ijte ní¾e uvedený formuláø.</p>

<p>Námìty na nové slu¾by, vylep¹ení souèasných slu¾eb nebo nalezené
chyby zasílejte autorovi portálu <a href="/Profile/1">Leo¹i Literákovi</a>.
</p>

<h1>Toto není technická podpora pro Linux!</h1>

<p>Neple»te si tento formuláø s technickou podporou pro Linux!
Tento formuláø se opravdu týká jen po¾adavkù na <b>chod portálu</b>
www.abclinuxu.cz. Ostatní otázky budou bez odpovìdi smazány.</p>

</p>Potøebujete-li poradit s Linuxem, vyzkou¹ejte na¹e
mocné vyhledávání a nenajdete-li øe¹ení, po¾ádejte o pomoc
v <a href="/diskuse.jsp">diskusním fóru.</a>
</p>

<form action="${URL.make("/EditRequest")}" method="POST">
 <table border=0 cellpadding=5 style="padding-top: 10px">
  <tr>
   <td class="required">Va¹e jméno</td>
   <#if PARAMS.author?exists>
    <#assign author=PARAMS.author>
   <#elseif USER?exists>
    <#assign author=USER.name>
   </#if>
   <td>
    <input type="text" name="author" value="${author?if_exists}" size="20" tabindex="1" class="pole">
    <span class="error">${ERRORS.author?if_exists}</span>
   </td>
  </tr>
  <tr>
    <td class="required">Vá¹ email</td>
   <#if PARAMS.email?exists>
    <#assign email=PARAMS.email>
   <#elseif USER?exists>
    <#assign email=USER.email>
   </#if>
   <td>
    <input type="text" name="email" value="${email?if_exists}" size="20" tabindex="2" class="pole">
    <span class="error">${ERRORS.email?if_exists}</span>
   </td>
  </tr>
  <tr>
   <td class="required">Po¾adavek</td>
   <td>
    <textarea name="text" cols="60" rows="15" tabindex="3">
${PARAMS.text?default("Sem patøí jen po¾adavky na administrátory portálu www.abclinuxu.cz, otázky kolem Linuxu zadávejte do diskusního fóra!")?html}
    </textarea>
    <span class="error">${ERRORS.text?if_exists}</span>
  </td>
  </tr>
  <tr>
   <td></td>
   <td><input type="submit" value="OK" tabindex="4" class="buton"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add">
</form>

<#if CHILDREN?exists && CHILDREN?size gt 0>
 <table border=0 cellpadding=5>
  <tr>
   <th>Nevyøízené po¾adavky</th>
  </tr>
  <#list CHILDREN as relation>
   <tr>
    <td>
     <b>${DATE.show(relation.child.created,"CZ_FULL")} ${TOOL.xpath(relation.child,"data/author")}</b>
     <br>
     ${TOOL.render(TOOL.element(relation.child.data,"data/text"),USER?if_exists)}
     <br>
     <a href="${URL.make("/EditRequest?action=email&requestId="+relation.id)}">Poslat email</a>,
     <a href="${URL.make("/EditRequest?action=deliver&requestId="+relation.id)}">Vyøízeno</a>,
     <a href="${URL.make("/EditRequest?action=delete&requestId="+relation.id)}">Smazat</a>
     <a href="${URL.make("/EditRequest?action=todo&requestId="+relation.id)}">Pøesunout do TODO</a>
    </td>
   </tr>
  </#list>
 </table>
</#if>
