<#assign plovouci_sloupec>
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Nápovìda k formátování</h1>
    </div></div>

    <p>
        Existují dva zpùsoby, jak formátovat va¹e pøíspìvky.
        První z nich je psaní textu podobnì, jako kdy¾ pí¹ete
        dopis. Rozdìlujte va¹e texty do odstavcù, pokud vlo¾íte
        prázdný øádek, systém automaticky bude zachovávat toto
        formátování. Konce øádkù nemají ¾ádný speciální význam.
        Pøi tomto zpùsobu je pou¾ít i HTML znaèky s vyjímkou znaèky
        nového øádku a odstavce. jejich pou¾ití automaticky
        pøepne do HTML modu, kde jste plnì odpovìdni za formátování.
    </p>

    <p>
        Druhý zpùsob vám dává vìt¹í volnost pøi formátování.
        Máte k dispozici relativnì velkou sadu HTML znaèek.
        Pro zaèáteèníky mohu doporuèit star¹í
        <a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>
        tohoto formátovacího jazyka. Z rùzných dùvodù jsou
        povoleny jen tyto <a href="http://www.w3.org/TR/html4/index/elements.html">znaèky</a>:
        P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
        VAR, HR a IT.
    </p>

    <p>
        Pro oba styly platí, ¾e text musí být HTML validní. Èastým
        problémem je, ¾e nìkdo se sna¾í vlo¾it text obsahující
        znak men¹ítka èi vìt¹ítka. Tyto znaky se zapisují následovnì:
        &lt; jako &amp;lt;,  &gt; jako &amp;gt;. Dal¹ím èastým problémem
        je, jak vlo¾it výpis logu èi konfiguraèní soubor. V tomto
        pøípadì pou¾ijte znaèku PRE a vá¹ text vlo¾te mezi znaèky,
        pøíli¹ dlouhé øádky rozdìlte znakem enter.
    </p>
</#assign>

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
 <@lib.showComment PREVIEW, 0, 0, false />
</#if>

<h1 class="st_nadpis">Zde mù¾ete provést své úpravy</h1>

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
    <td class="required">nebo va¹e jméno</td>
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
        <a href="javascript:insertAtCursor(document.form.text, '<b></b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i></i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;></a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<p></p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre></pre>');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
    </div>
    <textarea name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj náhled">
    <input type="submit" name="finish" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
