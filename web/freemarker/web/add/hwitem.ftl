<@lib.addRTE textAreaId="note" formId="form" menu="wiki" />
<@lib.addRTE textAreaId="setup" formId="form" menu="wiki" />
<@lib.addRTE textAreaId="params" formId="form" menu="wiki" />
<@lib.addRTE textAreaId="identification" formId="form" menu="wiki" />
<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<#if PREVIEW??>
 <h2>Náhled</h2>
 <p>
    Prohlédněte si vzhled vašeho záznamu. Opravte chyby a zvolte tlačítko <code>Náhled</code>.
    Pokud jste s výsledkem spokojeni, stiskněte tlačítko <code>Dokonči</code>.
 </p>

 <div style="padding-left: 30pt">
    <@hwlib.showHardware PREVIEW />
 </div>
</#if>

<h2>Nápověda</h2>

<p>
   Zadejte prosím podrobné informace o tomto druhu hardwaru, zda je vůbec podporován
   a na jaké úrovni, kde je možné najít ovladač, jak jej detekuje Linux, technické
   parametry, váš názor na cenu a postup zprovoznění. U něj je vhodné psát postup,
   který je nezávislý na distribuci, aby byl váš záznam užitečný i lidem, kteří
   si zvolili jinou distribuci.
</p>

<h2>Formátování</h2>

<p>
    Smíte používat základní HTML značky. Pokud je nepoužijete,
    prázdné řádky budou nahrazeny novým odstavcem. Více informací
    <a href="#formatovani">najdete</a> pod formulářem.
</p>

<@lib.addForm URL.make("/edit"), "name='form'">
    <@lib.addInput true, "name", "Jméno", 40 />
    <@lib.addFormField true, "Podpora pod Linuxem">
        <@lib.addRadioChoice "support", "complete", "kompletní" />
        <@lib.addRadioChoice "support", "partial", "částečná" />
        <@lib.addRadioChoice "support", "none", "žádná" />
    </@lib.addFormField>
    <@lib.addFormField true, "Ovladač je dodáván">
        <@lib.addRadioChoice "driver", "kernel", "v jádře" />
        <@lib.addRadioChoice "driver", "xfree", "v X.Org/XFree86" />
        <@lib.addRadioChoice "driver", "maker", "výrobcem" />
        <@lib.addRadioChoice "driver", "other", "někým jiným" />
        <@lib.addRadioChoice "driver", "none", "neexistuje" />
    </@lib.addFormField>
    <@lib.addInput false, "driverUrl", "Adresa ovladače", 60 />
    <@lib.addFormField false, "Zastaralý">
        <@lib.addRadioChoice "outdated", "yes", "ano" />
        <@lib.addRadioChoice "outdated", "", "ne", true />
    </@lib.addFormField>

    <@lib.addTextArea false, "identification", "Identifikace pod Linuxem", 15>
        <div>
            Identifikaci zařízení pod Linuxem se věnuje <a href="/faq/hardware/jak-zjistim-co-mam-za-hardware">FAQ</a>.
            Zadejte jen skutečně relevantní údaje, buďte struční.
        </div>
        <@lib.showRTEControls "identification"/>
    </@lib.addTextArea>

    <@lib.addTextArea false, "params", "Technické parametry", 15>
        <@lib.showRTEControls "params"/>
    </@lib.addTextArea>

    <@lib.addTextArea false, "setup", "Postup zprovoznění", 15>
        <@lib.showRTEControls "setup"/>
    </@lib.addTextArea>

    <@lib.addTextArea false, "note", "Poznámka", 15>
        <@lib.showRTEControls "note"/>
    </@lib.addTextArea>

    <@lib.addFormField>
        <#if PREVIEW??>
            <@lib.addSubmitBare "Zopakuj náhled", "preview" />
            <@lib.addSubmitBare "Dokonči", "finish" />
        <#else>
            <@lib.addSubmitBare "Náhled", "preview" />
        </#if>
    </@lib.addFormField>

    <@lib.addHidden "rid", RELATION.id />
    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
