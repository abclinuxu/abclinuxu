<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úvod</h1>

<p>Ka¾dá anketa se skládá z HTML souborù obsahujících formuláøe
a z XML, které je ulo¾eno v databázi jako polo¾ka typu 6. Tato polo¾ka
urèuje poøadí volání jednotlivých HTML souborù (nazývaných SCREEN)
a ukládání výsledkù. Zároveò obsahuje pomocné údaje pro automatickou
analýzu výsledkù, zvlá¹tì pak jednotlivé VOLBY (radio buttony a check boxy).
</p>

<p>
První screen musí mít ID nastaven na START. Ka¾dý screen musí obsahovat
znaèku template, která obsahuje cestu k urèitému HTML souboru. Cesta
musí zaèínat lomítkem a být relativní vùèi ¹ablonì (WEB-INF/freemarker/web).
Své HTML soubory zkopírujte jak do této hlavní ¹ablony, tak i do ostatních
(v souèasnosti WEB-INF/freemarker/lynx).
</p>

<p>
Pokud chcete v nìkterém kroku ulo¾it v¹echny údaje získané od u¾ivatele
v pøedchozích screenech, vlo¾te do daného screenu znaèku dump. Ta zpùsobí,
¾e se v¹echny údaje zapí¹í do XML souboru do daného adresáøe pod náhodným
jménem zaèínajícím na prefix a konèícím na suffix. Údaje budou zároveò vymazány
z pamìti, tak¾e dal¹í dump je ji¾ nebude zapisovat. Takto mù¾ete napøíklad
zajistit anonymní anketu - oddìlení osobních údajù od ostatních informací.
Obvykle se tato znaèka pou¾ívá v posledním screenu, který ji¾ pochopitelnì
neobsahuje formuláø, jen podìkování.
</p>

<p>
HTML soubory musí obsahovat formuláø s pøesnì danými informacemi, pokud
nìkteré opomenete, anketa nebude fungovat správnì. Základní informací
je nastavit správnì URL, kam bude formuláø zasílat údaje. Toto URL
je /Survey. Kvùli lidem majícím zakázané cookies u anket s více screeny
je vhodné pou¾ít URL rewriting. To získáte takto: ${URL.noPrefix("/Survey")},
nicménì nebude fungovat, pokdu první screen zaèleníte do èlánku. Tento
fígl funguje, pouze pokud jej pou¾ijete v samostatném HTML souboru definovaném
v znaèce template.
</p>

<p>
Formuláø musí dále obsahovat ètyøi skrytá vstupní políèka. První je
surveyId, který obsahuje èíslo polo¾ky této ankety. Druhé políèko je
SCREEN_CURRENT, co¾ je id aktuálního screenu. SCREEN_NEXT zase obsahuje
èíslo následujícího screenu, který bude zobrazen na dal¹í stránce.
SAVE_PARAMS musí obsahovat èárkou oddìlená jména v¹ech formuláøových
políèek, které chcete ulo¾it. Pokud nìkteré políèko zde vynecháte,
nebude ulo¾eno a jeho hodnota bude ztracena.
</p>

<h1>Pøíklad</h1>

<h2>XML definice</h2>

<pre>${"<anketa>
  <screen id=\"START\">
    <template>/ankety/abc1/q1.ftl</template>
  </screen>
  <screen id=\"demografie\">
    <template>/ankety/abc1/q2.ftl</template>
  </screen>
  <screen id=\"last\">
    <template>/ankety/abc1/thanks.ftl</template>
    <dump>
      <dir>/home/www-data/deploy/abclinuxu/WEB-INF/freemarker/web/ankety/abc1/data</dir>
      <prefix>answear_</prefix>
      <suffix>.xml</suffix>
    </dump>
  </screen>
</anketa>"?html}</pre>

<h2>HTML soubor /home/www-data/deploy/abclinuxu/WEB-INF/freemarker/web/ankety/abc1/q2.ftl</h2>

<pre>${"<form action=\"${URL.noPrefix(\"/Survey\")}\" method=\"POST\">
   <input type=\"hidden\" name=\"SCREEN_CURRENT\" value=\"demografie\">
   <input type=\"hidden\" name=\"SCREEN_NEXT\" value=\"last\">
   <input type=\"hidden\" name=\"SAVE_PARAMS\" value=\"speed,sluzby_chybi,kvalita_clanku,frekvence_clanku,vzkaz\">
   <input type=\"hidden\" name=\"surveyId\" value=\"3285\">
</form>"?html}</pre>

<h1>Anketa</h1>

<form action="${URL.make("/EditSurvey")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="120">Jméno ankety</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" align="middle">Volby</td>
   <td>
    <p>Zde napi¹te jména v¹ech radio buttonù a check boxù z formuláøù.<br>
    Ka¾dé jméno dejte na samostatný øádek.</p>
    <textarea name="choices" cols="40" rows="4" tabindex="2">${PARAMS.choices?if_exists}</textarea>
    <div class="error">${ERRORS.choices?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required" align="middle">XML definice</td>
   <td>
    <pre>${"<!ELEMENT anketa (screen+)>
<!ELEMENT screen (#PCDATA)>
<!ATTLIST screen id (ID) #REQUIRED>
<!ELEMENT template (#PCDATA)>
<!ELEMENT dump (dir,prefix,suffix)>
<!ELEMENT dir (#PCDATA)>
<!ELEMENT prefix (#PCDATA)>
<!ELEMENT suffix (#PCDATA)>"?html}</pre>
    <textarea name="definition" cols="80" rows="20" tabindex="3">${PARAMS.definition?if_exists?html}</textarea>
    <div class="error">${ERRORS.definition?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokraèuj" tabindex="4"></td>
  </tr>
 </table>

 <#if PARAMS.surveyId?exists>
  <input type="hidden" name="action" value="edit2">
  <input type="hidden" name="surveyId" value="${PARAMS.surveyId}">
 <#else>
  <input type="hidden" name="action" value="add2">
 </#if>
</form>


<#include "../footer.ftl">
