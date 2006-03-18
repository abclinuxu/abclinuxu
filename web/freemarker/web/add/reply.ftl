<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>
    Prohlédnìte si vzhled va¹eho komentáøe. Zkontrolujte
    pravopis, obsah i tón va¹eho textu. Nìkdy to vá¾nì
    chce chladnou hlavu. Opravte chyby a zvolte tlaèítko <code>Náhled</code>.
    Pokud jste s pøíspìvkem spokojeni, stisknìte tlaèítko <code>Dokonèi</code>.
 </p>
</#if>

<#if ! USER?exists>
 <p>Nejste pøihlá¹en. Nový úèet mù¾ete zalo¾it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <h1>Pøíspìvek, na který reagujete</h1>
 <@lib.showThread THREAD, 0, TOOL.createEmptyDiscussion(), false />

 <script language="javascript1.2" type="text/javascript">
    original = "${TOOL.xpath(THREAD.data,"//text")?js_string}";
    original = "<blockquote>"+original+"</blockquote>";
    function cituj(input) {
      input.value += original;
    }
 </script>
</#if>

<#if PREVIEW?exists>
 <h1>Náhled va¹eho pøíspìvku</h1>
 <div style="padding-left: 30pt">
  <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
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
    <#elseif PARENT_TITLE?exists>
        <#assign title=PARENT_TITLE>
        <#assign title="Re: "+title>
    <#elseif THREAD?exists>
        <#assign title=THREAD.title?if_exists>
        <#if !title.startsWith("Re: ")><#assign title="Re: "+title></#if>
    </#if>
   <input tabindex="4" type="text" name="title" size="60" maxlength="70" value="${title?if_exists?html}">
   <div class="error">${ERRORS.title?if_exists}</div>
  </p>
  <p>
    <span class="required">Vá¹ komentáø</span>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&amp;lt;', '');" id="mono" title="Vlo¾it písmeno &lt;">&lt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&amp;gt;', '');" id="mono" title="Vlo¾it písmeno &gt;">&gt;</a>
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

<p>Povolené HTML <a href="http://www.w3.org/TR/html4/index/elements.html" rel="nofollow">znaèky</a>:
A, B, BR, BLOCKQUOTE, CITE, CODE, DEL, DIV, EM, I, INS, HR, H1, H2, H3, LI,
OL, P, PRE, STRONG, TT, UL, VAR. </p>

<p>Nejrychlej¹í zpùsob formátování je rozdìlovat
text do odstavcù. Systém detekuje prázdné øádky
(dvakrát enter) a nahradí je HTML znaèkou odstavce.
Pokud ale v textu pou¾ijete znaèku P èi BR,
pak pøedpokládáme, ¾e o formátování se budete starat
sami a tato konverze nebude aktivována.</p>

<p>Pokud neovládáte HTML, doporuèuji si pøeèíst jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text musí být HTML validní, proto znak men¹ítka èi vìt¹ítka zapisujte takto:
<code>&lt;</code> jako <code>&amp;lt;</code> a <code>&gt;</code> jako <code>&amp;gt;</code>.
Dal¹ím èastým problémem je, jak vlo¾it výpis logu èi konfiguraèní soubor. V tomto
pøípadì vá¹ text vlo¾te mezi znaèky PRE, pøíli¹ dlouhé øádky rozdìlte klávesou enter.</p>


<#include "../footer.ftl">
