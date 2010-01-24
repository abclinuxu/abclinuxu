<@lib.addRTE textAreaId="content" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vkládání dokumentu</h1>

<p>Tento formulář slouží pro vkládání obsahu. Obvykle jde jen
o obyčejný text, který má pevné, hezké URL. Například nápověda,
podmínky užití či reklama. Obsah ale může být i dynamický,
pak však potřebuje podporu programátora, který připraví data.</p>

<#assign content = TOOL.xpath(PREVIEW,"/data/content")!"UNDEFINED">
<#if PREVIEW?? && content != "UNDEFINED">
    <fieldset>
        <legend>Náhled</legend>
        <#if (PARAMS.execute!"no")!="yes">
            ${content}
        <#else>
            ${content?interpret}
        </#if>
    </fieldset>
</#if>

<@lib.addForm URL.make("/editContent"), "name='form'">
    <@lib.addInput true, "title", "Titulek stránky" />
    <@lib.addInput true, "url", "Adresa stránky", "Zadejte absolutní, ale lokální URL." />
    <#if USER.hasRole("root")>
        <@lib.addFormField false, "Layout", "Vyberte rozvržení stránky pro tento dokument">
            <@lib.addRadioChoice "layout", "default", "standardní"/>
            <@lib.addRadioChoice "layout", "no_columns", "bez sloupců"/>
        </@lib.addFormField>
        <@lib.addFormField false, "Dynamický obsah", "Zaškrtněte, pokud se má obsah zpracovat šablonovacím jazykem Freemarker">
            <@lib.addCheckboxBare "execute", ""/>
        </@lib.addFormField>
        <#--<@lib.addInput false, "java_class", "Java FQCN", "Jméno třídy controlleru, který připraví data pro tuto stránku." />-->
    </#if>
    <@lib.addTextArea true, "content", "Obsah stránky", 30>
        <p>Všechna URL na články, obrázky a soubory z našeho serveru musí být relativní!</p>
        <@lib.showRTEControls "content"/>
    </@lib.addTextArea>

    <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
        <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
            <@lib.addInputBare "rev_descr" />
        </@lib.addFormField>
    </#if>

    <@lib.addFormField>
        <#if PREVIEW??>
            <@lib.addSubmitBare "Zopakuj náhled", "preview" />
        <#else>
            <@lib.addSubmitBare "Náhled", "preview" />
        </#if>
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <#if PARAMS.action=="add" || PARAMS.action="add2" >
        <@lib.addHidden "action", "add2" />
    <#else>
        <@lib.addHidden "action", "edit2" />
        <#if PARAMS.startTime??><#assign value=PARAMS.startTime><#else><#assign value=START_TIME?c></#if>
        <@lib.addHidden "startTime", value />
    </#if>
    <@lib.addHidden "rid", PARAMS.rid!"" />
</@lib.addForm>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
