<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Ne� polo��te dotaz</h1>

<p>P�e�t�te si �l�nek na t�ma, <a href="/clanky/show/4006">jak �e�it
probl�my</a>.</p>

<h1 class="st_nadpis">Jak ps�t titulek</h1>

<ol>
<li>N�kolika slovy popi�te sv�j probl�m.
<li>Nepou��vejte slova jako help, pomoc �i za��te�n�k.
<li>NEPI�TE VELK�MI P�SMENY, je to neslu�n� (jako byste k�i�eli).
<li>Nepou��vejte vyk�i�n�ky.
<li>Nepokl�dejte v�ce nesouvisej�c�ch dotaz�.
</ol>

<h2>P��klady �patn�ch titulk�</h2>

<ol>
 <li>je to mo�n�???
 <li>HELP PLEASE!!!
 <li>P�ipojen� na internet; zvukovka CMI
</ol>

<p>Administr�to�i mohou �patn� formulovan� titulek
opravit!</p>

<h1 class="st_nadpis">Jak popsat probl�m</h1>

<p>Sna�te se uv�st co nejv�ce relevantn�ch informac�,
jako je verze distribuce, druh hardwaru �i verze
knihoven. Napi�te tak� v� postup, kter� nevede k c�li.
</p>

<dl>
 <dt>�patn�</dt>
 <dd>nejede mi zvukovka</dd>
 <dt>spr�vn�</dt>
 <dd>neda�� se mi zprovoznit zvukovku Yamaha OPL3 pod
 RedHatem 7.3. pou��v�m p��kaz soundconfig</dd>
</dl>

<p>�asto je dobr� vlo�it uk�zku konfigura�n�ho souboru, v�pis
dmesg �i lspci. Nicm�n� bu�te opatrn� a vkl�dejte jen skute�n�
zaj�mav� ��sti souvisej�c� s probl�mem, maxim�ln� deset a�
patn�ct ��dek. V�t�� soubory um�st�te n�kam na internet
a do diskuse vlo�te jen URL. Pokud toto pravidlo nebudete
respektovat, administr�to�i mohou v� p��sp�vek upravit �i
dokonce smazat.
</p>

<p>Ve chv�li, kdy p�ijdete na �e�en�, vlo�te jej do diskuse
jako odpov��. Pom��ete tak dal��m n�v�t�vn�k�m, kte�� budou
v budoucnu ��st v� dotaz.
</p>

<#if ! USER?exists>
 <h1 class="st_nadpis">Pro� se p�ihl�sit</h1>

 <p>Pokud jste na abclinuxu registrov�ni, je v�hodn�
 se nyn� p�ihl�sit. V� dotaz m��ete nechat sledovat
 Monitorem, kter� v�m za�le emailem upozorn�n�, kdy�
 n�kdo na v� dotaz bude reagovat. A� jednou budete
 diskusi znovu pot�ebovat, ve sv�m profilu ji velice
 snadno najdete: p��kaz <code>M� diskuse</code>.
 Nov� ��et m��ete zalo�it
 <a href="${URL.noPrefix("/EditUser?action=add")}">zde</a>.
 </p>
 <p>V� dotaz najdete ve sv�m profilu pod p��kazem
 <code>M� diskuse</code>. Chcete-li b�t bezprost�edn�
 informov�n� o v�ech reakc�ch, zapn�te si po ulo�en�
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
    <td class="required">nebo va�e jm�no</td>
    <td><input type="text" size="30" name="author"></td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70">
    ��dn� HTML zna�ky!
   </td>
  </tr>
  <tr>
   <td class="required">Dotaz<br>(jeden)</td>
   <td>
    <textarea name="text" cols="60" rows="20"></textarea><br>
    Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
    pr�zdn� ��dky budou nahrazeny nov�m odstavcem.
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td><input type="submit" name="preview" value="N�hled"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez4">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
