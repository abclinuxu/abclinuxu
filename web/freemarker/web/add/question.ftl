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

<h1 class="st_nadpis">Ne¾ polo¾íte dotaz</h1>

<p>Pøeètìte si èlánek na téma, <a href="/clanky/show/4006">jak øe¹it
problémy</a>.</p>

<h1 class="st_nadpis">Jak psát titulek</h1>

<ol>
<li>Nìkolika slovy popi¹te svùj problém.
<li>Nepou¾ívejte slova jako help, pomoc èi zaèáteèník.
<li>NEPI©TE VELKÝMI PÍSMENY, je to neslu¹né (jako byste køièeli).
<li>Nepou¾ívejte vykøièníky.
<li>Nepokládejte více nesouvisejících dotazù.
</ol>

<p>Administrátoøi mohou ¹patnì formulovaný titulek
opravit!</p>

<h1 class="st_nadpis">Jak popsat problém</h1>

<p>Sna¾te se uvést co nejvíce relevantních informací,
jako je verze distribuce, druh hardwaru èi verze
knihoven. Napi¹te také vá¹ postup, který nevede k cíli.
</p>

<p>Èasto je dobré vlo¾it ukázku konfiguraèního souboru, výpis
dmesg èi lspci. Nicménì buïte opatrní a vkládejte jen skuteènì
zajímavé èásti související s problémem, maximálnì deset a¾
patnáct øádek. Vìt¹í soubory umístìte nìkam na internet
a do diskuse vlo¾te jen URL. Pokud toto pravidlo nebudete
respektovat, administrátoøi mohou vá¹ pøíspìvek upravit èi
dokonce smazat.
</p>

<p>Ve chvíli, kdy pøijdete na øe¹ení, vlo¾te jej do diskuse
jako odpovìï. Pomù¾ete tak dal¹ím náv¹tìvníkùm, kteøí budou
v budoucnu èíst vá¹ dotaz.
</p>

<#if ! USER?exists>
 <h1 class="st_nadpis">Proè se pøihlásit</h1>

 <p>Pokud jste na abclinuxu registrováni, je výhodné
 se nyní pøihlásit. Vá¹ dotaz mù¾ete nechat sledovat
 Monitorem, který vám za¹le emailem upozornìní, kdy¾
 nìkdo na vá¹ dotaz bude reagovat. A¾ jednou budete
 diskusi znovu potøebovat, ve svém profilu ji velice
 snadno najdete. Nový úèet mù¾ete zalo¾it
 <a href="${URL.noPrefix("/EditUser?action=add")}">zde</a>.
 </p>
</#if>

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
    <td><input type="text" size="30" name="author"></td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70">
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
    <textarea name="text" cols="60" rows="20"></textarea><br>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td><input type="submit" name="preview" value="Náhled"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
