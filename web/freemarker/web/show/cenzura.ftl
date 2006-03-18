<#include "../header.ftl">

<h1 class="st_nadpis">�vod</h1>

<p>Tento p��sp�vek byl ozna�en na�imi administr�tory,
nebo� dle jejich n�zoru nevyhovoval pravidl�m slu�n�ho
chov�n� �i z�kon�m t�to zem�.
</p>

<h1 class="st_nadpis">Pro�?</h1>

<p>N� port�l pat�� k posledn�m server�m na sv�t�, kter� si udr�uj�
vysok� standard kvality diskus�. Koment��e jsou k v�ci, lid� se
sna�� druh�m pomoci, nenad�vaj� si. Schv�ln� srovnejte kvalitu
na�ich diskus� s konkurenc�. Chceme tento standard uchovat, bohu�el
n�kte�� n�v�t�vn�ci se u n�s za��naj� chovat tak, jak jsou zvykl�
jinde. A to nehodl�me tolerovat. Proto jsme zavedli cenzuru.
</p>

<h1 class="st_nadpis">K �emu je dobr�</h1>

<p>Cenzura je speci�ln� n�stroj na�ich administr�tor� k ozna�en�
nevhodn�ch p��sp�vk�. Mohou jej pou��t tehdy, pokud naraz� na
koment�� obsahuj�c� vulgarismy, osobn� ur�ky �i oso�ov�n� nebo
pokud obsah odporuje platn�m z�kon�m (nap��klad podpora takov�ch
zr�dnost�, jako je komunismus �i fa�ismus, v�zvy k nez�konn�m
akc�m a podobn�).
</p>

<h1 class="st_nadpis">Jak funguje</h1>

<p>Text takto ozna�en�ch p��sp�vk� nen� v diskusi zobrazen. M�sto
n�j je naps�no vysv�tlen� a odkaz na tuto str�nku, kde je zobrazen
nezm�n�n� z�vadn� p��sp�vek. Takto se ka�d� n� �ten�� m��e s�m rozhodnout,
zda si jej p�e�te (jde o explicitn� akci) a zda m�l n� administr�tor
pravdu, nebo byl p��li� �zkostliv�.
</p>

<h1 class="st_nadpis">Z�vadn� p��sp�vek</h1>

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
