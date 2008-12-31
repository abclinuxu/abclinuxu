<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tato stránka slouží ke změně jména blogu. Jedná se
o docela delikátní operaci, zvláště pokud je váš blog již
veřejně znám. Tímto úkonem se totiž změní URL, přes které
je váš blog přístupný. Odkazy, které mají vaši čtenáři
či známí ve svých záložkách, se tedy stanou neplatnými.
Pokud jste si skutečně jisti, změnit jméno můžete níže.</p>

<p>Jméno může obsahovat jen písmena anglické abecedy, číslice
a podtržítko (navíc první písmeno nesmí být číslice). Na velikosti písmen
nezáleží. Například pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. Jméno
blogu můžete později změnit, přestane-li vám vyhovovat.</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST">
    <p>
        <span class="required">Jméno</span>
        <input type="text" name="blogName" size="40" maxlength="70" value="${PARAMS.blogName!?html}">
        <input type="submit" name="finish" value="Dokonči">
    </p>
    <div class="error">${ERRORS.blogName!}</div>
    <input type="hidden" name="action" value="rename2">
</form>

<#include "../footer.ftl">
