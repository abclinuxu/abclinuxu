<#assign plovouci_sloupec>
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>N�pov�da k form�tov�n�</h1>
    </div></div>

    <p>
        Existuj� dva zp�soby, jak form�tovat va�e p��sp�vky.
        Prvn� z nich je psan� textu podobn�, jako kdy� p�ete
        dopis. Rozd�lujte va�e texty do odstavc�, pokud vlo��te
        pr�zdn� ��dek, syst�m automaticky bude zachov�vat toto
        form�tov�n�. Konce ��dk� nemaj� ��dn� speci�ln� v�znam.
        P�i tomto zp�sobu je pou��t i HTML zna�ky s vyj�mkou zna�ky
        nov�ho ��dku a odstavce. jejich pou�it� automaticky
        p�epne do HTML modu, kde jste pln� odpov�dni za form�tov�n�.
    </p>

    <p>
        Druh� zp�sob v�m d�v� v�t�� volnost p�i form�tov�n�.
        M�te k dispozici relativn� velkou sadu HTML zna�ek.
        Pro za��te�n�ky mohu doporu�it star��
        <a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>
        tohoto form�tovac�ho jazyka. Z r�zn�ch d�vod� jsou
        povoleny jen tyto <a href="http://www.w3.org/TR/html4/index/elements.html">zna�ky</a>:
        P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
        VAR, HR a IT.
    </p>

    <p>
        Pro oba styly plat�, �e text mus� b�t HTML validn�. �ast�m
        probl�mem je, �e n�kdo se sna�� vlo�it text obsahuj�c�
        znak men��tka �i v�t��tka. Tyto znaky se zapisuj� n�sledovn�:
        &lt; jako &amp;lt;,  &gt; jako &amp;gt;. Dal��m �ast�m probl�mem
        je, jak vlo�it v�pis logu �i konfigura�n� soubor. V tomto
        p��pad� pou�ijte zna�ku PRE a v� text vlo�te mezi zna�ky,
        p��li� dlouh� ��dky rozd�lte znakem enter.
    </p>
</#assign>

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
 <@lib.showComment PREVIEW, 0, 0, false />
</#if>

<h1 class="st_nadpis">Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form">
 <table cellpadding="5">
  <#if ! USER?exists>
   <tr>
    <td class="required">Login a heslo</td>
    <td>
     <input type="text" name="LOGIN" size="8">
     <input type="password" name="PASSWORD" size="8">
    </td>
   </tr>
   <tr>
    <td class="required">nebo va�e jm�no</td>
    <td>
     <input type="text" size="30" name="author" value="${PARAMS.author?if_exists}">
     <div class="error">${ERRORS.author?if_exists}</div>
    </td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists?html}">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Dotaz</td>
   <td>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.text, '<b></b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i></i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;></a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<p></p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre></pre>');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
    </div>
    <textarea name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj n�hled">
    <input type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
