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
    <!--li><a href="${URL.noPrefix("/Admin?action=restartTasks")}">restartuj �lohy</a></li-->
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
    <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid=250")}">Vytvo� anketu</a></li>
    <li><a href="${URL.noPrefix("/EditRelation?action=setURL")}">Nastav URL relaci</a></li>
    <li><a href="${URL.noPrefix("/EditAdvertisement")}">Spr�va reklamn�ch pozic</a></li>
    <li><a href="${URL.noPrefix("/editContent/66948?action=add")}">Vytvo� dokument</a></li>
    <li><a href="${URL.noPrefix("/clanky/dir/66948")}">Listuj dokumenty</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=add")}">Vytvo� velkou anketu</a></li>
    <li><a href="${URL.noPrefix("/EditSurvey?action=list")}">Listuj velk� ankety</a></li>
</ul>

<h3>Spr�va redakce</h3>

<ul>
    <li><a href="${URL.noPrefix("/clanky/dir/8082")}">�ekaj�c� �l�nky</a></li>
    <li><a href="${URL.noPrefix("/clanky/honorare")}">honor��e</a></li>
    <li><a href="${URL.noPrefix("/autori")}">seznam autor�</a></li>
    <li><a href="${URL.noPrefix("/serialy")}">seznam seri�l�</a></li>
</ul>

<h3>Kontrola stavu a statistika</h3>

<ul>
    <li><a href="${URL.noPrefix("/Admin/statistika")}">Statistika n�v�t�vnosti</a></li>
    <li><a href="/ProxoolAdmin">Statistika JDBC</a></li>
    <li><a href="${URL.noPrefix("/Admin?action=performCheck")}">Kontrola stavu port�lu</a></li>
</ul>

<#if USER.hasRole("root")>
    <p>
        <a href="${URL.noPrefix("/Admin?action=switchMaintainance")}">
            <#if SYSTEM_CONFIG.isMaintainanceMode()>Vypnout<#else>Zapnout</#if> re�im �dr�by
        </a> <br>
        pou��vat jen v krajn� nouzi! Re�im �dr�by znamen�, �e cel� ab��ko bude jen ke �ten�.
    </p>
</#if>

<#include "../footer.ftl">
