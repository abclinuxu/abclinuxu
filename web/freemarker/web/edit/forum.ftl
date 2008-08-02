<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/edit")}" name="forumForm" method="post">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td class="required">Název</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Popis</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.note, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
                </div>

                <textarea name="note" cols="80" rows="15" tabindex="8">${PARAMS.note?if_exists?html}</textarea>
                <div class="error">${ERRORS.note?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Pravidla</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
                    <a href="javascript:insertAtCursor(document.forumForm.rules, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
                </div>

                <textarea name="rules" cols="80" rows="15" tabindex="8">${PARAMS.rules?if_exists?html}</textarea>
                <div class="error">${ERRORS.rules?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Dokonči"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "../footer.ftl">
