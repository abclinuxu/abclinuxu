<#include "../header.ftl">

<@lib.showMessages/>

<h1>Zm�na</h1>

<p>V diskusn�m foru se objevuje p��li� hodn� dotaz�, kter� ji� byly
mnohokr�t polo�eny a zodpov�zeny. Tyto dotazy sv�d�� o tom, �e
jejich auto�i jsou l�n� prov�st jednoduch� fulltextov� hled�n�,
kter� by jim ihned zobrazilo hledan� informace. Lid�, kte�� odpov�daj�
v diskusn�m foru, jsou pak opr�vn�n� na�tvan�, �e tito jedinci
nevyvinuli ani nejmen�� osobn� iniciativu. Proto jsme se rozhodli
p�istoupit ke zm�n� a nastavit hled�n� jako povinn� krok v pokl�d�n�
ot�zky do diskusn�ho fora.</p>

<h1>Hled�n�</h1>

<p>Ne� m��ete polo�it sv�j dotaz, mus�te nejd��ve prohledat na�i datab�zi,
zda ji� neobsahuje odpov�� na va�i ot�zku. Datab�ze obsahuje v�ce ne�
6000 diskus� a n�kolik tis�c dal��ch dokument�, tak�e m�te velkou �anci,
�e zde najdete pot�ebn� informace. Pokud ne, na dal�� str�nce najdete
odkaz na formul��, kde budete moci polo�it ot�zku do diskusn�ho fora.</p>

<h1>Jak hledat</h1>

<p>Fulltextov� hled�n� pou��v� knihovnu <a href="http://jakarta.apache.org/lucene/">Jakarta Lucene</a>.
Zadejte hledanou fr�zi. K dispozici m�te logick�
operatory AND, OR, NOT (velk� p�smena), respektive
krat�� varianty + a -. Hv�zdi�ka funguje jako z�stupn�
znak. Plus a m�nus pat�� na za��tek hledan�ho v�razu,
um�st�n� na konci je chyba (proto dc++ nefunguje).
Hled�n� je nez�visl� na velikosti p�smen a diakritice.
<a href="/clanky/show/5024">V�ce informac�</a>.</p>

<h1>P��klady</h1>

<dl>
<dt>nvidia</dt>
<dd>Najde v�echny dokumenty s v�skytem �et�zce nvidia.</dd>
<dt>nvidia tnt</dt>
<dd>Najde v�echny dokumenty obsahuj�c� �et�zce nvidia nebo tnt.</dd>
<dt>nvidia OR tnt</dt>
<dd>Najde v�echny dokumenty obsahuj�c� �et�zce nvidia nebo tnt.</dd>
<dt>nvidia AND TNT</dt>
<dd>Najde v�echny dokumenty obsahuj�c� �et�zce nvidia a tnt.</dd>
<dt>+nvidia +tnt</dt>
<dd>Najde v�echny dokumenty obsahuj�c� �et�zce nvidia a tnt.</dd>
<dt>+nvidia -tnt</dt>
<dd>Najde v�echny dokumenty obsahuj�c� �et�zec nvidia a neobsahuj�c� �et�zec tnt.</dd>
<dt>nvidia NOT tnt</dt>
<dd>Najde v�echny dokumenty obsahuj�c� �et�zec nvidia a neobsahuj�c� �et�zec tnt.</dd>
</dl>

<form action="${URL.make("/EditDiscussion")}" method="POST">
  Hledan� �et�zec: <input type="text" name="query" size="50" tabindex="1">
  <input type="submit" value="Hledej" tabindex="2">  <br>
  <input type="hidden" name="action" value="addQuez2">
  <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "../footer.ftl">
