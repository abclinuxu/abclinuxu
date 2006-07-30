<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tato stránka slou¾í ke zmìnì jména blogu. Jedná se
o docela delikátní operaci, zvlá¹tì pokud je vá¹ blog ji¾
veøejnì znám. Tímto úkonem se toti¾ zmìní URL, pøes které
je vá¹ blog pøístupný. Odkazy, které mají va¹i ètenáøi
èi známí ve svých zálo¾kách, se tedy stanou neplatnými.
Pokud jste si skuteènì jisti, zmìnit jméno mù¾ete ní¾e.</p>

<p>Jméno mù¾e obsahovat jen písmena anglické abecedy, èíslice
a podtr¾ítko (navíc první písmeno nesmí být èíslice). Na velikosti písmen
nezále¾í. Napøíklad pojmenujete-li si blog snehulak, jeho
adresa bude www.abclinuxu.cz/blog/snehulak. Jméno
blogu mù¾ete pozdìji zmìnit, pøestane-li vám vyhovovat.</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST">
    <p>
        <span class="required">Jméno</span>
        <input type="text" name="blogName" size="40" maxlength="70" value="${PARAMS.blogName?if_exists?html}">
        <input type="submit" name="finish" value="Dokonèi">
    </p>
    <div class="error">${ERRORS.blogName?if_exists}</div>
    <input type="hidden" name="action" value="rename2">
</form>

<#include "../footer.ftl">
