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

<h2>Pøíklady ¹patných titulkù</h2>

<ol>
 <li>je to mo¾né???
 <li>HELP PLEASE!!!
 <li>Pøipojení na internet; zvukovka CMI
</ol>

<p>Administrátoøi mohou ¹patnì formulovaný titulek
opravit!</p>

<h1 class="st_nadpis">Jak popsat problém</h1>

<p>Sna¾te se uvést co nejvíce relevantních informací,
jako je verze distribuce, druh hardwaru èi verze
knihoven. Napi¹te také vá¹ postup, který nevede k cíli.
</p>

<dl>
 <dt>¹patnì</dt>
 <dd>nejede mi zvukovka</dd>
 <dt>správnì</dt>
 <dd>nedaøí se mi zprovoznit zvukovku Yamaha OPL3 pod
 RedHatem 7.3. pou¾ívám pøíkaz soundconfig</dd>
</dl>

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
 snadno najdete: pøíkaz <code>Mé diskuse</code>.
 Nový úèet mù¾ete zalo¾it
 <a href="${URL.noPrefix("/EditUser?action=add")}">zde</a>.
 </p>
 <p>Vá¹ dotaz najdete ve svém profilu pod pøíkazem
 <code>Mé diskuse</code>. Chcete-li být bezprostøednì
 informování o v¹ech reakcích, zapnìte si po ulo¾ení
 dotazu Monitor.
 </p>
</#if>

<form action="${URL.make("/EditDiscussion")}" method="POST">
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
    ®ádné HTML znaèky!
   </td>
  </tr>
  <tr>
   <td class="required">Dotaz<br>(jeden)</td>
   <td>
    <textarea name="text" cols="60" rows="20"></textarea><br>
    Smíte pou¾ívat základní HTML znaèky. Pokud je nepou¾ijete,
    prázdné øádky budou nahrazeny novým odstavcem.
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td><input type="submit" name="preview" value="Náhled"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez4">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
