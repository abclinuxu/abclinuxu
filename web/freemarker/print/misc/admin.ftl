<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h1>Vyprázdnìní cache</h1>

<p>Tato akce slou¾í pro vyprázdnìní transparentní
cache. Následkem toho budou v¹echny objekty znovu
naèteny z databáze. Typické pou¾ití je, pokud ruènì
provedete zmìny v databázi a nechcete èekat, kdy se
projeví.
</p>

<p><a href="${URL.noPrefix("/Admin?action=clearCache")}">Clear cache</a></p>


<h1>Správa práv u¾ivatelù</h1>

<dl>

<dt>Pøiøazování rolí jednotlivým u¾ivatelùm.</dt>
<dd><a href="${URL.noPrefix("/EditUser?action=grant")}">Grant roles</a></dd>

<dt>Zneplatnìní emailu u¾ivatelùm</dt>
<dd><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">Invalidate emails</a></dd>

<dt>Seznam skupin</dt>
<dd><a href="${URL.noPrefix("/Group?action=show")}">Show groups</a></dd>

</dl>

<h1>Vytvoøení ankety</h1>

<p>Zde je mo¾né vytvoøit anketu.</p>

<p><a href="${URL.noPrefix("/EditSurvey?action=add")}">Create survey</a></p>


<h1>Kontrola stavu a statistika</h1>

<p>Kontrola stavu portálu</p>

<p><a href="${URL.noPrefix("/Admin?action=performCheck")}">Perform check</a></p>

<p>Statistické informace o JDBC poolu.</p>

<p><a href="/ProxoolAdmin">Proxool info</p>

<#include "../footer.ftl">
