<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Upozorn�n�</h1>

<p>Nyn� si prohl�dn�te vzhled va�eho dotazu. Zkontrolujte
si pravopis, obsah i t�n va�eho textu. Uv�domte si, �e
toto nen� placen� technick� podpora, ale dobrovoln�
a neplacen� pr�ce ochotn�ch lid�. Pokud se v�m text n�jak nel�b�,
opravte jej a zvolte N�hled. Pokud jste s n�m spokojeni,
zvolte OK.</p>

<#if PREVIEW?exists>
 <h1 class="st_nadpis">N�hled va�eho dotazu</h1>
 <@lib.showThread PREVIEW, 0, 0, 0, false />
</#if>

<h1 class="st_nadpis">Zde m��ete prov�st sv� �pravy</h1>

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
    <td class="required">nebo va�e jm�no</td>
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
        <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
	<a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
    </div>
    <div class="error">${ERRORS.text?if_exists}</div>
    <textarea tabindex="5" name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input tabindex="6" type="submit" name="preview" value="Zopakuj n�hled">
    <input tabindex="7" type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<h1>N�pov�da k form�tov�n�</h1>

<p>Povolen� HTML <a href="http://www.w3.org/TR/html4/index/elements.html">zna�ky</a>:
P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
VAR, HR a IT.</p>

<p>Nejrychlej�� zp�sob form�tov�n� je rozd�lovat
text do odstavc�. Syst�m detekuje pr�zdn� ��dky
(dvakr�t enter) a nahrad� je HTML zna�kou odstavce.
Pokud ale v textu pou�ijete zna�ku P �i BR,
pak p�edpokl�d�me, �e o form�tov�n� se budete starat
sami a tato konverze nebude aktivov�na.</p>

<p>Pokud neovl�d�te HTML, doporu�uji si p�e��st jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text mus� b�t HTML validn�, proto znak men��tka �i v�t��tka zapisujte takto:
&lt; jako &amp;lt; a &gt; jako &amp;gt;. Dal��m �ast�m probl�mem
je, jak vlo�it v�pis logu �i konfigura�n� soubor. V tomto
p��pad� v� text vlo�te mezi zna�ky PRE, p��li� dlouh� ��dky rozd�lte kl�vesou enter.</p>


<#include "../footer.ftl">
