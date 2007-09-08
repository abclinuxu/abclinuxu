<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>
    Prohlédněte si vzhled vašeho komentáře. Zkontrolujte
    pravopis, obsah i tón vašeho textu. Někdy to vážně
    chce chladnou hlavu. Opravte chyby a zvolte tlačítko <code>Náhled</code>.
    Pokud jste s příspěvkem spokojeni, stiskněte tlačítko <code>Dokonči</code>.
 </p>
</#if>

<#if THREAD?exists>
 <h2>Příspěvek, na který reagujete</h2>
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
 <h2>Náhled vašeho příspěvku</h2>
 <div style="padding-left: 30pt">
  <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
 </div>
</#if>

<h2>Váš komentář</h2>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <p>
    <span class="required">Login a heslo</span>
     <input tabindex="1" type="text" name="LOGIN" size="8">
     <input tabindex="2" type="password" name="PASSWORD" size="8">
     <span class="error">${ERRORS.LOGIN?if_exists}</span>
   </p>
   <p>
    <span class="required">nebo vaše jméno</span>
        <input tabindex="3" type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}">
    <span class="error">${ERRORS.author?if_exists}</span>
   </p>
   <#if ! USER_VERIFIED?if_exists>
       <p>
           <span class="required">Zadejte aktuální rok</span>
           <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}">
           <a class="info" href="#">?<span class="tooltip">Vložte aktuální rok. Jedná se o ochranu před spamboty.
           Po úspěšném ověření se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.</span></a>
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
    <span class="required">Váš komentář</span>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;blockquote&gt;', '&lt;/blockquote&gt;');" id="mono" title="Vložit značku citace">BQ</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
	    <a href="javascript:insertAtCursor(document.replyForm.text, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
        <#if THREAD?exists>
            <a href="javascript:cituj(document.replyForm.text);" id="mono" title="Vloží komentovaný příspěvek jako citaci">Citace</a>
        </#if>
    </div>
   <div class="error">${ERRORS.text?if_exists}</div>
   <textarea tabindex="5" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea>
  </p>
  <p>
    <#if PREVIEW?exists>
     <input tabindex="6" type="submit" name="preview" value="Zopakuj náhled">
     <input tabindex="7" type="submit" name="finish" value="Dokonči">
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

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
