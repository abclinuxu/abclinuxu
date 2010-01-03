<@lib.addRTE textAreaId="content" formId="form" menu="blog" />
<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>
Každý zápis musí mít titulek, který by měl stručně a jasně popisovat,
o čem váš zápis bude pojednávat. Titulek bude zobrazen i ve vašem RSS.
Text zápisu pište ve validním HTML
(<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>,
<a href="http://www.jakpsatweb.cz/html/">příručka</a>).
</p>

<p>Delší příspěvky lze rozdělit na úvodní část, která se zobrazí
ve výpisu a zbytek textu. Při zobrazení zápisu budou obě části automaticky
spojeny do jednoho celku. Pro dělení použijte speciální značku <code>&lt;break&gt;</code>.
Dávejte si pozor na to, aby tato značka nebyla mezi párovými HTML značkami.
Systém zlom vyžaduje až od limitu stopadesáti slov.
</p>

<#if DELAYED>
    <p>Pokud nechcete příspěvek ihned publikovat, použijte tlačítko
    Ulož. Tlačítko Publikuj okamžitě příspěvek zveřejní.
    </p>
</#if>

<#if PREVIEW??>
 <h2>Náhled vašeho zápisu</h2>

 <div style="padding-left: 30pt">
    <h3>${PREVIEW.title!}</h3>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Přečteno: ${TOOL.getCounterValue(PREVIEW,"read")}x
        <#if PREVIEW.subType??>| ${CATEGORIES[PREVIEW.subType]!}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")!}
    ${TOOL.xpath(PREVIEW, "/data/content")!}
 </div>
</#if>

<h2>Zde můžete provést své úpravy</h2>

<@lib.addForm URL.make("/blog/edit/"+STORY.id), "name='form'">
    <@lib.addFormField true, "Titulek zápisu", "Zde nastavíte titulek vašeho zápisu. Je důležitý pro RSS.">
        <@lib.addInputBare "title", 60 />
    </@lib.addFormField>

    <#if (CATEGORIES?size>0)>
    <@lib.addFormField true, "Kategorie zápisku", "Zde nastavíte kategorii vašeho zápisu. Můžete tak členit zápisy do různých kategorií.">
        <@lib.addSelectBare "cid">
            <#list CATEGORIES as category>
                <@lib.addOption "cid", category.name, category.id />
            </#list>
        </@lib.addSelect>
    </#if>

    <#if STORY.child.type==15 || PARAMS.publish??>
        <@lib.addInput "publish", "Datum zveřejnění", 16>
            <input type="button" id="datetime_btn" value="..."><script type="text/javascript">cal_setupDateTime()</script>
            Formát 2005-01-25 07:12
        </@lib.addInput>
    </#if>

    <@lib.addTextArea true, "content", "Obsah zápisu", 30, "class='siroka'">
        <@lib.showRTEControls "content"/>
    </@lib.addTextArea>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <#if DELAYED>
            <@lib.addSubmitBare "Ulož", "delay" />
            <@lib.addSubmitBare "Publikuj", "finish" />
        <#else>
            <@lib.addSubmitBare "Dokonči", "finish" />
        </#if>
    </@lib.addFormField>

    <@lib.addHidden "action", "edit2" />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">
<p>
    Na HTML značky v perexu jsou uplatňována ještě další omezení. Zde nejsou povoleny
    značky B, STRONG, H1-H5 a IMG. Navíc je možné použít tagy P a BR maximálně jednou.
</p>

<#include "../footer.ftl">
