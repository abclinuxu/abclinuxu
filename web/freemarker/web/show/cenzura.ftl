<#include "../header.ftl">

<h1>Úvod</h1>

<p>Tento pøíspìvek byl oznaèen na¹imi administrátory,
nebo» dle jejich názoru nevyhovoval pravidlùm slu¹ného
chování èi zákonùm této zemì.
</p>

<h1>Proè?</h1>

<p>Ná¹ portál patøí k posledním serverùm na svìtì, které si udr¾ují
vysoký standard kvality diskusí. Komentáøe jsou k vìci, lidé se
sna¾í druhým pomoci, nenadávají si. Schválnì srovnejte kvalitu
na¹ich diskusí s konkurencí. Chceme tento standard uchovat, bohu¾el
nìkteøí náv¹tìvníci se u nás zaèínají chovat tak, jak jsou zvyklí
jinde. A to nehodláme tolerovat. Proto jsme zavedli cenzuru.
</p>

<h1>K èemu je dobrá</h1>

<p>Cenzura je speciální nástroj na¹ich administrátorù k oznaèení
nevhodných pøíspìvkù. Mohou jej pou¾ít tehdy, pokud narazí na
komentáø obsahující vulgarismy, osobní urá¾ky èi osoèování nebo
pokud obsah odporuje platným zákonùm (napøíklad podpora takových
zrùdností, jako je komunismus èi fa¹ismus, výzvy k nezákonným
akcím a podobnì).
</p>

<h1>Jak funguje</h1>

<p>Text takto oznaèených pøíspìvkù není v diskusi zobrazen. Místo
nìj je napsáno vysvìtlení a odkaz na tuto stránku, kde je zobrazen
nezmìnìný závadný pøíspìvek. Takto se ka¾dý ná¹ ètenáø mù¾e sám rozhodnout,
zda si jej pøeète (jde o explicitní akci) a zda mìl ná¹ administrátor
pravdu, nebo byl pøíli¹ úzkostlivý.
</p>

<h1>Závadný pøíspìvek</h1>

 <p class="diz_header">
  <span class="diz_header_prefix">Datum:</span> ${DATE.show(THREAD.created,"CZ_FULL")}<br>
  <span class="diz_header_prefix">Od:</span>
  <#if THREAD.author?exists>
   <#assign who=TOOL.sync(THREAD.author)>
   <a href="/Profile/${who.id}">${who.name}</a><br>
  <#else>
   ${TOOL.xpath(THREAD.data,"author")?if_exists}<br>
  </#if>
  <span class="diz_header_prefix">Titulek:</span> ${TOOL.xpath(THREAD.data,"title")?if_exists}<br>
 </p>
 <div>${TOOL.render(TOOL.element(THREAD.data,"text"),USER?if_exists)}</div>

<#include "../footer.ftl">
