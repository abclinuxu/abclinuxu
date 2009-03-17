<#include "../../header.ftl">

<@lib.showMessages/>

<p>
Vítejte v redakčním systému. Zde můžete psát nové články, kontrolovat své honoráře,
prohlížet náměty, upravovat osobní údaje atd.
</p> 

<div>
	<div style="width: 50%; float: left">
	<h3>Editor</h3>
	<div style="width: 50%; float: left">
	<ul>
		<li><a href="/sprava/redakce/clanky">Články</a></li>
		<li><a href="/sprava/redakce/namety">Náměty</a></li>
		<li><a href="/sprava/redakce/serialy">Seriály</a></li>
		<li><a href="/sprava/redakce/zpravicky">Zprávičky</a></li>
		<li><a href="/sprava/redakce/ankety">Ankety</a></li>
		<li><a href="/sprava/redakce/udalosti">Události</a></li>
	</ul>
	</div>
	<div style="width: 50%; float: right">
	<ul>
		<li><a href="/sprava/redakce/honorare">Honoráře</a></li>
		<li><a href="/sprava/redakce/statistiky">Statistiky</a></li>
		<li><a href="/sprava/redakce/autori">Autoři</a></li>
		<li><a href="/sprava/redakce/smlouvy">Autorské smlouvy</a></li>
	</ul>
	</div>	
	
	</div>
	
	<#if AUTHOR?? >
	<!-- redaktor -->
	<div style="width: 50%; float: right" />		
	<h3>Redaktor</h3>
	<ul>
		<li><a href="/redakce/clanky">Mé články</a></li>
		<li><a href="/redakce/namety">Náměty</a></li>
		<li><a href="/redakce/honorare">Mé honoráře</a></li>
		<li><a href="/redakce/osobni-udaje">Osobní údaje</a></li>
		<li><a href="/redakce/smlouvy">Autorské smlouvy</a></li>
	</ul>
	</div>
	</#if>		
</div>


<#include "../../footer.ftl">
