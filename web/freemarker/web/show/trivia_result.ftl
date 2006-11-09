<#include "../header.ftl">

<@lib.showMessages/>

<h1>Va�e sk�re je ${SCORE}</h1>

<p>
    <#if (SCORE == 0)>
        Ouvej, ani jedna spr�vn� odpov��? Jak se v�m to povedlo? Projd�te si spr�vn�
        odpov�di a u dal��ho kv�zu vyb�rejte odpov�di n�hodn�, h��e u� skon�it nem��ete&nbsp;:-).
    <#elseif (SCORE < 4)>
        No, nic moc. Pozorn� si prohl�dn�te spr�vn� odpov�di a zkuste jin� kv�z. T�eba
        budete m�t lep�� mu�ku.
    <#elseif (SCORE <= 8)>
        Zlat� pr�m�r, ale na pochvalu to nen�. Schv�ln� se pod�vejte na va�e chyby.
        Prostor ke zlep�ov�n� zde je, jin� kv�z v�m m��e vyj�t l�pe.
    <#elseif (SCORE == 9)>
        V�born�, jedin� chybi�ka v�s d�lila od dokonalosti. Kter� ot�zka v�s dostala?
        U p��t�ho kv�zu u� tolik sm�ly m�t nemus�te, vysn�n� des�tka na v�s �ek�...
    <#else>
        Uctivost, va�nosti. V�e spr�vn�, jste nejlep��. Aspo� v tomhle kv�zu. Dok�ete
        �sp�ch zopakovat i u dal��ho kv�zu?
    </#if>
</p>

<h2>Shrnut� kv�zu</h2>

<table border="0">
    <tr>
        <td>Jm�no:</td>
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
          <span class="game-correct">Spr�vn�</span><#else>
          <span class="game-wrong">�patn�</span></#if> odpov��: ${result.reply}
    <#if (! result.correct)><br />Spr�vn� odpov�� je:
          <span class="game-reply">${result.correctAnswear}</span></#if></p>
</#list>

<p><a href="/hry">Zp�tky na hry</a></p>

<#include "../footer.ftl">
