<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Hled�n�</h1>

<p>Ne� m��ete polo�it sv�j dotaz, mus�te nejd��ve prohledat na�i datab�zi,
zda ji� neobsahuje odpov�� na va�i ot�zku. Datab�ze obsahuje v�ce ne�
6000 diskus� a n�kolik tis�c dal��ch dokument�, tak�e m�te velkou �anci,
�e zde najdete pot�ebn� informace. Pokud ne, na dal�� str�nce najdete
odkaz na formul��, kde budete moci polo�it ot�zku do diskusn�ho fora.</p>

<h1 class="st_nadpis">Jak hledat</h1>

<p>Zadejte hledanou fr�zi. K dispozici m�te logick�
operatory AND, OR, NOT (velk� p�smena), respektive
krat�� varianty + a -.  Plus a m�nus pat�� na za��tek hledan�ho v�razu,
um�st�n� na konci je chyba. Hv�zdi�ka funguje jako z�stupn�
znak. Hled�n� je nez�visl� na velikosti p�smen a diakritice.
<a href="/clanky/show/5024">V�ce informac�</a>.</p>

<h1 class="st_nadpis">P��klady</h1>

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
