<#include "../header.ftl">

<@lib.showMessages/>

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
<dd><a href="${URL.noPrefix("/EditUser?action=grant")}">p�i�a� roli</a></dd>

<dt>Zneplatn�n� emailu u�ivatel�m</dt>
<dd><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">invaliduj emaily</a></dd>

<dt>Seznam skupin</dt>
<dd><a href="${URL.noPrefix("/Group?action=show")}">uka� skupiny</a></dd>

</dl>

<h1>Dokument identifikovan� adresou</h1>

<p><a href="${URL.noPrefix("/editContent?action=add")}">Vytvo� dokument</a></p>
<p><a href="${URL.noPrefix("/clanky/dir/66948")}">Listuj dokumenty</a></p>


<h1>Vytvo�en� ankety</h1>

<p><a href="${URL.noPrefix("/EditSurvey?action=add")}">Vytvo� anketu</a></p>

<h1>Nov� vygenerov�n� RSS</h1>

<p><a href="${URL.noPrefix("/Admin?action=refreshRss")}">Obnov RSS</a></p>


<h1>Kontrola stavu a statistika</h1>

<p><a href="${URL.noPrefix("/Admin?action=performCheck")}">Kontrola stavu port�lu</a></p>

<p>Statistick� informace o JDBC poolu.</p>

<p><a href="/ProxoolAdmin">Proxool info</p>

<#include "../footer.ftl">
