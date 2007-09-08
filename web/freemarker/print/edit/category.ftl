<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/EditCategory")}" method="POST" name="abcForm">

    <table width="100" border="0" cellpadding="5">
        <tr>
            <td width="120" class="required">Jméno sekce</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120" class="required">Typ</td>
            <td>
                <select name="type" tabindex="2">
                    <option value="software"<#if PARAMS.type?if_exists=="software"> SELECTED</#if>>Sekce software</option>
                    <option value="hardware"<#if PARAMS.type?if_exists=="hardware"> SELECTED</#if>>Sekce hardware</option>
                    <option value="forum"<#if PARAMS.type?if_exists=="forum"> SELECTED</#if>>Diskusní fórum</option>
                    <option value="faq"<#if PARAMS.type?if_exists=="faq"> SELECTED</#if>>Sekce FAQ</option>
                    <option value="section"<#if PARAMS.type?if_exists=="section"> SELECTED</#if>>Rubrika</option>
                    <option value="blog"<#if PARAMS.type?if_exists=="blog"> SELECTED</#if>>Blog</option>
                    <option value="generic"<#if PARAMS.type?default("generic")=="generic"> SELECTED</#if>>Sekce</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="120">Podtyp</td>
            <td>
                <input type="text" name="subtype" value="${PARAMS.subtype?if_exists}" tabindex="3">
            </td>
        </tr>
        <tr>
            <td width="120">Otevřená</td>
            <td>
                <input type="checkbox" name="open" value="true"<#if PARAMS.open?default("")=="true"> checked</#if> tabindex="4">
            </td>
        </tr>
        <tr>
            <td width="120">Relation.upper</td>
            <td>
                <input type="text" name="upper" value="${PARAMS.upper?if_exists}" tabindex="5">
                <div class="error">${ERRORS.upper?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120">Poznámka</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
                    <a href="javascript:insertAtCursor(document.abcForm.note, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
                </div>
                <textarea name="note" cols="80" rows="15" tabindex="6">${PARAMS.note?if_exists?html}</textarea>
                <div class="error">${ERRORS.note?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td><input type="submit" VALUE="Dokonči" TABINDEX="7"></td>
        </tr>
    </table>

    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
    <input type="hidden" name="url" value="${URL.make("/EditCategory")}">
</form>


<#include "../footer.ftl">
