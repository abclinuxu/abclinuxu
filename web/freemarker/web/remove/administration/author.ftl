<#include "../../header.ftl">

<#if UNDELETABLE?? >
<@lib.showSignPost "Rozcestník">
<ul>
  <li><a href="${URL.make("/autori/clanky")}" title="Autorovy články">Články</a></li>  
</ul>			
</@lib.showSignPost>
</#if>

<@lib.showMessages/>

<h1>Smazání autora</h1>
<#if UNDELETABLE??>
<p>
Litujeme, ale tohoto autora nelze smazat, protože napsal nejméně jeden článek. 
Chcete-li jej opravdu smazat, musíte nejdříve jeho články přiřadit někomu jinému.
</p>
<#else>
<p style="white-space: nowrap">
Opravdu chcete smazat autora <b>${AUTHOR.title}</b>?
<form action="${URL.make("/redakce/autori/edit")}" method="POST">
	<input type="submit" name="delete" value="Ano, smazat" />
	<input type="submit" name="leave" value="Ne, nemazat" />
	<input type="hidden" name="aId" value="${AUTHOR.id}" />
	<input type="hidden" name="action" value="rm2" />
<form>
</p>
</#if>

<#include "../../footer.ftl">