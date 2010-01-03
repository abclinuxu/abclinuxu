<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vložení reklamní pozice</h1>

<p>
    Tato stránka slouží pro definování nové reklamní pozice. Každá pozice
    musí mít své jméno a unikátní identifikátor. Dále je možné zadat popis
    vysvětlující účel a umístění této pozice a hlavní reklamní kód.
    Ten bude zobrazen vždy, nebude-li adresa aktuální stránky obsloužena jiným
    reklamním kódem. Reklamní kód je obvykle HMTL kód reklamní agentury, ale
    může to být libovolný HTML kód včetně odkazů a obrázků.
</p>
<p>
    Po vytvoření nové pozice je nutné, aby programátor přidal do patřičné
    šablony zobrazení této reklamní pozice. To vyžaduje identifikátor
    této pozice. Identifikátor se musí skládat jen z písmen anglické abecedy,
    číslic, pomlčky či podtržítka. Příznak dynamického kódu nastavte jen tehdy,
    obsahuje-li reklamní kód programovací instrukce jazyku Freemarker a musí se
    nejdříve zpracovat.
</p>

<@lib.addForm URL.noPrefix("/EditAdvertisement"), "name='form'">
    <@lib.addInput true, "name", "Jméno", 60 />
    <@lib.addInput true, "newIdentifier", "Identifikátor", 60 />
    <@lib.addTextArea false, "desc", "Popis", 3, "class='siroka'" />
    <@lib.addSubmit "Dokonči", "finish" />

    <@lib.addHidden "action", "addPosition2" />
</@lib.addForm>

<#include "../footer.ftl">
