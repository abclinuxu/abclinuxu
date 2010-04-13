<#include "../header.ftl">

<div class="no-col-ad">
    <@lib.advertisement id="hypertext2nahore" />
    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />
</div>


<h1>Cenzura</h1>

<p>Tento příspěvek byl označen našimi administrátory, neboť dle jejich názoru nevyhovoval pravidlům slušného chování či zákonům této země.</p>


<h2>Proč?</h2>

<p>Náš portál patří k posledním serverům na světě, které si udržují vysoký standard kvality diskusí. Komentáře jsou k věci, lidé se snaží druhým pomoci, nenadávají si. Schválně srovnejte kvalitu našich diskusí s konkurencí. Chceme tento standard uchovat, bohužel někteří návštěvníci se u nás začínají chovat tak, jak jsou zvyklí jinde. A to nehodláme tolerovat. Proto jsme zavedli cenzuru.</p>


<h2>K čemu je dobrá</h2>

<p>Cenzura je speciální nástroj našich administrátorů k označení nevhodných příspěvků. Mohou jej použít tehdy, pokud narazí na komentář obsahující vulgarismy, osobní urážky či osočování nebo pokud obsah odporuje platným zákonům (například podpora takových zrůdností, jako je komunismus či fašismus, výzvy k nezákonným akcím a podobně).</p>


<h2>Jak funguje</h2>

<p>Text takto označených příspěvků není v diskusi zobrazen. Místo něj je napsáno vysvětlení a odkaz na tuto stránku, kde je zobrazen nezměněný závadný příspěvek. Takto se každý náš čtenář může sám rozhodnout, zda si jej přečte (jde o explicitní akci) a zda měl náš administrátor pravdu, nebo byl příliš úzkostlivý.</p>


<h2>Závadný příspěvek</h2>

 <div class="ds_hlavicka">

  Datum:</span> ${DATE.show(THREAD.created,"CZ_FULL")}<br>
  Od:
  <#if THREAD.author??>
   <#assign who=TOOL.createUser(THREAD.author)>
   <@lib.showUser who/><br>
  <#else>
   ${THREAD.anonymName!}<br>
  </#if>
  Titulek: ${THREAD.title!}
 </div>

  <div class="ds_text">
${TOOL.render(TOOL.element(THREAD.data,"//text"),USER!)}
  </div>

<#include "../footer.ftl">
