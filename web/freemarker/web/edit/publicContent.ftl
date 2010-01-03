<@lib.addRTE textAreaId="content" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava dokumentu</h1>

<p>Pokud chcete vylepšit obsah dokumentu nebo opravit chybu, jste na
správné adrese. Všechny změny se automaticky ukládají do databáze, takže
je možné prohlížet obsah tohoto dokumentu v průběhu času nebo vrátit
změny zpět.</p>

<#if PREVIEW??>
    <fieldset>
        <legend>Náhled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

<@lib.addForm URL.make("/editContent"), "name='form'">
    <@lib.addInput true, "title", "Titulek stránky" />
    <@lib.addTextArea true, "content", "Obsah stránky", 30>
        <p>Všechna URL na články, obrázky a soubory z našeho serveru musí být relativní!</p>
        <@lib.showRTEControls "content"/>
    </@lib.addTextArea>

    <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
        <@lib.addInputBare "rev_descr" />
    </@lib.addFormField>
    <@lib.addFormField>
        <#if PREVIEW??>
            <@lib.addSubmitBare "Zopakuj náhled", "preview" />
        <#else>
            <@lib.addSubmitBare "Náhled", "preview" />
        </#if>
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "editPublicContent2" />
    <@lib.addHidden "rid", PARAMS.rid!"" />
    <#if PARAMS.startTime??><#assign value=PARAMS.startTime><#else><#assign value=START_TIME?c></#if>
    <@lib.addHidden "startTime", value />
</@lib.addForm>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
