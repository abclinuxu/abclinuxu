<@lib.addRTE textAreaId="description" formId="softForm" menu="wiki" />
<#include "../header.ftl">
<#import "../misc/software.ftl" as swlib>

<@lib.showMessages/>

<#if PREVIEW??>
 <h2>Náhled softwarového záznamu</h2>
 <p>
    Prohlédněte si vzhled vašeho záznamu. Opravte chyby a zvolte tlačítko <code>Náhled</code>.
    Pokud jste s výsledkem spokojeni, stiskněte tlačítko <code>Dokonči</code>.
 </p>

 <fieldset>
     <legend>Náhled</legend>
     <@swlib.showSoftware PREVIEW, false />
 </fieldset>
</#if>

<h2>Nápověda</h2>

<p>
   Zadejte prosím co nejpodrobnější informace o tomto softwaru. Povinné položky jsou
   jméno<#if EDIT_MODE!false> (ze kterého se vygeneruje URL)</#if> a popis.
   První věta popisu se zobrazí ve výpise této sekce, proto si na
   jejím textu dejte záležet. Adresa pro stažení by neměla záviset na konkrétní verzi.
   Adresa RSS s aktualitami umožní automatické stahování novinek.
</p>

<h2>Formátování</h2>

<p>
    Smíte používat základní HTML značky. Pokud je nepoužijete,
    prázdné řádky budou nahrazeny novým odstavcem. Více informací
    <a href="#formatovani">najdete</a> pod formulářem.
</p>

<@lib.addForm URL.make("/edit"), "name='softForm'">
    <@lib.addInput true, "name", "Jméno", 0 />
    <@lib.addTextArea true, "description", "Popis", 20>
        <@lib.showRTEControls "description"/>
    </@lib.addTextArea>

    <@lib.addInput false, "homeUrl", "Adresa domovské stránky", 60 />
    <@lib.addInput false, "downloadUrl", "Adresa stránky pro stažení", 60 />
    <@lib.addInput false, "rssUrl", "Adresa RSS s novinkami", 60 />

    <@lib.addFormField false, "Je alternativou pro<br/> tyto programy z Windows">
        <#if PARAMS.alternative??>
            <#list TOOL.asList(PARAMS.alternative) as alternative>
                    <input type="text" name="alternative" value="${alternative}" size="40" tabindex="6"><br/>
            </#list>
        </#if>
        <input type="text" name="alternative" value="" size="40" tabindex="6"><br/>
        <input type="text" name="alternative" value="" size="40" tabindex="6"><br/>
        <input type="text" name="alternative" value="" size="40" tabindex="6">
    </@lib.addFormField>

    <@lib.addFormField true, "Uživatelské prostředí">
        <div class="sw-strom" id="strom">
            <div>
                <@lib.showOption "ui", "xwindows", UI_PROPERTY["xwindows"], "checkbox" />
                <div>
                    <@lib.showOption "ui", "qt", UI_PROPERTY["qt"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                    <div>
                        <@lib.showOption "ui", "kde", UI_PROPERTY["kde"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                    </div>
                </div>
                <div>
                    <@lib.showOption "ui", "gtk", UI_PROPERTY["gtk"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                    <div>
                        <@lib.showOption "ui", "gnome", UI_PROPERTY["gnome"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                    </div>
                </div>
                <div>
                    <@lib.showOption "ui", "motif", UI_PROPERTY["motif"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                </div>
                <div>
                    <@lib.showOption "ui", "java", UI_PROPERTY["java"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                </div>
                <div>
                    <@lib.showOption "ui", "tk", UI_PROPERTY["tk"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                </div>
            </div>
            <div>
                <@lib.showOption "ui", "console", UI_PROPERTY["console"], "checkbox" />
                <div>
                    <@lib.showOption "ui", "cli", UI_PROPERTY["cli"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                </div>
                <div>
                    <@lib.showOption "ui", "tui", UI_PROPERTY["tui"], "checkbox", " onclick=\"startCheckParent(event);\"" />
                </div>
                <div>
                    <@lib.showOption "ui", "grconsole", UI_PROPERTY["grconsole"], "checkbox", "onclick=\"startCheckParent(event);\"" />
                </div>
            </div>
            <div>
                <@lib.showOption "ui", "web", UI_PROPERTY["web"], "checkbox" />
            </div>
        </div>
    </@lib.addFormField>

    <@lib.addFormField false, "Licence">
        <@lib.showOption "license", "gpl", LICENSE_PROPERTY["gpl"], "checkbox" />
        <@lib.showOption "license", "lgpl", LICENSE_PROPERTY["lgpl"], "checkbox" />
        <@lib.showOption "license", "bsd", LICENSE_PROPERTY["bsd"], "checkbox" />
        <@lib.showOption "license", "mpl", LICENSE_PROPERTY["mpl"], "checkbox" />
        <@lib.showOption "license", "apl", LICENSE_PROPERTY["apl"], "checkbox" />
        <@lib.showOption "license", "oss", LICENSE_PROPERTY["oss"], "checkbox" />
        <br/>
        <@lib.showOption "license", "freeware", LICENSE_PROPERTY["freeware"], "checkbox" />
        <@lib.showOption "license", "commercial", LICENSE_PROPERTY["commercial"], "checkbox" />
        <@lib.showOption "license", "other", LICENSE_PROPERTY["other"], "checkbox" />
    </@lib.addFormField>

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
