<#include "../../header.ftl">

<@lib.showMessages/>

<@lib.showSignPost "Rozcestník">
<ul>
  <li><a href="${URL.make("/autori/clanky/?action=add")}" title="Napsat článek">Napsat článek</a></li>
</ul>			
</@lib.showSignPost>

<p>
Vítejte v redakčním systému. Zde můžete psát nové články, kontrolovat své honoráře,
prohlížet náměty, upravovat osobní údaje atd.
</p> 
<ul>
	<li><a href="${URL.make("/autori/clanky")}">Mé články</a></li>
	<li><a href="${URL.make("/autori/namety")}">Náměty</a></li>
	<li><a href="${URL.make("/autori/honorare")}">Mé honoráře</a></li>
	<li><a href="${URL.make("/redakce/autori/edit/${AUTHOR.id}?action=edit")}">Osobní údaje</a></li>
	<li><a href="${URL.make("/autori/smlouvy")}">Autorské smlouvy</a></li>
</ul>

<h2>Chystané články</h2>
<h2>Plánováné náměty</h2>

<#include "../../footer.ftl">
