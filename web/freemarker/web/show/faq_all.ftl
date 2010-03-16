<#include "../header.ftl">

<@lib.advertisement id="square" />

<h1>Často kladené otázky</h1>

<p>Často kladené otázky (anglicky <a href="/slovnik/faq">FAQ</a>) jsou kolekcí předem
zodpovězených otázek, na které se čtenáři často ptají
v <a href="/poradna">Poradně</a>. Pokud řešíte určitý problém nebo se začínáte
seznamovat s Linuxem, měli byste začít na této stránce a prostudovat
pečlivě jednotlivé otázky. Výhodou oproti Poradně je přehlednost a
(většinou i) úplnost odpovědi. Jelikož jde o společnou
práci, kterýkoliv čtenář smí vylepšit či upřesnit odpověď,
díky čemuž narůstá kvalita zodpovězených otázek.</p>

<h2>Sekce</h2>

<p>Pro usnadnění orientace jsou zodpovězené otázky řazeny
do sekcí, které pokrývají jedno téma. Chcete-li přidat novou
zodpovězenou otázku a cítíte-li, že se nehodí do žádné sekce,
požádejte administrátory o vytvoření nové sekce. Netušíte-li,
ve které sekci hledat, projděte si <a href="/History?type=faq">historii</a>,
kde jsou všechny otázky řazeny nezávisle na sekci podle datumu
poslední změny.</p>

<table class="faq">
  <thead>
    <tr>
        <td class="td-nazev">Sekce</td>
        <td class="td-meta td-right">Počet</td>
    </tr>
  </thead>
  <tbody>
    <#list VARS.faqTree.children as faq>
      <tr>
        <td class="td-nazev">
          <a href="${faq.url}">${faq.name}</a>
            <#if faq.description??>
              <p class="meta-vypis">${faq.description}</p>
            </#if>
        </td>
        <td class="td-meta td-right">${faq.size}</td>
      </tr>
    </#list>
  </tbody>
</table>

<#include "../footer.ftl">
