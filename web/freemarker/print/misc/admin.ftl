<#include "../header.ftl">

<@lib.showMessages/>

<h3>Reinicializace</h3>

<p>Tato akce slouží pro vyprázdnění transparentní
cache. Následkem toho budou všechny objekty znovu
načteny z databáze. Také se znovu načtou všechny
konfigurační soubory.
</p>

<ul>
    <li><a href="${URL.noPrefix("/Admin?action=clearCache")}">nová inicializace</a></li>
    <!--li><a href="${URL.noPrefix("/Admin?action=restartTasks")}">restartuj úlohy</a></li-->
    <li><a href="${URL.noPrefix("/Admin?action=refreshRss")}">přegeneruj RSS soubory</a></li>
</ul>


<h3>Správa uživatelů</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditUser?action=grant")}">přiřaď roli</a></li>
    <li><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">invaliduj emaily uživatelům</a></li>
    <li><a href="${URL.noPrefix("/Group?action=show")}">seznam skupin</a></li>
</ul>

<h3>Správa obsahu</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid=250")}">Vytvoř anketu</a></li>
    <li><a href="${URL.noPrefix("/EditRelation?action=setURL")}">Nastav URL relaci</a></li>
    <li><a href="${URL.noPrefix("/EditAdvertisement")}">Správa reklamních pozic</a></li>
    <li><a href="${URL.noPrefix("/editContent/66948?action=add")}">Vytvoř dokument</a></li>
    <li><a href="${URL.noPrefix("/clanky/dir/66948")}">Listuj dokumenty</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=add")}">Vytvoř velkou anketu</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=list")}">Listuj velké ankety</a></li>
</ul>

<h3>Správa redakce</h3>

<ul>
    <li><a href="${URL.noPrefix("/clanky/dir/8082")}">čekající články</a></li>
    <li><a href="${URL.noPrefix("/clanky/honorare")}">honoráře</a></li>
    <li><a href="${URL.noPrefix("/autori")}">seznam autorů</a></li>
    <li><a href="${URL.noPrefix("/serialy")}">seznam seriálů</a></li>
</ul>

<h3>Kontrola stavu a statistika</h3>

<ul>
    <li><a href="${URL.noPrefix("/Admin/statistika")}">Statistika návštěvnosti</a></li>
    <li><a href="/ProxoolAdmin">Statistika JDBC</a></li>
    <li><a href="${URL.noPrefix("/Admin?action=performCheck")}">Kontrola stavu portálu</a></li>
</ul>

<#if USER.hasRole("root")>
    <p>
        <a href="${URL.noPrefix("/Admin?action=switchMaintainance")}">
            <#if SYSTEM_CONFIG.isMaintainanceMode()>Vypnout<#else>Zapnout</#if> režim údržby
        </a> <br>
        používat jen v krajní nouzi! Režim údržby znamená, že celé abíčko bude jen ke čtení.
    </p>
</#if>

<#include "../footer.ftl">
