<@lib.addRTE textAreaId="content" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vkládání dokumentu</h1>

<p>Tento formulář slouží pro vložení nového dokumentu pod existující
dokument. Typické použití bude, když chcete rozčlenit text na více
stránek nebo do kapitol. Věnujte pečlivou pozornost volbě titulku,
neboť URL se bude skládat z URL nadřazeného dokumentu a titulku
zbaveného diakritiky a speciálních znaků. Toto URL už běžnými prostředky
nebudete moci změnit.
</p>

<#if PREVIEW??>
    <fieldset>
        <legend>Náhled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

<@lib.addForm URL.make("/editContent")>
    <@lib.addInput true, "title", "Titulek stránky", 60 />
    <@lib.addTextArea true, "content", "Obsah stránky", 30>
        <@lib.showRTEControls "content"/>
    </@lib.addTextArea>
    <@lib.addFormField>
        <#if PREVIEW??>
            <@lib.addSubmitBare "Zopakuj náhled", "preview" />
        <#else>
            <@lib.addSubmitBare "Náhled", "preview" />
        </#if>
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "addDerivedPage2" />
    <@lib.addHidden "rid", PARAMS.rid />
</@lib.addForm>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
