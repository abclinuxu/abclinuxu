<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="desc" formId="dictForm" inputMode="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nový pojem</h1>

<p>
    Napište jméno pojmu a jeho vysvětlení. Pojem by měl být buď linuxový nebo počítačový.
    Jméno pište malými písmeny, velká písmena použijte jen pro ustálené zkratky (například SCSI).
    První znak jména pojmu musí být písmeno (a-z).
    Ze jména se vygeneruje URL adresa (po normalizacích - odstranění všech znaků kromě písmen, čísel,
    tečky a pomlčky, na konci i všech teček a pomlček). Pokud se vám výsledné URL nelíbí, kontaktujte
    administrátory.
</p>

<p>
    Pokud v popisu nepoužijete formatovací znaky &lt;br&gt; nebo &lt;p&gt;, systém automaticky
    nahradí prázdné řádky značkou pro nový odstavec.
</p>

<#if PARAMS.preview?exists>
    <h2>Náhled</h2>

    <fieldset>
        <legend>Náhled</legend>
        <h3>${PARAMS.name?if_exists}</h3>
        <#if PARAMS.desc?exists>
            <div class="dict-item">
            ${TOOL.render(PARAMS.desc,USER?if_exists)}
            </div>
        </#if>
    </fieldset>
</#if>

<form action="${URL.make("/edit")}" method="POST" name="dictForm">
    <table cellpadding="0" border="0" class="siroka">
        <tr>
            <td class="required">Pojem</td>
            <td>
                <input tabindex="1" type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="30" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Popis</td>
            <td>
                <@lib.showError key="desc"/>
                <@rte.showFallback "desc"/>
                <textarea tabindex="2" name="desc" class="siroka" rows="20" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input tabindex="3" type="submit" name="preview" value="Náhled">
                <#if PARAMS.preview?exists><input tabindex="4" type="submit" name="submit" value="Dokonči"></#if>
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
</form>

<#include "../footer.ftl">
