<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Smazání šablony smlouvy</h2>

<#if UNDELETABLE??>
    <p>
        Tato šablona nejde smazat, protože již byla podepsána nejméně jedním autorem.
    </p>
<#else>
    <p>
        Chystáte se smazat šablonu smlouvy '${CONTRACT_TEMPLATE.title}'. Taková akce má smysl v případě omylu.
        Šablony, které již někdo podepsal, nejde mazat.
    </p>

    <form action="${URL.make("/sprava/redakce/smlouvy/edit")}" method="POST">
        <p style="white-space: nowrap">
            Opravdu chcete smazat tut smlouvu?
            <input type="submit" name="delete" value="Ano, smazat"/>
            <input type="submit" name="leave" value="Ne, nemazat"/>
            <input type="hidden" name="rid" value="${RELATION.id}"/>
            <input type="hidden" name="action" value="rm2"/>
        </p>
    <form>
</#if>

<#include "../../footer.ftl">