<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Ne� polo��te dotaz</h1>

<ul>
    <li>P�e�t�te si �l�nek na t�ma, <a href="/clanky/show/4006">jak �e�it probl�my</a>.</li>
    <li>Zkusili jste hled�n� a pro�li jste si <a href="/faq">�asto kladen� ot�zky</a> (FAQ)?</li>
    <li>Pokud m�te probl�m s MS&nbsp;Windows a nen� zde p���inn� souvislost
        s Linuxem, obra�te se na Microsoft, tady jste na �patn� adrese.</li>
    <li>Dotazy na warez budou smaz�ny.</li>
</ul>

<h1 class="st_nadpis">Jak ps�t titulek</h1>

<p>Dob�e zvolen� titulek p�il�k� pozornost �ten���, �patn� zaru�en� odrad� zku�en� u�ivatele, kte��
by v�m mohli pomoci. Admini mohou �patn� formulovan� titulek upravit.</p>

<ul>
<li>Popi�te v�tou, v �em p�esn� spo��v� v� probl�m.
<li>Nepou��vejte slova jako help, pomoc, pros�m, za��te�n�k �i lama.
<li>NEPI�TE VELK�MI P�SMENY, nepou��vejte vyk�i�n�ky, je to nezdvo�il�.
</ul>

<h1 class="st_nadpis">Jak popsat probl�m</h1>

<p>Sna�te se uv�st co nejv�ce relevantn�ch informac�,
jako je druh hardwaru �i verze aplikace. Ostatn� �ten��i nemaj� k�i���lovou kouli,
aby to sami uh�dli. Popi�te postup, kter� nevede k c�li.
�asto je dobr� vlo�it uk�zku konfigura�n�ho souboru, v�pis
dmesg �i lspci (HTML zna�ka PRE). Nicm�n� vkl�dejte jen skute�n�
zaj�mav� ��sti souvisej�c� s probl�mem, maxim�ln� deset a�
patn�ct ��dek. Pokud p�ijdete na �e�en� sami, vlo�te jej do diskuse.
Pom��ete tak ostatn�m �ten���m.
</p>

<#if ! USER?exists>
 <h1 class="st_nadpis">Pro� se p�ihl�sit</h1>

 <p>Registrovan� �ten��i si mohou nechat sledovat diskusi, kter� za�le emailem
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
