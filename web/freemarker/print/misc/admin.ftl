<#include "../header.ftl">

<@lib.showMessages/>

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
<dd><a href="${URL.noPrefix("/EditUser?action=grant")}">pøiøaï roli</a></dd>

<dt>Zneplatnìní emailu u¾ivatelùm</dt>
<dd><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">invaliduj emaily</a></dd>

<dt>Seznam skupin</dt>
<dd><a href="${URL.noPrefix("/Group?action=show")}">uka¾ skupiny</a></dd>

</dl>

<h1>Dokument identifikovaný adresou</h1>

<p><a href="${URL.noPrefix("/editContent?action=add")}">Vytvoø dokument</a></p>
<p><a href="${URL.noPrefix("/clanky/dir/66948")}">Listuj dokumenty</a></p>


<h1>Vytvoøení ankety</h1>

<p><a href="${URL.noPrefix("/EditSurvey?action=add")}">Vytvoø anketu</a></p>

<h1>Nové vygenerování RSS</h1>

<p><a href="${URL.noPrefix("/Admin?action=refreshRss")}">Obnov RSS</a></p>


<h1>Kontrola stavu a statistika</h1>

<p><a href="${URL.noPrefix("/Admin?action=performCheck")}">Kontrola stavu portálu</a></p>

<p>Statistické informace o JDBC poolu.</p>

<p><a href="/ProxoolAdmin">Proxool info</p>

<#include "../footer.ftl">
