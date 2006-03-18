<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>
    Prohl�dn�te si vzhled va�eho koment��e. Zkontrolujte
    pravopis, obsah i t�n va�eho textu. N�kdy to v�n�
    chce chladnou hlavu. Opravte chyby a zvolte tla��tko <code>N�hled</code>.
    Pokud jste s p��sp�vkem spokojeni, stiskn�te tla��tko <code>Dokon�i</code>.
 </p>
</#if>

<#if ! USER?exists>
 <p>Nejste p�ihl�en. Nov� ��et m��ete zalo�it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <h1>P��sp�vek, na kter� reagujete</h1>
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
 <h1>N�hled va�eho p��sp�vku</h1>
 <div style="padding-left: 30pt">
  <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
 </div>
</#if>

<h1>V� koment��</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <p>
    <span class="required">Login a heslo</span>
     <input tabindex="1" type="text" name="LOGIN" size="8">
     <input tabindex="2" type="password" name="PASSWORD" size="8">
   </p>
   <p>
    <span class="required">nebo va�e jm�no</span>
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
    <span class="required">V� koment��</span>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&amp;lt;', '');" id="mono" title="Vlo�it p�smeno &lt;">&lt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&amp;gt;', '');" id="mono" title="Vlo�it p�smeno &gt;">&gt;</a>
        <#if THREAD?exists>
            <a href="javascript:cituj(document.replyForm.text);" id="mono" title="Vlo�� komentovan� p��sp�vek jako citaci">Citace</a>
        </#if>
    </div>
   <div class="error">${ERRORS.text?if_exists}</div>
   <textarea tabindex="5" name="text" cols="70" rows="20">${PARAMS.text?if_exists?html}</textarea>
  </p>
  <p>
    <#if PREVIEW?exists>
     <input tabindex="6" type="submit" name="preview" value="Zopakuj n�hled">
     <input tabindex="7" type="submit" name="finish" value="Dokon�i">
    <#else>
     <input tabindex="6" type="submit" name="preview" value="N�hled">
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

<h1>N�pov�da k form�tov�n�</h1>

<p>Povolen� HTML <a href="http://www.w3.org/TR/html4/index/elements.html" rel="nofollow">zna�ky</a>:
A, B, BR, BLOCKQUOTE, CITE, CODE, DEL, DIV, EM, I, INS, HR, H1, H2, H3, LI,
OL, P, PRE, STRONG, TT, UL, VAR. </p>

<p>Nejrychlej�� zp�sob form�tov�n� je rozd�lovat
text do odstavc�. Syst�m detekuje pr�zdn� ��dky
(dvakr�t enter) a nahrad� je HTML zna�kou odstavce.
Pokud ale v textu pou�ijete zna�ku P �i BR,
pak p�edpokl�d�me, �e o form�tov�n� se budete starat
sami a tato konverze nebude aktivov�na.</p>

<p>Pokud neovl�d�te HTML, doporu�uji si p�e��st jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text mus� b�t HTML validn�, proto znak men��tka �i v�t��tka zapisujte takto:
<code>&lt;</code> jako <code>&amp;lt;</code> a <code>&gt;</code> jako <code>&amp;gt;</code>.
Dal��m �ast�m probl�mem je, jak vlo�it v�pis logu �i konfigura�n� soubor. V tomto
p��pad� v� text vlo�te mezi zna�ky PRE, p��li� dlouh� ��dky rozd�lte kl�vesou enter.</p>


<#include "../footer.ftl">
