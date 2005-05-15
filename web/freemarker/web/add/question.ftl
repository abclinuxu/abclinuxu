<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Ne� polo��te dotaz</h1>

<p>P�e�t�te si �l�nek na t�ma, <a href="/clanky/show/4006">jak �e�it
probl�my</a>.</p>

<h1 class="st_nadpis">Jak ps�t titulek</h1>

<ol>
<li>N�kolika slovy popi�te sv�j probl�m.
<li>Nepou��vejte slova jako help, pomoc �i za��te�n�k.
<li>NEPI�TE VELK�MI P�SMENY, nepou��vejte vyk�i�n�ky.
<li>Nepokl�dejte v�ce nesouvisej�c�ch dotaz�.
</ol>

<p>Administr�to�i mohou �patn� formulovan� titulek
opravit.</p>

<p>Pro u�ivatele windows: jsme port�l o Linuxu, tak�e
dotazy na nefunk�nost Windows sem opravdu nepat��,
leda by zde byla jasn� souvislost probl�mu s Linuxem (nap��klad
samba).</p>

<h1 class="st_nadpis">Jak popsat probl�m</h1>

<p>Sna�te se uv�st co nejv�ce relevantn�ch informac�,
jako je verze distribuce, druh hardwaru �i verze
knihoven. Napi�te tak� v� postup, kter� nevede k c�li.
</p>

<p>�asto je dobr� vlo�it uk�zku konfigura�n�ho souboru, v�pis
dmesg �i lspci. Nicm�n� vkl�dejte jen skute�n�
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
 snadno najdete. Nov� ��et m��ete zalo�it
 <a href="${URL.noPrefix("/EditUser?action=add")}">zde</a>.
 </p>
</#if>

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
    <td><input tabindex="3" type="text" size="30" name="author"></td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input tabindex="4" type="text" name="title" size="40" maxlength="70">
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
    <textarea tabindex="5" name="text" cols="60" rows="20"></textarea><br>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td><input tabindex="6" type="submit" name="preview" value="N�hled"></td>
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
