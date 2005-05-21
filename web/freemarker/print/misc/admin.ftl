<#include "../header.ftl">

<@lib.showMessages/>

<h3>Reinicializace</h3>

<p>Tato akce slou¾í pro vyprázdnìní transparentní
cache. Následkem toho budou v¹echny objekty znovu
naèteny z databáze. Také se znovu naètou v¹echny
konfiguraèní soubory.
</p>

<ul>
    <li><a href="${URL.noPrefix("/Admin?action=clearCache")}">nová inicializace</a></li>
    <li><a href="${URL.noPrefix("/Admin?action=refreshRss")}">pøegeneruj RSS soubory</a></li>
</ul>


<h3>Správa u¾ivatelù</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditUser?action=grant")}">pøiøaï roli</a></li>
    <li><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">invaliduj emaily u¾ivatelùm</a></li>
    <li><a href="${URL.noPrefix("/Group?action=show")}">seznam skupin</a></li>
</ul>

<h3>Správa obsahu</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditRelation?action=setURL")}">Nastav URL relaci</a></li>
    <li><a href="${URL.noPrefix("/editContent?action=add")}">Vytvoø dokument</a></li>
    <li><a href="${URL.noPrefix("/clanky/dir/66948")}">Listuj dokumenty</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=add")}">Vytvoø velkou anketu</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=list")}">Listuj velké ankety</a></li>
</ul>


<h3>Kontrola stavu a statistika</h3>

<ul>
    <li><a href="${URL.noPrefix("/Admin?action=performCheck")}">Kontrola stavu portálu</a></li>
    <li><a href="/ProxoolAdmin">Statistika JDBC</a></li>
</ul>

<#include "../footer.ftl">
