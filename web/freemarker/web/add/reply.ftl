<#include "../header.ftl">

<@lib.showMessages/>

<h1>Upozorn�n�</h1>

<#if PREVIEW?exists>
 <p>Nyn� si prohl�dn�te vzhled va�eho koment��e. Zkontrolujte
 si pravopis, obsah i t�n va�eho textu. N�kdy to v�n�
 chce klidnou hlavu. Pokud se v�m text n�jak nel�b�,
 opravte jej a zvolte n�hled. Pokud jste s n�m spokojeni,
 zvolte OK.</p>
<#else>
 <p>Chyst�te se vlo�it p��sp�vek do diskuse. Sna�te
se pros�m b�t p�esn� a slu�n�. Do textu m��ete
vkl�dat z�kladn� HTML zna�ky, pr�zdn� ��dek bude
automaticky konvertov�n na zna�ku nov�ho odstavce,
pokud se o form�tov�n� nestar�te sami.
Po odesl�n� si budete moci prohl�dnout, jak v�
p��sp�vek bude vypadat.</p>
</#if>

<#if ! USER?exists>
 <p>Nejste p�ihl�en. Nov� ��et m��ete zalo�it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <h1>P��sp�vek na kter� reagujete</h1>
 <@lib.showComment THREAD, 0, 0, false />
</#if>

<#if PREVIEW?exists>
 <h1>N�hled va�eho p��sp�vku</h1>
 <div style="padding-left: 30pt">
  <@lib.showComment PREVIEW, 0, 0, false />
 </div>
</#if>

<h1>Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table border="0" cellpadding="5">
  <#if ! USER?exists>
   <tr>
    <td class="required">Login a heslo</td>
    <td>
     <input type="text" name="LOGIN" size="8" class="pole">
     <input type="password" name="PASSWORD" size="8" class="pole">
    </td>
   </tr>
   <tr>
    <td class="required">nebo va�e jm�no</td>
    <td>
     <input type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}" class="pole">
     <div class="error">${ERRORS.author?if_exists}</div>
    </td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <#if PARAMS.title?exists>
    <#assign title=PARAMS.title>
   <#elseif THREAD?exists>
    <#assign title=TOOL.xpath(THREAD.data,"title")>
    <#if !title.startsWith("Re: ")><#assign title="Re: "+title></#if>
   </#if>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${title?if_exists?html}" class="pole">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">V� koment��</td>
   <td>
    <div>Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
    pr�zdn� ��dky budou nahrazeny nov�m odstavcem.</div>
    <div class="error">${ERRORS.text?if_exists}</div>
    <textarea name="text" cols="60" rows="20" class="pole">${PARAMS.text?if_exists?html}</textarea>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <#if PREVIEW?exists>
     <input type="submit" name="preview" value="Zopakuj n�hled" class="buton">
     <input type="submit" name="finish" value="Dokon�i" class="buton">
    <#else>
     <input type="submit" name="preview" value="N�hled" class="buton">
    </#if>
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${DISCUSSION.id}">
 <#if THREAD?exists>
  <input type="hidden" name="threadId" value="${THREAD.id}">
 </#if>
</form>


<#include "../footer.ftl">
