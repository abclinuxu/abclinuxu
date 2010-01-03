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

<@lib.addForm URL.make("/blog/edit/"+REL_BLOG.id)>
    <@lib.addInput true, "blogname", "Jméno", 40 />
    <@lib.addSubmit "Dokonči", "finish" />
    <@lib.addHidden "action", "rename2" />
</@lib.addForm>

<#include "../footer.ftl">
