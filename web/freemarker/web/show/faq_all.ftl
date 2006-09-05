<#include "../header.ftl">

<h1>Èasto kladené otázky</h1>

<p>Èasto kladené otázky (anglicky FAQ) jsou kolekcí pøedem
zodpovìzených otázek, na které se ètenáøi èasto ptají
v diskusním fóru. Pokud øe¹íte urèitý problém nebo se zaèínáte
seznamovat s Linuxem, mìli byste zaèít na této stránce a prostudovat
peèlivì jednotlivé otázky. Výhodou oproti fóru je pøehlednost a
(vìt¹inou i) úplnost odpovìdi. Jeliko¾ jde o spoleènou
práci, kterýkoliv ètenáø smí vylep¹it èi upøesnit odpovìï,
díky èemu¾ narùstá kvalita zodpovìzených otázek.
</p>

<h2>Sekce</h2>

<p>Pro usnadnìní orientace jsou zodpovìzené otázky øazeny
do sekcí, které pokrývají jedno téma. Chcete-li pøidat novou
zodpovìzenou otázku a cítíte-li, ¾e se nehodí do ¾ádné sekce,
po¾ádejte administrátory o vytvoøení nové sekce. Netu¹íte-li,
ve které sekci hledat, projdìte si <a href="/History?type=faq">historii</a>,
kde jsou v¹echny otázky øazeny nezávisle na sekci podle datumu
poslední zmìny.
</p>

<#assign FAQS = VARS.faqTree>
<table border="0" class="siroka">
    <tr>
        <td>
            <a href="/faq/aplikace">Aplikace</a> (${FAQS.getByRelation(117404).size})
        </td>
        <td>
            <a href="/faq/bezpecnost">Bezpeènost</a> (${FAQS.getByRelation(105223).size})
        </td>
        <td>
            <a href="/faq/boot">Boot</a> (${FAQS.getByRelation(94492).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/digitalni-foto">Digitální foto</a> (${FAQS.getByRelation(94486).size})
        </td>
        <td>
            <a href="/faq/disky">Disky</a> (${FAQS.getByRelation(94480).size})
        </td>
        <td>
            <a href="/faq/distribuce">Distribuce</a> (${FAQS.getByRelation(94496).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/grafika">Grafika</a> (${FAQS.getByRelation(94478).size})
        </td>
        <td>
            <a href="/faq/instalace">Instalace</a> (${FAQS.getByRelation(94502).size})
        </td>
        <td>
            <a href="/faq/kernel">Kernel</a> (${FAQS.getByRelation(94493).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/klavesnice">Klávesnice</a> (${FAQS.getByRelation(94483).size})
        </td>
        <td>
            <a href="/faq/multimedia">Multimédia</a> (${FAQS.getByRelation(94494).size})
        </td>
        <td>
            <a href="/faq/mysi">My¹i</a> (${FAQS.getByRelation(94484).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/hardware">Ostatní hardware</a> (${FAQS.getByRelation(94485).size})
        </td>
        <td>
            <a href="/faq/prava">Práva</a> (${FAQS.getByRelation(94490).size})
        </td>
        <td>
            <a href="/faq/site">Sítì</a> (${FAQS.getByRelation(94479).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/skripty">Skripty</a> (${FAQS.getByRelation(95259).size})
        </td>
        <td>
            <a href="/faq/souborove-systemy">Souborové systémy</a> (${FAQS.getByRelation(94481).size})
        </td>
        <td>
            <a href="/faq/tisk">Tisk</a> (${FAQS.getByRelation(94488).size})
        </td>
    </tr>
    <tr>
        <td>
            <a href="/faq/vypalovani">Vypalování</a> (${FAQS.getByRelation(94491).size})
        </td>
        <td>
            <a href="/faq/web">Web</a> (${FAQS.getByRelation(94495).size})
        </td>
        <td>
            <a href="/faq/zalohovani">Zálohování</a> (${FAQS.getByRelation(94482).size})
        </td>
    </tr>
    <tr>
        <td colspan="3">
            <a href="/faq/zvuk">Zvuk</a> (${FAQS.getByRelation(94489).size})
        </td>
    </tr>
</table>

<#include "../footer.ftl">
