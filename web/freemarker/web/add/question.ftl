<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Hledání</h1>

<p>Ne¾ mù¾ete polo¾it svùj dotaz, musíte nejdøíve prohledat na¹i databázi,
zda ji¾ neobsahuje odpovìï na va¹i otázku. Databáze obsahuje více ne¾
6000 diskusí a nìkolik tisíc dal¹ích dokumentù, tak¾e máte velkou ¹anci,
¾e zde najdete potøebné informace. Pokud ne, na dal¹í stránce najdete
odkaz na formuláø, kde budete moci polo¾it otázku do diskusního fora.</p>

<h1 class="st_nadpis">Jak hledat</h1>

<p>Zadejte hledanou frázi. K dispozici máte logické
operatory AND, OR, NOT (velká písmena), respektive
krat¹í varianty + a -.  Plus a mínus patøí na zaèátek hledaného výrazu,
umístìní na konci je chyba. Hvìzdièka funguje jako zástupný
znak. Hledání je nezávislé na velikosti písmen a diakritice.
<a href="/clanky/show/5024">Více informací</a>.</p>

<h1 class="st_nadpis">Pøíklady</h1>

<dl>
<dt>nvidia</dt>
<dd>Najde v¹echny dokumenty s výskytem øetìzce nvidia.</dd>
<dt>nvidia tnt</dt>
<dd>Najde v¹echny dokumenty obsahující øetìzce nvidia nebo tnt.</dd>
<dt>nvidia OR tnt</dt>
<dd>Najde v¹echny dokumenty obsahující øetìzce nvidia nebo tnt.</dd>
<dt>nvidia AND TNT</dt>
<dd>Najde v¹echny dokumenty obsahující øetìzce nvidia a tnt.</dd>
<dt>+nvidia +tnt</dt>
<dd>Najde v¹echny dokumenty obsahující øetìzce nvidia a tnt.</dd>
<dt>+nvidia -tnt</dt>
<dd>Najde v¹echny dokumenty obsahující øetìzec nvidia a neobsahující øetìzec tnt.</dd>
<dt>nvidia NOT tnt</dt>
<dd>Najde v¹echny dokumenty obsahující øetìzec nvidia a neobsahující øetìzec tnt.</dd>
</dl>

<form action="${URL.make("/EditDiscussion")}" method="POST">
  Hledaný øetìzec: <input type="text" name="query" size="50" tabindex="1">
  <input type="submit" value="Hledej" tabindex="2">  <br>
  <input type="hidden" name="action" value="addQuez2">
  <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "../footer.ftl">
