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

<#if THREAD?exists>
 <h2>P��sp�vek, na kter� reagujete</h2>
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
 <h2>N�hled va�eho p��sp�vku</h2>
 <div style="padding-left: 30pt">
  <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
 </div>
</#if>

<h2>V� koment��</h2>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <p>
    <span class="required">Login a heslo</span>
     <input tabindex="1" type="text" name="LOGIN" size="8">
     <input tabindex="2" type="password" name="PASSWORD" size="8">
     <span class="error">${ERRORS.LOGIN?if_exists}</span>
   </p>
   <p>
    <span class="required">nebo va�e jm�no</span>
        <input tabindex="3" type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}">
    <span class="error">${ERRORS.author?if_exists}</span>
   </p>
   <#if ! USER_VERIFIED?if_exists>
       <p>
           <span class="required">Zadejte aktu�ln� rok</span>
           <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}">
           <a class="info" href="#">?<span class="tooltip">Vlo�te aktu�ln� rok. Jedn� se o ochranu p�ed spamboty.
           Po �sp�n�m ov��en� se ulo�� cookie (v�etn� va�eho jm�na) a tato kontrola p�estane b�t prov�d�na.</span></a>
           <span class="error">${ERRORS.antispam?if_exists}</span>
       </p>
   </#if>
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
   <textarea tabindex="5" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea>
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

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
