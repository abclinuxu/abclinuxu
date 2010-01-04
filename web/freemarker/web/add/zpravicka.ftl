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

<h2>Co je to zprávička?</h2>

<p>Zprávička je krátký text, který upozorňuje naše čtenáře
na zajímavou informaci, stránky či událost ve světě Linuxu,
Open Source či IT. Zprávičky o Microsoftu mažeme, stejně
jako dotazy, žádosti o pomoc či předem nedomluvené reklamy.
</p>

<h2>Jak ji mám napsat?</h2>

<p>
    Pište s háčky a čárkami, jinak vás požádáme o nápravu. Titulek by měl krátce popsat
    hlavní téma zprávičky, bude použit v RSS a vygeneruje se z něj URL.
    Ve výpisu zpráviček však zobrazen nebude, proto vlastní obsah zprávičky musí dávat smysl
    i bez něj. <b>Zprávička by měla mít alespoň dvě věty a obsahovat takové podrobnosti,
    aby dávala smysl, aniž by člověk musel navštívit odkazovanou stránku.</b> Pokud je text příliš
    dlouhý nebo obsahuje více odstavců, bude ve výpise zobrazen jen jeho začátek či první odstavec.
    Povolenými HTML značkami jsou A (odkaz, URL musí být absolutní, tedy začínat na http://),
    ACRONYM, BR a P.
</p>

<h2>A co dále?</h2>

<p>Vaše zprávička bude čekat, než ji některý správce schválí.
Správce může upravit váš text (například jej doplnit, opravit překlep, ...)
nebo změnit kategorii. V případě zamítnutí vám bude poslán email
s vysvětlením. Teprve po schválení bude zprávička zveřejněna.</p>

<#if PARAMS.preview??>
 <h2>Náhled</h2>
 <@lib.showNews RELATION />
<#elseif WAITING_NEWS?size gt 0>
 <h2>Čekající zprávičky</h2>
 <p>
    Následující zprávičky už napsal někdo před vámi a ty nyní čekají na
    schválení administrátorem nebo na svůj čas vydání.
 </p>
 <ul>
    <#list WAITING_NEWS as rel>
        <li>${TOOL.childName(rel)}</li>
    </#list>
 </ul>
</#if>

<@lib.addForm URL.make("/edit"), "name='newsForm'">
    <@lib.addInput true, "title", "Titulek", 40 />
    <@lib.addTextArea true, "content", "Obsah", 10, "cols='60'">
        <@lib.showRTEControls "content"/>
    </@lib.addTextArea>

    <#if USER?? && USER.hasRole("news admin")>
        <@lib.addInput false, "publish", "Datum zveřejnění", 16>
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
    <#assign selected = PARAMS.category!"RELEASE">
    <dl>
        <#list CATEGORIES as category>
            <dt>
                <input type="radio" name="category" value="${category.key}"
                <#if category.key=selected>checked</#if> >
                <b>${category.name}</b>
            </dt>
            <dd>${category.desc}</dd>
        </#list>
    </dl>
    </@lib.addFormField>

    <@lib.addSubmit "Náhled", "preview" />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "../footer.ftl">
