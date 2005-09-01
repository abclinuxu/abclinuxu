<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>Úprava dokumentu</h1>

<p>Pokud chcete vylep¹it obsah dokumentu nebo opravit chybu, jste na
správné adrese. V¹echny zmìny se automaticky ukládají do databáze, tak¾e
je mo¾né prohlí¾et obsah tohoto dokumentu v prùbìhu èasu nebo vrátit
zmìny zpìt.</p>

<#if PREVIEW?exists>
    <fieldset>
        <legend>Náhled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

 <table width=100 border=0 cellpadding=5>
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
    <p>V¹echna URL na èlánky, obrázky a soubory z na¹eho serveru musí být relativní!</p>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h1&gt;', '&lt;/h1&gt;');" id="mono" title="Vlo¾it znaèku nadpisu">&lt;h1&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="mono" title="Vlo¾it znaèku nadpisu">&lt;h2&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="mono" title="Vlo¾it znaèku nadpisu">&lt;h3&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo¾it formátovaný text. Vhodné pouze pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
    </div>
    <textarea name="content" cols="100" rows="30" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td>
     <input tabindex="8" type="submit" name="preview" value="<#if PREVIEW?exists>Zopakuj náhled<#else>Náhled</#if>">
     <input tabindex="9" type="submit" name="finish" value="Dokonèi">
   </td>
  </tr>
 </table>

 <input type="hidden" name="action" value="editPublicContent2">
 <input type="hidden" name="rid" value="${PARAMS.rid?if_exists}">
</form>

<p>Povolené HTML <a href="http://www.w3.org/TR/html4/index/elements.html">znaèky</a>:
 A,  B, BLOCKQUOTE, BR, CENTER, CITE, CODE, DD, DEL, DIV, DL, DT, EM, IMG, H1, H2, H3, H4, HR, I,
 INS, KBD, LI, OL, P, PRE, Q, SMALL, SPAN, STRONG, SUB, SUP, TABLE, TBODY, TD, TFOOT, TH, THEAD,
 TR, TT, U, UL, VAR. Znaèky P, PRE, DIV, SPAN, H1-H4 a A povolují atrubity ID a CLASS.
</p>


<#include "../footer.ftl">
