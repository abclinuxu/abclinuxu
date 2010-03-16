<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="${URL.make("/slovnik/edit?action=add")}">Vysvětlit nový pojem</a>
            </li>
            <li>
                <@lib.showMonitor RELATION "Zašle upozornění na váš email při nové položce v této a v podřazených sekcích."/>
            </li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<@lib.advertisement id="square" />

<h1>Výkladový slovník</h1>

<p>
    Výkladový slovník našeho portálu je projekt, jenž se snaží českým
    a slovenským uživatelům přiblížit Linux. Málokterý nováček se začne
    s tímto operačním systémem seznamovat tak, že si o něm nejdříve přečte
    <a href="/hledani?dotaz=recenze+kniha+cena&advancedMode=true&type=clanek">nějakou knihu</a>
    nebo <a href="/ucebnice">učebnici</a>. Pokud nemá žádné zkušenosti
    s jinými operačními systémy odvozenými od Unixu, velmi rychle si připadá ztracený,
    protože se všude používají pojmy a slova, která nechápe.
</p>

<p>
    Výkladový slovník je pokus, jak tento problém zmenšit. Jak je našim dobrým zvykem,
    jedná se o otevřený komunitní projekt, do něhož může přispět každý. Jeho cílem je popsat
    všechny základní pojmy, které se v Linuxu běžně objevují. Pokud se chcete zapojit
    do tvorby této databáze, můžete
    <a class="bez-slovniku" href="${URL.make("/slovnik/edit?action=add")}">vysvětlit
    nový pojem</a>.
</p>

<p>
    Pokud nenajdete některý pojem v našem slovníku a rozumíte anglicky, zkuste
    <a href="http://en.wikipedia.org/wiki/Category:Computing">Wikipedii</a>
    nebo <a href="http://www.acronymdictionary.co.uk">seznam akronymů</a>. K dispozici
    existuje i <a href="http://www.ucc.ie/cgi-bin/acronym">hledání</a> akronymů.
</p>


<form action="/hledani" method="GET">
    <input type="text" name="dotaz" size="30" tabindex="1">
    <input type="hidden" name="typ" value="pojem">
    <input type="submit" value="Hledej ve slovníku" class="button" tabindex="2">
</form>

<p class="dict-abc">
    Filtr:
    <a href="/slovnik?prefix=a"<@highlight 'a'/>>A</a>
    <a href="/slovnik?prefix=b"<@highlight 'b'/>>B</a>
    <a href="/slovnik?prefix=c"<@highlight 'c'/>>C</a>
    <a href="/slovnik?prefix=d"<@highlight 'd'/>>D</a>
    <a href="/slovnik?prefix=e"<@highlight 'e'/>>E</a>
    <a href="/slovnik?prefix=f"<@highlight 'f'/>>F</a>
    <a href="/slovnik?prefix=g"<@highlight 'g'/>>G</a>
    <a href="/slovnik?prefix=h"<@highlight 'h'/>>H</a>
    <a href="/slovnik?prefix=i"<@highlight 'i'/>>I</a>
    <a href="/slovnik?prefix=j"<@highlight 'j'/>>J</a>
    <a href="/slovnik?prefix=k"<@highlight 'k'/>>K</a>
    <a href="/slovnik?prefix=l"<@highlight 'l'/>>L</a>
    <a href="/slovnik?prefix=m"<@highlight 'm'/>>M</a>
    <a href="/slovnik?prefix=n"<@highlight 'n'/>>N</a>
    <a href="/slovnik?prefix=o"<@highlight 'o'/>>O</a>
    <a href="/slovnik?prefix=p"<@highlight 'p'/>>P</a>
    <a href="/slovnik?prefix=q"<@highlight 'q'/>>Q</a>
    <a href="/slovnik?prefix=r"<@highlight 'r'/>>R</a>
    <a href="/slovnik?prefix=s"<@highlight 's'/>>S</a>
    <a href="/slovnik?prefix=t"<@highlight 't'/>>T</a>
    <a href="/slovnik?prefix=u"<@highlight 'u'/>>U</a>
    <a href="/slovnik?prefix=v"<@highlight 'v'/>>V</a>
    <a href="/slovnik?prefix=w"<@highlight 'w'/>>W</a>
    <a href="/slovnik?prefix=x"<@highlight 'x'/>>X</a>
    <a href="/slovnik?prefix=y"<@highlight 'y'/>>Y</a>
    <a href="/slovnik?prefix=z"<@highlight 'z'/>>Z</a>
</p>

<table border="0" class="siroka bez-slovniku">
 <#list FOUND.data as rel>
  <#if rel_index % 3 == 0><tr></#if>
   <td><a href="${rel.url}">${rel.child.title}</a></td>
  <#if rel_index % 3 == 2></tr></#if>
 </#list>
</table>

<#macro highlight(letter)>
    <#if (CURRENT_PREFIX?default('a')==letter)> class="selected"</#if><#rt>
</#macro>

<#include "../footer.ftl">
