<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>Prohlédnìte si vzhled va¹eho komentáøe. Zkontrolujte
 pravopis, obsah i tón va¹eho textu. Nìkdy to vá¾nì
 chce chladnou hlavu. Opravte chyby a zvolte náhled.
 Pokud jste s pøíspìvkem spokojeni, zvolte OK.</p>
</#if>

<#if ! USER?exists>
 <p>Nejste pøihlá¹en. Nový úèet mù¾ete zalo¾it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <#if ! PREVIEW?exists>
     <h1>Pøíspìvek na který reagujete</h1>
     <@lib.showComment THREAD, 0, 0, false />
 </#if>

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

<h1>Vá¹ komentáø</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <p>
    <span class="required">Login a heslo</span>
     <input tabindex="1" type="text" name="LOGIN" size="8">
     <input tabindex="2" type="password" name="PASSWORD" size="8">
   </p>
   <p>
    <span class="required">nebo va¹e jméno</span>
     <input tabindex="3" type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}">
     <div class="error">${ERRORS.author?if_exists}</div>
   </p>
  </#if>
  <p>
   <span class="required">Titulek</span><br>
   <#if PARAMS.title?exists>
    <#assign title=PARAMS.title>
   <#elseif THREAD?exists>
    <#assign title=TOOL.xpath(THREAD.data,"title")>
    <#if !title.startsWith("Re: ")><#assign title="Re: "+title></#if>
   </#if>
   <input tabindex="4" type="text" name="title" size="60" maxlength="70" value="${title?if_exists?html}">
   <div class="error">${ERRORS.title?if_exists}</div>
  </p>
  <p>
    <span class="required">Vá¹ komentáø</span>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.replyForm.text, '<b>', '</b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<i>', '</i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<p>', '</p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<pre>', '</pre>');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
        <#if THREAD?exists>
            <a href="javascript:cituj(document.replyForm.text);" id="mono" title="Vlo¾í komentovaný pøíspìvek jako citaci">Citace</a>
        </#if>
    </div>
   <div class="error">${ERRORS.text?if_exists}</div>
   <textarea tabindex="5" name="text" cols="70" rows="20">${PARAMS.text?if_exists?html}</textarea>
  </p>
  <p>
    <#if PREVIEW?exists>
     <input tabindex="6" type="submit" name="preview" value="Zopakuj náhled">
     <input tabindex="7" type="submit" name="finish" value="Dokonèi">
    <#else>
     <input tabindex="6" type="submit" name="preview" value="Náhled">
    </#if>
  </p>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${DISCUSSION.id}">
 <#if THREAD?exists>
  <input type="hidden" name="threadId" value="${THREAD.id}">
 </#if>
 <#if PARAMS.url?exists>
  <input type="hidden" name="url" value="${PARAMS.url}">
 </#if>
</form>

<h1>Nápovìda k formátování</h1>

<p>Povolené HTML <a href="http://www.w3.org/TR/html4/index/elements.html">znaèky</a>:
P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
VAR, HR a IT.</p>

<p>Nejrychlej¹í zpùsob formátování je rozdìlovat
text do odstavcù. Systém detekuje prázdné øádky
(dvakrát enter) a nahradí je HTML znaèkou odstavce.
Pokud ale v textu pou¾ijete znaèku P èi BR,
pak pøedpokládáme, ¾e o formátování se budete starat
sami a tato konverze nebude aktivována.</p>

<p>Pokud neovládáte HTML, doporuèuji si pøeèíst jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text musí být HTML validní, proto znak men¹ítka èi vìt¹ítka zapisujte takto:
&lt; jako &amp;lt; a &gt; jako &amp;gt;. Dal¹ím èastým problémem
je, jak vlo¾it výpis logu èi konfiguraèní soubor. V tomto
pøípadì vá¹ text vlo¾te mezi znaèky PRE, pøíli¹ dlouhé øádky rozdìlte klávesou enter.</p>


<#include "../footer.ftl">
