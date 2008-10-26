<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="description" formId="softForm" inputMode="wiki" />
<#include "../header.ftl">
<#import "../misc/software.ftl" as swlib>

<@lib.showMessages/>

<#if PREVIEW?exists>
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
   jméno<#if EDIT_MODE?if_exists> (ze kterého se vygeneruje URL)</#if> a popis.
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

<form action="${URL.make("/edit")}" method="POST" name="softForm">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td class="required">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td class="required">Popis</td>
            <td>
                <@rte.showFallback "description"/>
                <textarea name="description" rows="11" tabindex="2" class="siroka">${PARAMS.description?if_exists?html}</textarea>
                <@lib.showError key="description"/>
            </td>
        </tr>

        <tr>
            <td>Adresa domovské stránky</td>
            <td>
                <input type="text" name="homeUrl" value="${PARAMS.homeUrl?if_exists}" size="60" tabindex="3">
                <div class="error">${ERRORS.homeUrl?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Adresa stránky pro stažení</td>
            <td>
                <input type="text" name="downloadUrl" value="${PARAMS.downloadUrl?if_exists}" size="60" tabindex="4">
                <div class="error">${ERRORS.downloadUrl?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Adresa RSS s novinkami</td>
            <td>
                <input type="text" name="rssUrl" value="${PARAMS.rssUrl?if_exists}" size="60" tabindex="5">
                <div class="error">${ERRORS.rssUrl?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>
                Je alternativou pro<br> tyto programy z Windows
            </td>
            <td>
                <#if PARAMS.alternative?exists>
                    <#list TOOL.asList(PARAMS.alternative) as alternative>
                            <input type="text" name="alternative" value="${alternative}" size="40" tabindex="6"><br/>
                    </#list>
                </#if>
                <input type="text" name="alternative" value="" size="40" tabindex="6"><br/>
                <input type="text" name="alternative" value="" size="40" tabindex="6"><br/>
                <input type="text" name="alternative" value="" size="40" tabindex="6">
                <div class="error">${ERRORS.alternative?if_exists}</div>
            </td>
        </tr>

    	<tr>
            <td class="required">Uživatelské prostředí</td>
            <td>
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
            </td>
        </tr>

        <tr>
            <td>Licence</td>
            <td>
                <@lib.showOption "license", "gpl", LICENSE_PROPERTY["gpl"], "checkbox" />
                <@lib.showOption "license", "lgpl", LICENSE_PROPERTY["lgpl"], "checkbox" />
                <@lib.showOption "license", "bsd", LICENSE_PROPERTY["bsd"], "checkbox" />
                <@lib.showOption "license", "mpl", LICENSE_PROPERTY["mpl"], "checkbox" />
                <@lib.showOption "license", "apl", LICENSE_PROPERTY["apl"], "checkbox" />
                <@lib.showOption "license", "oss", LICENSE_PROPERTY["oss"], "checkbox" />
                <br>
                <@lib.showOption "license", "freeware", LICENSE_PROPERTY["freeware"], "checkbox" />
                <@lib.showOption "license", "commercial", LICENSE_PROPERTY["commercial"], "checkbox" />
                <@lib.showOption "license", "other", LICENSE_PROPERTY["other"], "checkbox" />
            </td>
        </tr>

        <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
            <tr>
                <td>
                    Popis změny
                    <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
                </td>
                <td>
                    <input tabindex="7" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists?html}">
                    <div class="error">${ERRORS.rev_descr?if_exists}</div>
                </td>
            </tr>
        </#if>

        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <#if PREVIEW?exists>
                    <input type="submit" name="preview" value="Zopakuj náhled">
                    <input type="submit" name="finish" value="Dokonči">
                <#else>
                    <input type="submit" name="preview" value="Náhled">
                    <#if EDIT_MODE?if_exists>
                        <input type="submit" name="finish" value="Dokonči">
                    </#if>
                </#if>
            </td>
        </tr>
    </table>

    <input type="hidden" name="rid" value="${RELATION.id}">
    <#if EDIT_MODE?if_exists>
        <input type="hidden" name="action" value="edit2">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
