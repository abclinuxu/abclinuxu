<#include "../header.ftl">

<@lib.showMessages/>

<h3>Reinicializace</h3>

<p>Tato akce slou�� pro vypr�zdn�n� transparentn�
cache. N�sledkem toho budou v�echny objekty znovu
na�teny z datab�ze. Tak� se znovu na�tou v�echny
konfigura�n� soubory.
</p>

<ul>
    <li><a href="${URL.noPrefix("/Admin?action=clearCache")}">nov� inicializace</a></li>
    <li><a href="${URL.noPrefix("/Admin?action=refreshRss")}">p�egeneruj RSS soubory</a></li>
</ul>


<h3>Spr�va u�ivatel�</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditUser?action=grant")}">p�i�a� roli</a></li>
    <li><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">invaliduj emaily u�ivatel�m</a></li>
    <li><a href="${URL.noPrefix("/Group?action=show")}">seznam skupin</a></li>
</ul>

<h3>Spr�va obsahu</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditRelation?action=setURL")}">Nastav URL relaci</a></li>
    <li><a href="${URL.noPrefix("/editContent?action=add")}">Vytvo� dokument</a></li>
    <li><a href="${URL.noPrefix("/clanky/dir/66948")}">Listuj dokumenty</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=add")}">Vytvo� velkou anketu</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=list")}">Listuj velk� ankety</a></li>
</ul>


<h3>Kontrola stavu a statistika</h3>

<ul>
    <li><a href="${URL.noPrefix("/Admin?action=performCheck")}">Kontrola stavu port�lu</a></li>
    <li><a href="/ProxoolAdmin">Statistika JDBC</a></li>
</ul>

<#include "../footer.ftl">
