
<#macro addRTE textAreaId formId inputMode commentedText="UNDEFINED">
    <#if commentedText=="UNDEFINED">
        ${TOOL.addRichTextEditor(RTE, textAreaId, formId, inputMode)}
    <#else>
        ${TOOL.addRichTextEditor(RTE, textAreaId, formId, inputMode, commentedText)}
    </#if>
</#macro>

<#macro showRTEFallback textAreaId>
    <#if ! RTE.wysiwygMode && RTE.displayJavascriptButtons>
        <#local editor = RTE['textAreaId']>
        <div class="form-edit">
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;blockquote&gt;', '&lt;/blockquote&gt;');" id="mono" title="Vložit značku citace">BQ</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
            <#if editor.commentedContent?exists>
                <script language="javascript1.2" type="text/javascript">
                original = "<blockquote>" + ${editor.commentedContent?js_string} + "</blockquote>";
                function cituj(input) {
                    input.value += original;
                }
                </script>
                <a href="javascript:cituj(document.${editor.form}.${editor.id});" id="mono" title="Vloží komentovaný příspěvek jako citaci">Citace</a>
            </#if>
        </div>
    </#if>
</#macro>
