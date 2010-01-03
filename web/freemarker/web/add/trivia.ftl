<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.rid??>
    <h1>Úprava kvízu</h1>
<#else>
    <h1>Vložení kvízu</h1>
</#if>

<p>Kvíz je sada desíti otázek s výběrem ze tří odpovědí, přičemž
jedna je správná. Na konci je zobrazen přehled všech otázek,
včetně správných odpovědí. Vyplňte prosím všechna políčka.</p>

<@lib.addForm URL.noPrefix("/EditTrivia")>
    <@lib.addInput true, "title", "Jméno", 40 />
    <@lib.addTextArea true, "desc", "Popis", 4, "class='siroka'" />
    <@lib.addFormField true, "Složitost">
        <@lib.showOption "difficulty", "simple", "jednoduchá", "radio"/>
        <@lib.showOption "difficulty", "normal", "normální", "radio"/>
        <@lib.showOption "difficulty", "hard", "těžká", "radio"/>
        <@lib.showOption "difficulty", "guru", "guru", "radio"/>
    </@lib.addFormField>

    <#list 1..10 as i>
        <@lib.addFormField true, "Otázka "+i>
            <@lib.addFakeForm>
                <@lib.addInput true, "q"+i+"question", "Otázka", 40 />
                <@lib.addInput true, "q"+i+"answear", "Správná odpověď", 40 />
                <@lib.addInput true, "q"+i+"bad1", "Špatná odpověď 1", 40 />
                <@lib.addInput true, "q"+i+"bad2", "Špatná odpověď 2", 40 />
            </@lib.addFakeForm>
        </@lib.addFormField>
    </#list>

    <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
        <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
            <@lib.addInputBare "rev_descr" />
        </@lib.addFormField>
    </#if>

    <@lib.addSubmit "Dokončit" />
    <#if PARAMS.rid??>
        <@lib.addHidden "action", "edit2" />
        <@lib.addHidden "rid", PARAMS.rid />
    <#else>
        <@lib.addHidden "action", "add2" />
    </#if>
</@lib.addForm>

<#include "../footer.ftl">
