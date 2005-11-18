<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Upozornìní</h1>

<p>Nyní si prohlédnìte vzhled va¹eho dotazu. Zkontrolujte
si pravopis, obsah i tón va¹eho textu. Uvìdomte si, ¾e
toto není placená technická podpora, ale dobrovolná
a neplacená práce ochotných lidí. Pokud se vám text nìjak nelíbí,
opravte jej a zvolte Náhled. Pokud jste s ním spokojeni,
zvolte OK.</p>

<#if PREVIEW?exists>
 <h1 class="st_nadpis">Náhled va¹eho dotazu</h1>
 <@lib.showThread PREVIEW, 0, 0, 0, false />
</#if>

<h1 class="st_nadpis">Zde mù¾ete provést své úpravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form">
 <table cellpadding="5">
  <#if ! USER?exists>
   <tr>
    <td class="required">Login a heslo</td>
    <td>
     <input tabindex="1" type="text" name="LOGIN" size="8">
     <input tabindex="2" type="password" name="PASSWORD" size="8">
    </td>
   </tr>
   <tr>
    <td class="required">nebo va¹e jméno</td>
    <td>
     <input tabindex="3" type="text" size="30" name="author" value="${PARAMS.author?if_exists}">
     <div class="error">${ERRORS.author?if_exists}</div>
    </td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input tabindex="4" type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists?html}">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Dotaz</td>
   <td>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
	<a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
    </div>
    <div class="error">${ERRORS.text?if_exists}</div>
    <textarea tabindex="5" name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input tabindex="6" type="submit" name="preview" value="Zopakuj náhled">
    <input tabindex="7" type="submit" name="finish" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
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
