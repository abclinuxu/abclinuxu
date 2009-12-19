<#include "../../header.ftl">

<#if UNDELETABLE?? >
<@lib.showSignPost "Rozcestník">
<ul>
    <li><a href="${URL.make("/autori/clanky")}" title="Autorovy články">Články</a></li>
</ul>
</@lib.showSignPost>
</#if>

<@lib.showMessages/>

<h2>Smazání autora</h2>

<#if UNDELETABLE??>
    <p>
        Litujeme, ale tohoto autora nelze smazat, protože napsal nejméně jeden článek.
        Chcete-li jej opravdu smazat, musíte nejdříve jeho články přiřadit někomu jinému.
    </p>
<#else>
    <form action="${URL.make("/redakce/autori/edit")}" method="POST">
        <p style="white-space: nowrap">
            Opravdu chcete smazat autora <b>${AUTHOR.title}</b>?
            <input type="submit" name="delete" value="Ano, smazat"/>
            <input type="submit" name="leave" value="Ne, nemazat"/>
            <input type="hidden" name="rid" value="${RELATION.id}"/>
            <input type="hidden" name="action" value="rm2"/>
        </p>
    <form>
</#if>

<#include "../../footer.ftl">