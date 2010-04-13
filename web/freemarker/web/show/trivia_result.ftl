<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vaše skóre je ${SCORE}</h1>

<p>
    <#if (SCORE == 0)>
        Ouvej, ani jedna správná odpověď? Jak se vám to povedlo? Projděte si správné odpovědi a u dalšího kvízu vybírejte odpovědi náhodně, hůře už skončit nemůžete&nbsp;:-).
    <#elseif (SCORE < 4)>
        No, nic moc. Pozorně si prohlédněte správné odpovědi a zkuste jiný kvíz. Třeba budete mít lepší mušku.
    <#elseif (SCORE <= 8)>
        Zlatý průměr, ale na pochvalu to není. Schválně se podívejte na vaše chyby. Prostor ke zlepšování zde je, jiný kvíz vám může vyjít lépe.
    <#elseif (SCORE == 9)>
        Výborně, jediná chybička vás dělila od dokonalosti. Která otázka vás dostala? U příštího kvízu už tolik smůly mít nemusíte, vysněná desítka na vás čeká...
    <#else>
        Uctivost, vašnosti. Vše správně, jste nejlepší. Aspoň v tomhle kvízu. Dokážete úspěch zopakovat i u dalšího kvízu?
    </#if>
</p>

<h2>Shrnutí kvízu</h2>

<table border="0">
    <tr>
        <td>Jméno:</td>
        <td>${TOOL.childName(RELATION)}</td>
    </tr>
    <tr>
        <td>Popis:</td>
        <td>${TOOL.xpath(RELATION.child, "/data/description")}</td>
    </tr>
</table>

<#list RESULTS as result>
    <div class="game-results"><b>${result_index+1}.</b> ${result.question}</div>
    <p><#if result.correct>
          <span class="game-correct">Správná</span><#else>
          <span class="game-wrong">Špatná</span></#if> odpověď: ${result.reply}
    <#if (! result.correct)><br />Správná odpověď je:
          <span class="game-reply">${result.correctAnswear}</span></#if></p>
</#list>

<p><a href="/hry">Zpátky na hry</a></p>

<#include "../footer.ftl">
