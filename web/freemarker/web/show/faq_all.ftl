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

<table class="faq">
  <thead>
    <tr>
        <td class="td01">Sekce</td>
        <td class="td04">Poèet</td>
    </tr>
  </thead>
  <tbody>
    <#list VARS.faqTree.children as faq>
      <tr>
        <td class="td01">
          <a href="${faq.url}">${faq.name}</a>
            <#if faq.description?exists>
              <span class="meta"><br />
              ${faq.description}</span>
            </#if>
        </td>
        <td class="td04">${faq.size}</td>
      </tr>
    </#list>
  </tbody>
</table>

<#include "../footer.ftl">
