<#include "../header.ftl">

<@lib.showMessages/>

<h1>Ne¾ polo¾íte dotaz</h1>

<p>Pøeètìte si èlánek na téma, <a href="/clanky/show/4006">jak øe¹it
problémy</a>.</p>

<h1>Jak psát titulek</h1>

<ol>
<li>Nìkolika slovy popi¹te svùj problém.
<li>Nepou¾ívejte slova jako help, please nebo pomoc.
<li>Nepi¹te VELKÝMI písmeny, je to neslu¹né (jako byste køièeli).
<li>Nepou¾ívejte vykøièníky.
<li>Neuvádìjte zbyteènì jméno va¹i distribuce.
<li>Nepokládejte více nesouvisejících dotazù.
</ol>

<h2>Pøíklady ¹patných titulkù</h2>

<ol>
 <li>je to mo¾né???
 <li>HELP PLEASE!!!
 <li>Redhat a modem Lucent
 <li>Pøipojení na internet; zvukovka CMI
</ol>

<p>Administrátoøi mohou ¹patnì formulovaný titulek
opravit!</p>

<h1>Jak popsat problém</h1>

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

<h1>Archivace starých diskusí</h1>

<p>Diskuse, které ji¾ skonèily, nebo na které bìhem dvou pracovních
dnù nepøi¹la ¾ádná nová odpovìï, jsou zpracovány na¹imi administrátory.
Pokud diskuse stojí za archivaci (napøíklad obsahuje øe¹ení,
podnìtné námìty èi informace), tak ji pøesunou z diskusního fóra
do nìkteré relevantní kategorie. Napøíklad dotazy o MPlayeru hledejte pod
Software/Multimédia. Ostatní dotazy budou smazány, stejnì jako ty,
které jsou duplicitní k ji¾ archivovaným diskusím.</p>

<p>Pokud nemù¾ete najít svou diskusi a jste si jisti, ¾e nebyla
z vý¹e uvedeného dùvodu smazána administrátorem, pøihla¹te se
a ve svém profilu zvolte pøíkaz <code>Mé diskuse</code>.
Dal¹í mo¾ností je pou¾ít fulltextové vyhledávání nebo procházení
v¹ech diskusí podle datumu polo¾ení dotazu.
</p>

<#if ! USER?exists>
 <p>Nejste pøihlá¹en, co¾ je ¹koda. Pøijdete tak o øadu výhod,
 napøíklad o snadné vyhledání tohoto dotazu ve svém profilu.
 Nový úèet mù¾ete zalo¾it
 <a href="${URL.noPrefix("/EditUser?action=add")}">zde</a>.
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
