<#include "../header.ftl">

<h1>Často kladené otázky</h1>

<p>Často kladené otázky (anglicky FAQ) jsou kolekcí předem
zodpovězených otázek, na které se čtenáři často ptají
v diskusním fóru. Pokud řešíte určitý problém nebo se začínáte
seznamovat s Linuxem, měli byste začít na této stránce a prostudovat
pečlivě jednotlivé otázky. Výhodou oproti fóru je přehlednost a
(většinou i) úplnost odpovědi. Jelikož jde o společnou
práci, kterýkoliv čtenář smí vylepšit či upřesnit odpověď,
díky čemuž narůstá kvalita zodpovězených otázek.
</p>

<h2>Sekce</h2>

<p>Pro usnadnění orientace jsou zodpovězené otázky řazeny
do sekcí, které pokrývají jedno téma. Chcete-li přidat novou
zodpovězenou otázku a cítíte-li, že se nehodí do žádné sekce,
požádejte administrátory o vytvoření nové sekce. Netušíte-li,
ve které sekci hledat, projděte si <a href="/History?type=faq">historii</a>,
kde jsou všechny otázky řazeny nezávisle na sekci podle datumu
poslední změny.
</p>

<table class="faq">
  <thead>
    <tr>
        <td class="td01">Sekce</td>
        <td class="td04">Počet</td>
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
