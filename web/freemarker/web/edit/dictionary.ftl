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
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
                    <a href="javascript:insertAtCursor(document.dictForm.desc, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
                </div>

                <textarea tabindex="2" name="desc" class="siroka" rows="20" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Popis této změny</td>
            <td>
               <input tabindex="3" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists}">
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
