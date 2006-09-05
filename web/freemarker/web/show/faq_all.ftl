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

<table border="0">
    <tr>
        <th>Sekce</th>
        <th>Poèet</th>
    </tr>
    <#list VARS.faqTree.children as faq>
        <tr>
            <td>
                <a href="${faq.url}">${faq.name}</a>
                <#if faq.description?exists>
                    <br>${faq.description}
                </#if>
            </td>
            <td align="right">${faq.size}</td>
        </tr>
    </#list>
</table>

<#include "../footer.ftl">
