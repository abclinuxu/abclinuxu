<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h1>Vypr�zdn�n� cache</h1>

<p>Tato akce slou�� pro vypr�zdn�n� transparentn�
cache. N�sledkem toho budou v�echny objekty znovu
na�teny z datab�ze. Typick� pou�it� je, pokud ru�n�
provedete zm�ny v datab�zi a nechcete �ekat, kdy se
projev�.
</p>

<p><a href="${URL.noPrefix("/Admin?action=clearCache")}">Clear cache</a></p>


<h1>Spr�va pr�v u�ivatel�</h1>

<dl>

<dt>P�i�azov�n� rol� jednotliv�m u�ivatel�m.</dt>
<dd><a href="${URL.noPrefix("/EditUser?action=grant")}">Grant roles</a></dd>

<dt>Zneplatn�n� emailu u�ivatel�m</dt>
<dd><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">Invalidate emails</a></dd>

<dt>Seznam skupin</dt>
<dd><a href="${URL.noPrefix("/Group?action=show")}">Show groups</a></dd>

</dl>

<h1>Vytvo�en� ankety</h1>

<p>Zde je mo�n� vytvo�it anketu.</p>

<p><a href="${URL.noPrefix("/EditSurvey?action=add")}">Create survey</a></p>


<h1>Kontrola stavu a statistika</h1>

<p>Kontrola stavu port�lu</p>

<p><a href="${URL.noPrefix("/Admin?action=performCheck")}">Perform check</a></p>

<p>Statistick� informace o JDBC poolu.</p>

<p><a href="/ProxoolAdmin">Proxool info</p>

<#include "../footer.ftl">
