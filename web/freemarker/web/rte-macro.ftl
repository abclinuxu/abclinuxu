
<#macro addRTE textAreaId formId inputMode commentedText="UNDEFINED">
    <#if commentedText=="UNDEFINED">
        ${TOOL.addRichTextEditor(RTE, textAreaId, formId, inputMode)}
    <#else>
        ${TOOL.addRichTextEditor(RTE, textAreaId, formId, inputMode, commentedText)}
    </#if>
</#macro>

<#macro showFallback textAreaId>
    <#if (! RTE.wysiwygMode) && RTE.displayJavascriptButtons>
        <#local editor = RTE[textAreaId]>
        <div class="form-edit">
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;b&gt;', '&lt;/b&gt;');" class="serif" title="Vložit značku tučně"><b>B</b></a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;i&gt;', '&lt;/i&gt;');" class="serif" title="Vložit značku kurzíva"><i>I</i></a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" class="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;blockquote&gt;', '&lt;/blockquote&gt;');" class="mono" title="Vložit značku citace">BQ</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;p&gt;', '&lt;/p&gt;');" class="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;pre&gt;', '&lt;/pre&gt;');" class="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;code&gt;', '&lt;/code&gt;');" class="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;ul&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;/ul&gt;');" class="mono" title="Vložit nečíslovaný seznam">&lt;ul&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&lt;ol&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;/ol&gt;');" class="mono" title="Vložit číslovaný seznam">&lt;ol&gt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&amp;lt;', '');" class="mono" title="Vložit písmeno &lt;">&lt;</a>
            <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '&amp;gt;', '');" class="mono" title="Vložit písmeno &gt;">&gt;</a>
            <#if editor.commentedContent??>
                <script language="javascript1.2" type="text/javascript">
                original = "<blockquote>${editor.commentedContent?js_string}</blockquote>";
                function cituj(input) {
                    input.value += original;
                }
                </script>
                <a href="javascript:cituj(document.${editor.form}.${editor.id});" class="mono" title="Vloží komentovaný příspěvek jako citaci">Citace</a>
            </#if>
        </div>
    </#if>
</#macro>
