<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Tato stránka slou¾í jako rozcestník pro
administrátory. K ní samotné není kontrolován
pøístup, zda u¾ivatel má dostateèná práva
èi nikoliv. K nìkterým akcím v¹ak ji¾
potøebujete dostateèná práva.
</p>

<h1>Vyprázdnìní cache</h1>

<p>Tato akce slou¾í pro vyprázdnìní transparentní
cache. Následkem toho budou v¹echny objekty znovu
naèteny z databáze. Typické pou¾ití je, pokud ruènì
provedete zmìny v databázi a nechcete èekat, kdy se
projeví.
</p>

<p><a href="${URL.noPrefix("/Admin?action=clearCache")}">Clear cache</a></p>


<h1>Správa práv u¾ivatelù</h1>

<p>Pøiøazování rolí jednotlivým u¾ivatelùm.</p>

<p><a href="${URL.noPrefix("/EditUser?action=grant")}">Grant roles</a></p>

<p>Zneplatnìní emailu u¾ivatelùm</p>

<p><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">Invalidate emails</a></p>


<h1>Vytvoøení ankety</h1>

<p>Zde je mo¾né vytvoøit anketu.</p>

<p><a href="${URL.noPrefix("/EditSurvey?action=add")}">Create survey</a></p>


<h1>Statistika JDBC poolu</h1>

<p>Statistické informace o JDBC poolu.</p>

<p><a href="/ProxoolAdmin">Proxool info</p>

<#include "../footer.ftl">
