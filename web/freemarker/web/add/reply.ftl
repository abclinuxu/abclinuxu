<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>Nyn� si prohl�dn�te vzhled va�eho koment��e. Zkontrolujte
 si pravopis, obsah i t�n va�eho textu. N�kdy to v�n�
 chce klidnou hlavu. Pokud se v�m text n�jak nel�b�,
 opravte jej a zvolte n�hled. Pokud jste s n�m spokojeni,
 zvolte OK.</p>
<#else>
 <!--p>Chyst�te se vlo�it p��sp�vek do diskuse. Sna�te
se pros�m b�t p�esn� a slu�n�. Do textu m��ete
vkl�dat z�kladn� HTML zna�ky, pr�zdn� ��dek bude
automaticky konvertov�n na zna�ku nov�ho odstavce,
pokud se o form�tov�n� nestar�te sami.
Po odesl�n� si budete moci prohl�dnout, jak v�
p��sp�vek bude vypadat.</p-->
</#if>

<#if ! USER?exists>
 <p>Nejste p�ihl�en. Nov� ��et m��ete zalo�it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <h1>P��sp�vek na kter� reagujete</h1>
 <@lib.showComment THREAD, 0, 0, false />

 <script language="javascript1.2" type="text/javascript">
    original = "${TOOL.xpath(THREAD.data,"text")?js_string}";
    original = "<blockquote>"+original+"</blockquote>";
    function cituj(input) {
      input.value += original;
    }
 </script>
</#if>

<#if PREVIEW?exists>
 <h1>N�hled va�eho p��sp�vku</h1>
 <div style="padding-left: 30pt">
  <@lib.showComment PREVIEW, 0, 0, false />
 </div>
</#if>

<h1>Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <div>
    <span class="required">Login a heslo</span>
     <input type="text" name="LOGIN" size="8">
     <input type="password" name="PASSWORD" size="8">
   </div>
   <div>
    <span class="required">nebo va�e jm�no</span>
     <input type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}">
     <div class="error">${ERRORS.author?if_exists}</div>
   </div>
  </#if>
  <div>
   <span class="required">Titulek</span><br>
   <#if PARAMS.title?exists>
    <#assign title=PARAMS.title>
   <#elseif THREAD?exists>
    <#assign title=TOOL.xpath(THREAD.data,"title")>
    <#if !title.startsWith("Re: ")><#assign title="Re: "+title></#if>
   </#if>
   <input type="text" name="title" size="60" maxlength="70" value="${title?if_exists?html}">
   <div class="error">${ERRORS.title?if_exists}</div>
  </div>
  <div>
   <span class="required">V� koment��</span>
   <#if THREAD?exists>
    <input type="button" value="Cituj" onClick="cituj(document.replyForm.text)">
   </#if>
   <div class="error">${ERRORS.text?if_exists}</div>
   <textarea name="text" cols="70" rows="20">${PARAMS.text?if_exists?html}</textarea>
  </div>

  <#if PREVIEW?exists>
   <input type="submit" name="preview" value="Zopakuj n�hled">
   <input type="submit" name="finish" value="Dokon�i">
  <#else>
   <input type="submit" name="preview" value="N�hled">
  </#if>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${DISCUSSION.id}">
 <#if THREAD?exists>
  <input type="hidden" name="threadId" value="${THREAD.id}">
 </#if>
</form>


<#include "../footer.ftl">
