<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="desc" formId="persForm" inputMode="wiki" />
<#include "../header.ftl">
<#import "../misc/personality.ftl" as perslib>

<@lib.showMessages/>

<#if PREVIEW?exists>
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

<form action="${URL.make("/edit")}" method="POST" name="persForm">
    <table cellpadding="0" border="0" width="100%">
        <tr>
            <td class="required">Jméno</td>
            <td>
                <input tabindex="1" type="text" name="firstname" value="${PARAMS.firstname?if_exists}" size="30" maxlength="30">
                <div class="error">${ERRORS.firstname?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Příjmení</td>
            <td>
                <input tabindex="2" type="text" name="surname" value="${PARAMS.surname?if_exists}" size="30" maxlength="30">
                <div class="error">${ERRORS.surname?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis</td>
            <td>
                <@lib.showError key="desc"/>
                <@rte.showFallback "desc"/>
                <textarea tabindex="3" name="desc" class="siroka" rows="20" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
            </td>
        </tr>
        <tr>
            <td>Datum narození</td>
            <td>
                <input tabindex="4" type="text" name="birthDate" value="${PARAMS.birthDate?if_exists}" size="30" maxlength="30">
                <div class="error">${ERRORS.birthDate?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Datum úmrtí</td>
            <td>
                <input tabindex="5" type="text" name="deathDate" value="${PARAMS.deathDate?if_exists}" size="30" maxlength="30">
                <div class="error">${ERRORS.deathDate?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Webová stránka</td>
            <td>
                <input tabindex="6" type="text" name="webUrl" value="${PARAMS.webUrl?if_exists}" size="40">
                <div class="error">${ERRORS.webUrl?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Adresa RSS</td>
            <td>
                <input tabindex="7" type="text" name="rssUrl" value="${PARAMS.rssUrl?if_exists}" size="40">
                <div class="error">${ERRORS.rssUrl?if_exists}</div>
            </td>
        </tr>
        <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
            <tr>
                <td>
                    Popis změny
                    <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
                </td>
                <td>
                    <input tabindex="8" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists?html}">
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
    <#if RELATION?exists>
        <input type="hidden" name="rid" value="${RELATION.id}">
    </#if>
    <#if EDIT_MODE?if_exists>
        <input type="hidden" name="action" value="edit2">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
