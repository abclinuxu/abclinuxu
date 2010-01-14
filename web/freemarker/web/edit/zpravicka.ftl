<@lib.addRTE textAreaId="content" formId="newsForm" menu="news" />
<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava zprávičky</h1>

<h2>Co je to zprávička?</h2>

<p>Zprávička je krátký text, který upozorňuje naše čtenáře
na zajímavou informaci, stránky či událost ve světě Linuxu,
Open Source, hnutí Free Software či obecně IT. Zprávičky
neslouží pro soukromou inzerci či oznámení, firemní oznámení
schvaluje i maže pouze <a href="/Profile/1">Leoš Literák</a>.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Zprávička by měla obsahovat pouze text bez formátování, z HTML značek
je povolen jen odkaz a případně paragraf. Formátovací značky (font,
italické či tučné písmo) a obrázky jsou zapovězeny.
Pokud uživatel zvolil nevhodnou kategorii, vyberte jinou.
Titulek by měl krátce popsat hlavní téma zprávičky, bude použít v RSS
a vygeneruje se z něj URL.</p>

<h2>Náhled</h2>

    <@lib.showNews RELATION />

<@lib.addForm URL.make("/edit"), "name='newsForm'">
    <@lib.addInput true, "title", "Titulek", 40, "maxlength='50'" />
    <@lib.addTextArea true, "content", "Obsah", 15>
        <@lib.showRTEControls "content"/>
    </@lib.addTextArea>

    <#if USER?? && USER.hasRole("news admin")>
        <@lib.addInput false, "publish", "Datum zveřejnění", 15 >
            <input type="button" id="datetime_btn" value="...">
            <script type="text/javascript">
                Calendar.setup({inputField:"publish",ifFormat:"%Y-%m-%d %H:%M",showsTime:true,button:"datetime_btn",singleClick:false,step:1,firstDay:1});
            </script>
            Formát 2005-01-25 07:12
        </@lib.addInput>
        <@lib.addInput false, "uid", "Vydat pod UID", 5 />
        <@lib.addCheckbox "forbidDiscussions", "Zakázat diskuzi" />
    </#if>

    <@lib.addFormField true, "Kategorie">
        <#assign selected = PARAMS.category!>
        <dl>
            <#list CATEGORIES as category>
                <dt>
                    <input type="radio" name="category" value="${category.key}"<#if category.key=selected> checked</#if>>
                    <b>${category.name}</b>
                </dt>
                <dd>${category.desc}</dd>
            </#list>
        </dl>
    </@lib.addFormField>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Uložit" />

        <@lib.addHidden "action", "edit2" />
        <@lib.addHidden "rid", RELATION.id />
    </@lib.addFormField>
</@lib.addForm>

<#include "../footer.ftl">
