<#include "../header.ftl">
<@lib.showMessages/>

<h1>Administrativn� po�adavky</h1>

<p>Na tomto m�st� najdete seznam <b>administrativn�ch</b> po�adavk�
na spr�vce serveru. Pokud m�te n�jak� probl�m s port�lem www.abclinuxu.cz,
pot�ebujete zalo�it novou sekci, nebo jste zapomn�li p�ihla�ovac� �daje,
pou�ijte n�e uveden� formul��.</p>

<p>N�m�ty na nov� slu�by, vylep�en� sou�asn�ch slu�eb nebo nalezen�
chyby zas�lejte autorovi port�lu <a href="/Profile/1">Leo�i Liter�kovi</a>.
</p>

<h1>Toto nen� technick� podpora pro Linux!</h1>

<p>Neple�te si tento formul�� s technickou podporou pro Linux!
Tento formul�� se opravdu t�k� jen po�adavk� na <b>chod port�lu</b>
www.abclinuxu.cz. Ostatn� ot�zky budou bez odpov�di smaz�ny.</p>

</p>Pot�ebujete-li poradit s Linuxem, vyzkou�ejte na�e
mocn� vyhled�v�n� a nenajdete-li �e�en�, po��dejte o pomoc
v <a href="/diskuse.jsp">diskusn�m f�ru.</a>
</p>

<form action="${URL.make("/EditRequest")}" method="POST">
 <table border=0 cellpadding=5 style="padding-top: 10px">
  <tr>
   <td class="required">Va�e jm�no</td>
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
    <td class="required">V� email</td>
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
   <td class="required">Po�adavek</td>
   <td>
    <textarea name="text" cols="60" rows="15" tabindex="3">
${PARAMS.text?default("Sem pat�� jen po�adavky na administr�tory port�lu www.abclinuxu.cz, ot�zky kolem Linuxu zad�vejte do diskusn�ho f�ra!")?html}
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
   <th>Nevy��zen� po�adavky</th>
  </tr>
  <#list CHILDREN as relation>
   <tr>
    <td>
     <b>${DATE.show(relation.child.created,"CZ_FULL")} ${TOOL.xpath(relation.child,"data/author")}</b>
     <br>
     ${TOOL.render(TOOL.element(relation.child.data,"data/text"),USER?if_exists)}
     <br>
     <a href="${URL.make("/EditRequest?action=email&requestId="+relation.id)}">Poslat email</a>,
     <a href="${URL.make("/EditRequest?action=deliver&requestId="+relation.id)}">Vy��zeno</a>,
     <a href="${URL.make("/EditRequest?action=delete&requestId="+relation.id)}">Smazat</a>
     <a href="${URL.make("/EditRequest?action=todo&requestId="+relation.id)}">P�esunout do TODO</a>
    </td>
   </tr>
  </#list>
 </table>
</#if>
