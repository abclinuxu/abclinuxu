<@lib.addRTE textAreaId="desc" formId="persForm" menu="wiki" />
<#include "../header.ftl">
<#import "../misc/personality.ftl" as perslib>

<@lib.showMessages/>

<#if PREVIEW??>
 <h2>Náhled záznamu osobnosti</h2>
 <p>
    Prohlédněte si vzhled vašeho záznamu. Opravte chyby a zvolte tlačítko <code>Náhled</code>.
    Pokud jste s výsledkem spokojeni, stiskněte tlačítko <code>Dokonči</code>.
 </p>

 <fieldset>
     <legend>Náhled</legend>
     <@perslib.showPersonality PREVIEW, false />
 </fieldset>
</#if>

<h2>Nápověda</h2>

<p>
    Služba Kdo je kdo shromažďuje informace u důležitých osobnostech světa open source
    a Linuxu. Zadejte prosím jméno a co nejpodrobnější popis této osobnosti. Prosíme, nezadávejte prostřední jména.
    Vhodnou adresou do pole webu s bližšími informacemi jsou osobní stránky dotyčného člověka
    nebo záznam ve wikipedii. Do pole adresy RSS můžete zadat například RSS blogu.
</p>

<h2>Formátování</h2>

<p>
    Smíte používat základní HTML značky. Pokud je nepoužijete,
    prázdné řádky budou nahrazeny novým odstavcem. Více informací
    <a href="#formatovani">najdete</a> pod formulářem.
</p>

<@lib.addForm URL.make("/edit"), "name='persForm'">
    <@lib.addInput true, "firstname", "Jméno" />
    <@lib.addInput true, "surname", "Příjmení" />

    <@lib.addTextArea true, "desc", "Popis", 20>
        <@lib.showRTEControls "desc"/>
    </@lib.addTextArea>

    <@lib.addInput false, "birthDate", "Datum narození" />
    <@lib.addInput false, "deathDate", "Datum úmrtí" />
    <@lib.addInput false, "webUrl", "Webová stránka" />
    <@lib.addInput false, "rssUrl", "Adresa RSS" />

    <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
        <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
            <@lib.addInputBare "rev_descr" />
        </@lib.addFormField>
    </#if>

    <@lib.addFormField>
        <#if PREVIEW??>
            <@lib.addSubmitBare "Zopakuj náhled", "preview" />
            <@lib.addSubmitBare "Dokonči", "finish" />
        <#else>
            <@lib.addSubmitBare "Náhled", "preview" />
            <#if EDIT_MODE!false>
                <@lib.addSubmitBare "Dokonči", "finish" />
            </#if>
        </#if>
    </@lib.addFormField>

    <#if RELATION??>
        <@lib.addHidden "rid", RELATION.id />
    </#if>
    <#if EDIT_MODE!false>
        <@lib.addHidden "action", "edit2" />
    <#else>
        <@lib.addHidden "action", "add2" />
    </#if>
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
