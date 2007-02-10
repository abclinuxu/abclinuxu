<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úvod</h1>

<p>Každá anketa se skládá z HTML souborů obsahujících formuláře
a z XML, které je uloženo v databázi jako položka typu 6. Tato položka
určuje pořadí volání jednotlivých HTML souborů (nazývaných SCREEN)
a ukládání výsledků. Zároveň obsahuje pomocné údaje pro automatickou
analýzu výsledků, zvláště pak jednotlivé VOLBY (radio buttony a check boxy).
</p>

<p>
První screen musí mít ID nastaven na START. Každý screen musí obsahovat
značku template, která obsahuje cestu k určitému HTML souboru. Cesta
musí začínat lomítkem a být relativní vůči šabloně (WEB-INF/freemarker/web).
Své HTML soubory zkopírujte jak do této hlavní šablony, tak i do ostatních
(v současnosti WEB-INF/freemarker/lynx).
</p>

<p>
Pokud chcete v některém kroku uložit všechny údaje získané od uživatele
v předchozích screenech, vložte do daného screenu značku dump. Ta způsobí,
že se všechny údaje zapíší do XML souboru do daného adresáře pod náhodným
jménem začínajícím na prefix a končícím na suffix. Údaje budou zároveň vymazány
z paměti, takže další dump je již nebude zapisovat. Takto můžete například
zajistit anonymní anketu - oddělení osobních údajů od ostatních informací.
Obvykle se tato značka používá v posledním screenu, který již pochopitelně
neobsahuje formulář, jen poděkování.
</p>

<p>
HTML soubory musí obsahovat formulář s přesně danými informacemi, pokud
některé opomenete, anketa nebude fungovat správně. Základní informací
je nastavit správně URL, kam bude formulář zasílat údaje. Toto URL
je /Survey. Kvůli lidem majícím zakázané cookies u anket s více screeny
je vhodné použít URL rewriting. To získáte takto: ${URL.noPrefix("/Survey")},
nicméně nebude fungovat, pokud první screen začleníte do článku. Tento
fígl funguje pouze, pokud jej použijete v samostatném HTML souboru definovaném
v značce template.
</p>

<p>
Formulář musí dále obsahovat čtyři skrytá vstupní políčka. První je
surveyId, který obsahuje číslo položky této ankety. Druhé políčko je
SCREEN_CURRENT, což je id aktuálního screenu. SCREEN_NEXT zase obsahuje
číslo následujícího screenu, který bude zobrazen na další stránce.
SAVE_PARAMS musí obsahovat čárkou oddělená jména všech formulářových
políček, které chcete uložit. Pokud některé políčko zde vynecháte,
nebude uloženo a jeho hodnota bude ztracena.
</p>

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
    <p>Zde napište jména všech radio buttonů a check boxů z formulářů.<br>
    Každé jméno dejte na samostatný řádek.</p>
    <textarea name="choices" cols="40" rows="4" tabindex="2">${PARAMS.choices?if_exists}</textarea>
    <div class="error">${ERRORS.choices?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required" align="middle">XML definice</td>
   <td>
    <textarea name="definition" cols="80" rows="20" tabindex="3">${PARAMS.definition?if_exists?html}</textarea>
    <div class="error">${ERRORS.definition?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokračuj" tabindex="4"></td>
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
