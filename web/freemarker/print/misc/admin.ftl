<#include "../header.ftl">

<@lib.showMessages/>

<h3>Správa obsahu</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid=250")}">vytvoř anketu</a></li>
    <li><a href="${URL.noPrefix("/EditRelation?action=setURL")}">nastav URL relaci</a></li>
    <li><a href="${URL.noPrefix("/EditAdvertisement")}">správa reklamních pozic</a></li>
    <li><a href="${URL.noPrefix("/editContent/66948?action=add")}">vytvoř dokument</a></li>
    <li><a href="${URL.noPrefix("/clanky/dir/66948")}">seznam dokumentů</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=list")}">seznam velkých anket</a></li>
    <li><a href="${URL.noPrefix("/Admin?action=refreshRss")}">přegeneruj RSS soubory</a></li>
</ul>

<h3>Správa redakce</h3>

<ul>
    <li><a href="${URL.noPrefix("/clanky/dir/8082")}">čekající články</a></li>
    <li><a href="${URL.noPrefix("/clanky/honorare")}">honoráře</a></li>
    <li><a href="${URL.noPrefix("/autori")}">seznam autorů</a></li>
    <li><a href="${URL.noPrefix("/serialy")}">seznam seriálů</a></li>
</ul>

<h3>Správa uživatelů</h3>

<ul>
    <li><a href="${URL.noPrefix("/EditUser?action=grant")}">přiřaď roli</a></li>
    <li><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">invaliduj emaily uživatelům</a></li>
    <li><a href="${URL.noPrefix("/Group?action=show")}">seznam skupin</a></li>
</ul>

<h3>Stav portálu</h3>

<ul>
    <li><a href="${URL.noPrefix("/Admin?action=clearCache")}">nová inicializace</a></li>
    <!--li><a href="${URL.noPrefix("/Admin?action=restartTasks")}">restartuj úlohy</a></li-->
    <li><a href="${URL.noPrefix("/Admin/statistika")}">statistika návštěvnosti</a></li>
    <li><a href="/ProxoolAdmin">statistika JDBC</a></li>
    <li><a href="${URL.noPrefix("/Admin?action=performCheck")}">kontrola stavu portálu</a></li>
    <#if USER.hasRole("root")>
        <li>
            <a href="${URL.noPrefix("/Admin?action=switchMaintainance")}">
                <#if SYSTEM_CONFIG.isMaintainanceMode()>vypnout<#else>zapnout</#if> režim údržby
            </a> <br>
            používat jen v krajní nouzi! Celé abíčko bude jen ke čtení.
        </li>
    </#if>
</ul>

<#include "../footer.ftl">
