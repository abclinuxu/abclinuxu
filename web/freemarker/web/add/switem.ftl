<#include "../header.ftl">
<#import "../misc/software.ftl" as swlib>

<@lib.showMessages/>

<#if PREVIEW?exists>
 <h2>N�hled softwarov�ho z�znamu</h2>
 <p>
    Prohl�dn�te si vzhled va�eho z�znamu. Opravte chyby a zvolte tla��tko <code>N�hled</code>.
    Pokud jste s v�sledkem spokojeni, stiskn�te tla��tko <code>Dokon�i</code>.
 </p>

 <fieldset>
     <legend>N�hled</legend>
     <@swlib.showSoftware PREVIEW, false />
 </fieldset>
</#if>

<h2>N�pov�da</h2>

<p>
   Zadejte pros�m co nejpodrobn�j�� informace o tomto softwaru. Povinn� polo�ky jsou
   jm�no<#if EDIT_MODE?if_exists> (ze kter�ho se vygeneruje URL)</#if> a popis.
   Prvn� v�ta popisu se zobraz� ve v�pise t�to sekce, proto si na
   jej�m textu dejte z�le�et. Adresa pro sta�en� by nem�la z�viset na konkr�tn� verzi.
   Adresa RSS s aktualitami umo�n� automatick� stahov�n� novinek. 
</p>

<h2>Form�tov�n�</h2>

<p>
    Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
    pr�zdn� ��dky budou nahrazeny nov�m odstavcem. V�ce informac�
    <a href="#formatovani">najdete</a> pod formul��em.
</p>

<form action="${URL.make("/edit")}" method="POST">
    <table width="100%" border="0" cellpadding="5">
        <tr>
            <td class="required">Jm�no</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td class="required">Popis</td>
            <td>
                <textarea name="description" cols="50" rows="11" tabindex="2" class="wide">${PARAMS.description?if_exists?html}</textarea>
                <div class="error">${ERRORS.description?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Adresa domovsk� str�nky</td>
            <td>
                <input type="text" name="homeUrl" value="${PARAMS.homeUrl?if_exists}" size="60" tabindex="3">
                <div class="error">${ERRORS.homeUrl?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Adresa str�nky pro sta�en�</td>
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
            <td class="required">U�ivatelsk� prost�ed�</td>
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

        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <#if PREVIEW?exists>
                    <input type="submit" name="preview" value="Zopakuj n�hled">
                    <input type="submit" name="finish" value="Dokon�i">
                <#else>
                    <input type="submit" name="preview" value="N�hled">
                    <#if EDIT_MODE?if_exists>
                        <input type="submit" name="finish" value="Dokon�i">
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
