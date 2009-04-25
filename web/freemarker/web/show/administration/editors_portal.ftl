<#include "../../header.ftl">

<@lib.showMessages/>

<#if AUTHOR?? >
<@lib.showSignPost "Rozcestník">
<ul>
  <li><a href="${URL.make("/autori/clanky/?action=add")}" title="Napsat článek">Napsat článek</a></li>
</ul>			
</@lib.showSignPost>
</#if>
<p>
Vítejte v redakčním systému. Zde můžete psát nové články, kontrolovat své honoráře,
prohlížet náměty, upravovat osobní údaje atd.
</p> 

<div class="two-columns">
	<div class="two-columns left-column">
	<h3>Editor</h3>
	<div class="left-column">
	<ul>
		<li><a href="/sprava/redakce/clanky">Články</a></li>
		<li><a href="/sprava/redakce/namety">Náměty</a></li>
		<li><a href="/sprava/redakce/serialy">Seriály</a></li>
		<li><a href="/sprava/redakce/zpravicky">Zprávičky</a></li>
		<li><a href="/sprava/redakce/ankety">Ankety</a></li>
		<li><a href="/sprava/redakce/udalosti">Události</a></li>
	</ul>
	</div>
	<div class="right-column">
	<ul>
		<li><a href="/sprava/redakce/honorare">Honoráře</a></li>
		<li><a href="/sprava/redakce/statistiky">Statistiky</a></li>
		<li><a href="/sprava/redakce/autori">Autoři</a></li>
		<li><a href="/sprava/redakce/smlouvy">Autorské smlouvy</a></li>
	</ul>
	</div>	
	</div>
	
	<#if AUTHOR?? >
	<#-- redaktor -->
	<div class="right-column" />		
	<h3>Redaktor</h3>
	<ul>
		<li><a href="/autori/clanky">Mé články</a></li>
		<li><a href="/autori/namety">Náměty</a></li>
		<li><a href="/autori/honorare">Mé honoráře</a></li>
		<li><a href="/autori/osobni-udaje">Osobní údaje</a></li>
		<li><a href="/autori/smlouvy">Autorské smlouvy</a></li>
	</ul>
	</div>
	</#if>		
</div>


<#include "../../footer.ftl">
