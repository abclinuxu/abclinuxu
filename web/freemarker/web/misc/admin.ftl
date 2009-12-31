<#include "../header.ftl">

<@lib.showMessages/>

<h2>Administrace portálu</h2>


<table border="0">
    <tr>
        <td valign="top">
            <h3>Správa redakce</h3>

            <ul>
                <li><a href="${URL.noPrefix("/sprava/redakce")}">správa redakce</a></li>
                <li><a href="${URL.noPrefix("/clanky/dir/8082")}">čekající články</a></li>
                <li><a href="${URL.noPrefix("/clanky/honorare")}">honoráře</a></li>
                <li><a href="${URL.noPrefix("/autori")}">seznam autorů</a></li>
                <li><a href="${URL.noPrefix("/serialy")}">seznam seriálů</a></li>
            </ul>

            <h3>Správa uživatelů</h3>

            <ul>
                <li><a href="${URL.noPrefix("/Group?action=show")}">seznam skupin</a></li>
                <li><a href="${URL.noPrefix("/EditUser?action=grant")}">přiřaď roli</a></li>
                <li><a href="${URL.noPrefix("/EditUser?action=invalidateEmail")}">invaliduj emaily uživatelům</a></li>
                <li><a href="${URL.noPrefix("/EditUser?action=removeMerge")}">odstranit či sloučit uživatele</a></li>
                <li>
                    <form action="/sprava" method="post">
                        <input type="text" name="uid" size="5">
                        <input type="hidden" name="action" value="su">
                        <input type="submit" value="su">
                        (číslo nebo login uživatele)
                    </form>
                </li>
            </ul>
        </td>
        <td valign="top">
            <h3>Správa obsahu</h3>

            <ul>
                <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid=250")}">vytvoř anketu</a></li>
                <li><a href="${URL.noPrefix("/EditRelation?action=setURL")}">nastav URL relaci</a></li>
                <li><a href="${URL.noPrefix("/EditAdvertisement")}">správa reklamních pozic</a></li>
                <li><a href="${URL.noPrefix("/clanky/dir/66948")}">seznam dokumentů</a></li>
                <li><a href="${URL.noPrefix("/EditSurvey?action=list")}">seznam velkých anket</a></li>
                <li><a href="${URL.noPrefix("/sprava?action=refreshRss"+TOOL.ticket(USER, false))}">přegeneruj RSS soubory</a></li>
                <li><a href="${URL.noPrefix("/EditServers/319")}">správa rozcestníku</a></li>
            </ul>

            <h3>Stav portálu</h3>

            <ul>
                <li><a href="${URL.noPrefix("/sprava?action=clearCache"+TOOL.ticket(USER, false))}">nová inicializace</a></li>
                <!--li><a href="${URL.noPrefix("/sprava?action=restartTasks")}">restartuj úlohy</a></li-->
                <li><a href="${URL.noPrefix("/sprava/statistika")}">statistika návštěvnosti</a></li>
                <li><a href="/ProxoolAdmin">statistika JDBC</a></li>
                <li><a href="${URL.noPrefix("/sprava?action=performCheck")}">kontrola stavu portálu</a></li>
                <#if USER.hasRole("root")>
                    <li>
                        <a href="${URL.noPrefix("/sprava?action=switchMaintainance"+TOOL.ticket(USER, false))}">
                            <#if SYSTEM_CONFIG.isMaintainanceMode()>vypnout<#else>zapnout</#if> režim údržby
                        </a>
                        používat jen v krajní nouzi! Celé abíčko bude jen ke čtení.
                    </li>
                </#if>
            </ul>
        </td>
    </tr>
</table>

<#include "../footer.ftl">
