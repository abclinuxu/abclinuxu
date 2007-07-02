<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>Úprava dokumentu</h1>

<p>Pokud chcete vylepšit obsah dokumentu nebo opravit chybu, jste na
správné adrese. Všechny změny se automaticky ukládají do databáze, takže
je možné prohlížet obsah tohoto dokumentu v průběhu času nebo vrátit
změny zpět.</p>

<#if PREVIEW?exists>
    <fieldset>
        <legend>Náhled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

 <table class="siroka" border="0" cellpadding="5">
  <tr>
   <td width="90" class="required">Titulek stránky</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size=60 tabindex=1>
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Obsah stránky</td>
   <td>
    <p>Všechna URL na články, obrázky a soubory z našeho serveru musí být relativní!</p>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h1&gt;', '&lt;/h1&gt;');" id="mono" title="Vložit značku nadpisu">&lt;h1&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="mono" title="Vložit značku nadpisu">&lt;h2&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="mono" title="Vložit značku nadpisu">&lt;h3&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit formátovaný text. Vhodné pouze pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
    </div>
    <div class="error">${ERRORS.content?if_exists}</div>
    <textarea name="content" class="siroka" rows="30" tabindex="2">${PARAMS.content?if_exists?html}</textarea>
   </td>
  </tr>
  <tr>
    <td>
        Popis změny
        <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
    </td>
   <td>
    <input tabindex="3" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists}">
    <div class="error">${ERRORS.rev_descr?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td>
     <input tabindex="4" type="submit" name="preview" value="<#if PREVIEW?exists>Zopakuj náhled<#else>Náhled</#if>">
     <input tabindex="5" type="submit" name="finish" value="Dokonči">
   </td>
  </tr>
 </table>

 <input type="hidden" name="action" value="editPublicContent2">
 <input type="hidden" name="rid" value="${PARAMS.rid?if_exists}">
  <#if PARAMS.startTime?exists><#assign value=PARAMS.startTime><#else><#assign value=START_TIME?c></#if>
  <input type="hidden" name="startTime" value="${value}">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
