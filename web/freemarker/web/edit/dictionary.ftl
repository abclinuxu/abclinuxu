<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="desc" formId="dictForm" inputMode="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava pojmu</h1>

<p>
    Zadejte jméno pojmu a jeho vysvětlení. Pojem by měl být buď linuxový nebo počítačový. Pokud je
    potřeba, upravte jméno pojmu. Mělo by být psáno malými písmeny, velká písmena použijte jen pro
    ustálené zkratky (například SCSI). První znak jména pojmu musí být písmeno (a-z).
    URL se při úpravě nemění, pokud se vám nelíbí, kontaktujte administrátory.
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
    <table cellpadding="0" border="0" width="100%">
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
                <@rte.showFallback "desc"/>
                <textarea tabindex="2" name="desc" class="siroka" rows="20" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <@lib.showError key="desc"/>
            </td>
        </tr>
        <tr>
            <td>
                Popis změny
                <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
            </td>
            <td>
               <input tabindex="3" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists?html}">
               <div class="error">${ERRORS.rev_descr?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <input tabindex="4" type="submit" name="preview" value="Náhled">
                <input tabindex="5" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "../footer.ftl">
