<#include "../header.ftl">

<@lib.showMessages/>

<h1>Va¹e skóre je ${SCORE}</h1>

<p>
    <#if (SCORE == 0)>
        Ouvej, ani jedna správná odpovìï? Jak se vám to povedlo? Projdìte si správné
        odpovìdi a u dal¹ího kvízu vybírejte odpovìdi náhodnì, hùøe u¾ skonèit nemù¾ete&nbsp;:-).
    <#elseif (SCORE < 4)>
        No, nic moc. Pozornì si prohlédnìte správné odpovìdi a zkuste jiný kvíz. Tøeba
        budete mít lep¹í mu¹ku.
    <#elseif (SCORE <= 8)>
        Zlatý prùmìr, ale na pochvalu to není. Schválnì se podívejte na va¹e chyby.
        Prostor ke zlep¹ování zde je, jiný kvíz vám mù¾e vyjít lépe.
    <#elseif (SCORE == 9)>
        Výbornì, jediná chybièka vás dìlila od dokonalosti. Která otázka vás dostala?
        U pøí¹tího kvízu u¾ tolik smùly mít nemusíte, vysnìná desítka na vás èeká...
    <#else>
        Uctivost, va¹nosti. V¹e správnì, jste nejlep¹í. Aspoò v tomhle kvízu. Doká¾ete
        úspìch zopakovat i u dal¹ího kvízu?
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
          <span class="game-wrong">©patná</span></#if> odpovìï: ${result.reply}
    <#if (! result.correct)><br />Správná odpovìï je:
          <span class="game-reply">${result.correctAnswear}</span></#if></p>
</#list>

<p><a href="/hry">Zpátky na hry</a></p>

<#include "../footer.ftl">
