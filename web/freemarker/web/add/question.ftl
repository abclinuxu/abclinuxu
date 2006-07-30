<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pokl�d�n� dotazu</h1>

<ul>
    <li>P�e�t�te si �l�nek
        <a href="/clanky/navody/jak-resit-problemy">Jak �e�it probl�my</a>.</li>
    <li>Zkusili jste <a href="/Search?advancedMode=true" title="Vyhled�v�n�">hled�n�</a>
        a pro�li jste si <a href="/faq" title="FAQ Linux">�asto kladen� ot�zky</a> (FAQ)?</li>
    <li>Pokud m�te probl�m s MS&nbsp;Windows a nen� zde p���inn� souvislost
        s Linuxem, obra�te se na Microsoft, tady jste na �patn� adrese.</li>
    <li>Dotazy na warez budou smaz�ny.</li>
</ul>

<h2>Jak ps�t titulek</h2>

<p>Dob�e zvolen� titulek p�il�k� pozornost �ten���, �patn� zaru�en� odrad� zku�en� u�ivatele, kte��
by v�m mohli pomoci. Admini mohou �patn� formulovan� titulek upravit.</p>

<ul>
<li>Popi�te v�tou, v �em p�esn� spo��v� v� probl�m.</li>
<li><b>Nepou��vejte</b> slova jako <i>help</i>, <i>pomoc</i>, <i>pora�te</i>, <i>pros�m</i>,
    <i>za��te�n�k</i> �i <i>lama</i>.</li>
<li>NEPI�TE VELK�MI P�SMENY, nepou��vejte vyk�i�n�ky, je to nezdvo�il�.</li>
<li>Jeden otazn�k �pln� sta��. Opravdu.</li>
</ul>

<h2>Jak popsat probl�m</h2>

<ul>
<li>Sna�te se uv�st co nejv�ce relevantn�ch informac�. Nap��klad:
    <ul>
    <li>druh hardwaru</li>
    <li>verze aplikace</li>
    </ul>
(Ostatn� �ten��i nemaj� k�i���lovou kouli, aby to sami uh�dli.)</li>

<li>Popi�te postup, kter� nevede k c�li. Uve�te, jestli jste postupovali podle n�jak�ho n�vodu.
Pokud ano, vlo�te na n�j odkaz.</li>

<li>�asto je dobr� vlo�it uk�zku konfigura�n�ho souboru, v�pis
<code>dmesg</code> �i <code>lspci</code> (HTML zna�ka <code>&lt;PRE&gt;</code>). Nicm�n�
vkl�dejte jen skute�n� zaj�mav� ��sti souvisej�c� s probl�mem, maxim�ln� deset a�
patn�ct ��dek.</li>

<li>Pokud p�ijdete na �e�en� sami, vlo�te jej do diskuse.
Pom��ete tak ostatn�m �ten���m.</li>
</ul>

<p><b>Do jednoho dotazu nevkl�dejte v�ce probl�m�</b>. Diskusi pak nen� mo�n� vhodn� za�adit do
p��slu�n�ho diskusn�ho f�ra a nen� mo�n� ji v�sti�n� pojmenovat. Pro u�ivatele,
kter� by pozd�ji hledal odpov�� na n�kter� z uveden�ch probl�m�, by bylo obt�n� takovou
diskusi vyhledat. Dotazy obsahuj�c� v�ce probl�m� mohou b�t administr�tory uzam�eny, p�i�em�
tazatel bude po��d�n, aby jednotliv� probl�my popsal v samostatn�ch diskus�ch.</p>

<#if ! USER?exists>
 <h1 class="st_nadpis">Pro� se p�ihl�sit</h1>

 <p>Registrovan� �ten��i si mohou nechat sledovat diskusi, tak�e jim budou emailem pos�l�ny
 reakce ostatn�ch �ten���. Z�rove� si budete moci ve sv�m profilu snadno vyhledat
 tuto diskusi. Proto je v�hodn� se p�ihl�sit. Nem�te-li u n�s je�t� ��et,
 <a href="${URL.noPrefix("/EditUser?action=add")}">zaregistrujte se</a>.
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

<#include "/include/napoveda-k-auto-formatovani.txt">


<#include "../footer.ftl">
