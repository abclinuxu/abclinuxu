<#include "../header.ftl">

<@lib.showMessages/>

<h1>Přiřazení článků k seriálu</h1>

<p>
    V tomto formuláři můžete přiřadit více článků najednou k vybranému seriálu.
    Stačí vložit na samostatné řádky jednotlivé URL adresy článků. URL musí být
    buď absolutní (včetně jména serveru), nebo relativní začínající lomítkem.
</p>

<@lib.addForm URL.noPrefix("/serialy/edit/"+RELATION.id)>
    <@lib.addTextArea true, "url", "URL", 20 />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "addArticlesUrls2" />
</@lib.addForm>

<#include "../footer.ftl">
