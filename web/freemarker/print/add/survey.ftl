<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úvod</h1>

<p>
    Každá anketa se skládá z HTML souborů obsahujících formuláře
    a z XML uloženého v databázi. XML určuje pořadí volání jednotlivých
    HTML souborů (nazývaných SCREEN) a ukládání výsledků. Zároveň obsahuje
    pomocné údaje pro automatickou analýzu výsledků, zvláště pak jednotlivé
    VOLBY (radio buttony a check boxy). První screen musí mít ID nastaven na START.
    Každý screen musí obsahovat značku template, která obsahuje cestu
    k určitému HTML souboru. Cesta musí začínat lomítkem a být relativní
    vůči šabloně (WEB-INF/freemarker/web).
</p>

<p>
    Screen může mít atributy check a onlyUsers. První zkontroluje, zda aktuální
    uživatel již nevolil v této anketě. U přihlášeného uživatele se uloží jeho
    id, pro ostatní se uloží cookie a zaznamená čas a IP adresa. Nastavení najdete
    v systemPrefs.xml, třída AccessKeeper. Druhý atribut zobrazí místo screenu
    přihlašovací formulář, pokud návštěvník není zalogovaný. 
</p>

<p>
    Pokud chcete v některém kroku uložit všechny údaje získané od uživatele
    v předchozích screenech, vložte do daného screenu značku dump. Ta způsobí,
    že se všechny údaje zapíší do XML souboru do daného adresáře pod náhodným
    jménem začínajícím na prefix a končícím na suffix. Obvykle se tato značka
    používá v posledním screenu, který obsahuje poděkování. Element dump může
    mít v sobě elementy dir (adresář, kam zapsat výsledky v XML podobě, relativní
    vůči adresáři web v šablonách), preffix a suffix definující pravidla pojmenování
    souboru. Dále zde může být element email (i vícekrát), na který se mají poslat data.
</p>

<h1>Anketa</h1>

<form action="${URL.make("/EditSurvey")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td width="120" class="required">Jméno ankety</td>
            <td>
                <input type="text" name="title" value="${PARAMS.title?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120" align="middle">Volby</td>
            <td>
                <p>
                    Zde napište jména všech radio buttonů a check boxů z formulářů.
                    Každé jméno dejte na samostatný řádek.
                </p>
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
