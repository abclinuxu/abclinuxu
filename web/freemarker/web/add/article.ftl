<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>
<#include "../header.ftl">

<#if PARAMS.action=="add" || PARAMS.action="add2" >
<h1>Přidání článku</h1>
<#else>
<h1>Úprava článku</h1>
</#if>

<@lib.showMessages/>

<#macro selected id><#t>
    <#list PARAMS.authors! as author><#if id?string==author> selected</#if></#list><#t>
</#macro>

<@lib.addForm URL.make("/edit"), "name='theForm'">
    <@lib.addInput true, "title", "Titulek", 60 />

    <#if AUTHORS??>
        <@lib.addSelect true, "authors", "Autor", true>
            <#list AUTHORS as relation>
                <#assign author=relation.child>
                <option value="${relation.id}"<@selected relation.id/>>
                    ${TOOL.childName(author)}
                </option>
            </#list>
        </@lib.addSelect>
    </#if>

    <@lib.addInput false, "published", "Datum publikování", 40>
        <input type="button" id="datetime_btn" value="...">
        <script type="text/javascript">
            Calendar.setup({inputField:"published",ifFormat:"%Y-%m-%d %H:%M",showsTime:true,button:"datetime_btn",singleClick:false,step:1,firstDay:1});
        </script>
    </@lib.addInput>

    <#if SECTIONS??>
        <@lib.addSelect true, "section", "Rubrika">
            <#list SECTIONS as section>
                <@lib.addOption "section", TOOL.childName(section), ""+section.id />
            </#list>
        </@lib.addSelect>
    </#if>

    <@lib.addTextArea true, "perex", "Perex", 4 />
    <@lib.addTextArea true, "content", "Obsah">
        <p>
            Rozdělit článek na více podstránek můžete pomocí následující direktivy: <br>
            <i>&lt;page title="Nastavení programu LILO"&gt;</i> <br>
            Pokud použijete tuto funkci, pojmenujte i první stránku, text před první značkou bude ignorován!
        </p>
        <@lib.addTextAreaEditor "content">
            <a href="javascript:insertAtCursor(document.theForm.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="serif" title="Vložit značku H2">H2</a>
            <a href="javascript:insertAtCursor(document.theForm.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="serif" title="Vložit značku H3">H3</a>
        </@lib.addTextAreaEditor>
    </@lib.addTextArea>

    <@lib.addTextArea false, "related", "Související články", 5>
        <div>
            Zde můžete zadat související články z našeho portálu. Na první řádek vložte
            relativní URL odkazu, na druhý jeho popis. Liché řádky jsou URL, sudé popisy. Popis může obsahovat
            znak |, zbytek textu řádky bude sloužit jako komentář, nebude součástí odkazu.
        </div>
    </@lib.addTextArea>

    <@lib.addTextArea false, "resources", "Zdroje a odkazy", 5>
        <div>
            Zde můžete zadat odkazy a zdroje. Místní URL vkládejte jako relativní! Na první řádek vložte
            URL odkazu, na druhý jeho popis. Liché řádky jsou URL, sudé popisy. Popis může obsahovat
            znak |, zbytek textu řádky bude sloužit jako komentář, nebude součástí odkazu.
        </div>
    </@lib.addTextArea>

    <#if AUTHORS??>
        <@lib.addFormField false, "Volby">
            <@lib.addCheckboxBare "forbid_discussions", "Zakázat diskuze" />
            <@lib.addCheckboxBare "forbid_rating", "Zakázat hodnocení" />
            <@lib.addCheckboxBare "notOnIndex", "Nezobrazovat na hlavní stránce" />
        </@lib.addFormField>
    </#if>

    <@lib.addFormField false, "URL">
        /clanky/nejaka-sekce/<@lib.addInputBare "url" />
        (nepovinné; je-li ponecháno prázdné, systém vygeneruje URL podle názvu článku)
    </@lib.addFormField>

    <@lib.addTextArea false, "thumbnail", "Ikonka", 2>
        <div>
                Pokud chcete, aby se ve výpise článků zobrazovala ikonka, vložte zde její HTML kód.
                Nedávejte zde formátování, to se řeší v šabloně. Jen definici tagu IMG.
        </div>
    </@lib.addTextArea>

    <@lib.addSubmit "Dokonči" />

    <#if PARAMS.action=="add" || PARAMS.action="add2" >
        <@lib.addHidden "action", "add2" />
    <#else>
        <@lib.addHidden "action", "edit2" />
    </#if>
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
