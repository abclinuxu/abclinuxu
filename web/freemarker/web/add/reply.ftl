<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>Nyní si prohlédnìte vzhled va¹eho komentáøe. Zkontrolujte
 si pravopis, obsah i tón va¹eho textu. Nìkdy to vá¾nì
 chce klidnou hlavu. Pokud se vám text nìjak nelíbí,
 opravte jej a zvolte náhled. Pokud jste s ním spokojeni,
 zvolte OK.</p>
<#else>
 <!--p>Chystáte se vlo¾it pøíspìvek do diskuse. Sna¾te
se prosím být pøesní a slu¹ní. Do textu mù¾ete
vkládat základní HTML znaèky, prázdný øádek bude
automaticky konvertován na znaèku nového odstavce,
pokud se o formátování nestaráte sami.
Po odeslání si budete moci prohlédnout, jak vá¹
pøíspìvek bude vypadat.</p-->
</#if>

<#if ! USER?exists>
 <p>Nejste pøihlá¹en. Nový úèet mù¾ete zalo¾it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <h1>Pøíspìvek na který reagujete</h1>
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
 <h1>Náhled va¹eho pøíspìvku</h1>
 <div style="padding-left: 30pt">
  <@lib.showComment PREVIEW, 0, 0, false />
 </div>
</#if>

<h1>Zde mù¾ete provést své úpravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <div>
    <span class="required">Login a heslo</span>
     <input type="text" name="LOGIN" size="8">
     <input type="password" name="PASSWORD" size="8">
   </div>
   <div>
    <span class="required">nebo va¹e jméno</span>
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
   <span class="required">Vá¹ komentáø</span>
   <#if THREAD?exists>
    <input type="button" value="Cituj" onClick="cituj(document.replyForm.text)">
   </#if>
   <div class="error">${ERRORS.text?if_exists}</div>
   <textarea name="text" cols="70" rows="20">${PARAMS.text?if_exists?html}</textarea>
  </div>

  <#if PREVIEW?exists>
   <input type="submit" name="preview" value="Zopakuj náhled">
   <input type="submit" name="finish" value="Dokonèi">
  <#else>
   <input type="submit" name="preview" value="Náhled">
  </#if>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${DISCUSSION.id}">
 <#if THREAD?exists>
  <input type="hidden" name="threadId" value="${THREAD.id}">
 </#if>
</form>


<#include "../footer.ftl">
