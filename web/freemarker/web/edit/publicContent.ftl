<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>�prava dokumentu</h1>

<p>Pokud chcete vylep�it obsah dokumentu nebo opravit chybu, jste na
spr�vn� adrese. V�echny zm�ny se automaticky ukl�daj� do datab�ze, tak�e
je mo�n� prohl�et obsah tohoto dokumentu v pr�b�hu �asu nebo vr�tit
zm�ny zp�t.</p>

<#if PREVIEW?exists>
    <fieldset>
        <legend>N�hled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Titulek str�nky</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size=60 tabindex=1>
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Obsah str�nky</td>
   <td>
    <p>V�echna URL na �l�nky, obr�zky a soubory z na�eho serveru mus� b�t relativn�!</p>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h1&gt;', '&lt;/h1&gt;');" id="mono" title="Vlo�it zna�ku nadpisu">&lt;h1&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="mono" title="Vlo�it zna�ku nadpisu">&lt;h2&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="mono" title="Vlo�it zna�ku nadpisu">&lt;h3&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo�it form�tovan� text. Vhodn� pouze pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
    </div>
    <textarea name="content" cols="100" rows="30" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td>
     <input tabindex="8" type="submit" name="preview" value="<#if PREVIEW?exists>Zopakuj n�hled<#else>N�hled</#if>">
     <input tabindex="9" type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>

 <input type="hidden" name="action" value="editPublicContent2">
 <input type="hidden" name="rid" value="${PARAMS.rid?if_exists}">
</form>

<p>Povolen� HTML <a href="http://www.w3.org/TR/html4/index/elements.html">zna�ky</a>:
 A,  B, BLOCKQUOTE, BR, CENTER, CITE, CODE, DD, DEL, DIV, DL, DT, EM, IMG, H1, H2, H3, H4, HR, I,
 INS, KBD, LI, OL, P, PRE, Q, SMALL, SPAN, STRONG, SUB, SUP, TABLE, TBODY, TD, TFOOT, TH, THEAD,
 TR, TT, U, UL, VAR. Zna�ky P, PRE, DIV, SPAN, H1-H4 a A povoluj� atrubity ID a CLASS.
</p>


<#include "../footer.ftl">
