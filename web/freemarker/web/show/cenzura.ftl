<#include "../header.ftl">

<h1 class="st_nadpis">Úvod</h1>

<p>Tento pøíspìvek byl oznaèen na¹imi administrátory,
nebo» dle jejich názoru nevyhovoval pravidlùm slu¹ného
chování èi zákonùm této zemì.
</p>

<h1 class="st_nadpis">Proè?</h1>

<p>Ná¹ portál patøí k posledním serverùm na svìtì, které si udr¾ují
vysoký standard kvality diskusí. Komentáøe jsou k vìci, lidé se
sna¾í druhým pomoci, nenadávají si. Schválnì srovnejte kvalitu
na¹ich diskusí s konkurencí. Chceme tento standard uchovat, bohu¾el
nìkteøí náv¹tìvníci se u nás zaèínají chovat tak, jak jsou zvyklí
jinde. A to nehodláme tolerovat. Proto jsme zavedli cenzuru.
</p>

<h1 class="st_nadpis">K èemu je dobrá</h1>

<p>Cenzura je speciální nástroj na¹ich administrátorù k oznaèení
nevhodných pøíspìvkù. Mohou jej pou¾ít tehdy, pokud narazí na
komentáø obsahující vulgarismy, osobní urá¾ky èi osoèování nebo
pokud obsah odporuje platným zákonùm (napøíklad podpora takových
zrùdností, jako je komunismus èi fa¹ismus, výzvy k nezákonným
akcím a podobnì).
</p>

<h1 class="st_nadpis">Jak funguje</h1>

<p>Text takto oznaèených pøíspìvkù není v diskusi zobrazen. Místo
nìj je napsáno vysvìtlení a odkaz na tuto stránku, kde je zobrazen
nezmìnìný závadný pøíspìvek. Takto se ka¾dý ná¹ ètenáø mù¾e sám rozhodnout,
zda si jej pøeète (jde o explicitní akci) a zda mìl ná¹ administrátor
pravdu, nebo byl pøíli¹ úzkostlivý.
</p>

<h1 class="st_nadpis">Závadný pøíspìvek</h1>

 <div class="ds_hlavicka">

  Datum:</span> ${DATE.show(THREAD.created,"CZ_FULL")}<br>
  Od:
  <#if THREAD.author?exists>
   <#assign who=TOOL.createUser(THREAD.author)>
   <a href="/Profile/${who.id}">${who.name}</a><br>
  <#else>
   ${THREAD.anonymName?if_exists}<br>
  </#if>
  Titulek: ${THREAD.title?if_exists}
 </div>

  <div class="ds_text">
${TOOL.render(TOOL.element(THREAD.data,"//text"),USER?if_exists)}
  </div>

<#include "../footer.ftl">
