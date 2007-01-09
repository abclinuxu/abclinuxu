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
    <!--li><a href="${URL.noPrefix("/Admin?action=restartTasks")}">restartuj úlohy</a></li-->
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
    <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid=250")}">Vytvoø anketu</a></li>
    <li><a href="${URL.noPrefix("/EditRelation?action=setURL")}">Nastav URL relaci</a></li>
    <li><a href="${URL.noPrefix("/EditAdvertisement")}">Správa reklamních pozic</a></li>
    <li><a href="${URL.noPrefix("/editContent/66948?action=add")}">Vytvoø dokument</a></li>
    <li><a href="${URL.noPrefix("/clanky/dir/66948")}">Listuj dokumenty</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=add")}">Vytvoø velkou anketu</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=list")}">Listuj velké ankety</a></li>
</ul>

<h3>Správa redakce</h3>

<ul>
    <li><a href="${URL.noPrefix("/clanky/dir/8082")}">èekající èlánky</a></li>
    <li><a href="${URL.noPrefix("/clanky/honorare")}">honoráøe</a></li>
    <li><a href="${URL.noPrefix("/autori")}">seznam autorù</a></li>
    <li><a href="${URL.noPrefix("/serialy")}">seznam seriálù</a></li>
</ul>

<h3>Kontrola stavu a statistika</h3>

<ul>
    <li><a href="${URL.noPrefix("/Admin/statistika")}">Statistika náv¹tìvnosti</a></li>
    <li><a href="/ProxoolAdmin">Statistika JDBC</a></li>
    <li><a href="${URL.noPrefix("/Admin?action=performCheck")}">Kontrola stavu portálu</a></li>
</ul>

<#if USER.hasRole("root")>
    <p>
        <a href="${URL.noPrefix("/Admin?action=switchMaintainance")}">
            <#if SYSTEM_CONFIG.isMaintainanceMode()>Vypnout<#else>Zapnout</#if> re¾im údr¾by
        </a> <br>
        pou¾ívat jen v krajní nouzi! Re¾im údr¾by znamená, ¾e celé abíèko bude jen ke ètení.
    </p>
</#if>

<#include "../footer.ftl">
