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

<h1>Nový zápis do blogu</h1>

<p>Tento formulář slouží pro vkládání nových zápisů do vašeho blogu.
Každý zápis musí mít titulek, který by měl stručně a jasně popisovat,
o čem váš zápis bude pojednávat. Titulek bude zobrazen i ve vašem RSS.
Mějte také na paměti, že z titulku bude automaticky vygenerována
textová adresa pro váš zápisek. Text zápisu pište ve validním HTML
(<a href="http://www.kosek.cz/clanky/html/01.html" rel="nofollow">rychlokurz</a>,
<a href="http://www.jakpsatweb.cz/html/" rel="nofollow">příručka</a>).</p>

<p>Delší příspěvky lze rozdělit na úvodní část, která se zobrazí
ve výpisu a zbytek textu. Při zobrazení zápisu budou obě části automaticky
spojeny do jednoho celku. Pro dělení použijte speciální značku <b><code>&lt;break&gt;</code></b>.
Dávejte si pozor na to, aby tato značka nebyla mezi párovými HTML značkami.
Systém zlom vyžaduje až od limitu stopadesáti slov. Do úvodní části zápisku
(před značku <b><code>&lt;break&gt;</code></b>) prosím nevkládejte velké obrázky
ani prázdné odstavce.</p>

<p>Pokud nechcete příspěvek ihned publikovat, použijte tlačítko
<b>Odlož</b>. Můžete se k příspěvku kdykoliv vrátit a vydat jej, až budete
spokojeni. Příkaz pro publikování odloženého zápisku najdete
v pravém sloupci v části nadepsané <b>Správa zápisku</b>.</p>

<#if PREVIEW??>
 <h2>Náhled vašeho zápisu</h2>

 <div style="padding-left: 30pt">
    <h3>${PREVIEW.title!}</h3>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Přečteno: 0x
        <#if PARAMS.cid??>| ${CATEGORIES[PARAMS.cid]!}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")!}
    ${TOOL.xpath(PREVIEW, "/data/content")!}
 </div>
</#if>

<h2>Zde můžete provést své úpravy</h2>

<@lib.addForm URL.make("/blog/edit/"+REL_BLOG.id), "name='form'", true>
    <@lib.addFormField true, "Titulek zápisu", "Zde nastavíte titulek vašeho zápisu. Je důležitý pro RSS.">
        <@lib.addInputBare "title", 60 />
    </@lib.addFormField>

    <#if (CATEGORIES?size>0)>
        <@lib.addFormField false, "Kategorie zápisu", "Zde nastavíte kategorii vašeho zápisu. Můžete tak členit zápisy do různých kategorií.">
            <@lib.addSelectBare "cid">
                <#list CATEGORIES as category>
                    <@lib.addOption "cid", category.name, category.id />
                </#list>
            </@lib.addSelectBare>
        </@lib.addFormField>
    </#if>

    <@lib.addFormField false, "Aktivovat sledování diskuse", "Zde můžete aktivovat sledování diskuse k tomuto zápisu. Komentáře čtenářů vám budou chodit emailem.">
        <@lib.addCheckboxBare "watchDiz" ""/>
    </@lib.addFormField>
    <@lib.addInput false, "publish", "Datum zveřejnění", 16>
        <input type="button" id="datetime_btn" value="...">
        <script type="text/javascript">
            Calendar.setup({inputField:"publish",ifFormat:"%Y-%m-%d %H:%M",showsTime:true,button:"datetime_btn",singleClick:false,step:1,firstDay:1});
        </script>
        Formát 2005-01-25 07:12
    </@lib.addInput>

    <@lib.addFormField true, "Obsah zápisku">
        Ze souboru <@lib.addFileBare "contentFile" /><@lib.addSubmitBare "Načti", "upload" />

        <@lib.addTextAreaBare "content", 30>
            <@lib.showRTEControls "content"/>
        </@lib.addTextAreaBare>
    </@lib.addFormField>

    <@lib.addFormField>
        <#if PREVIEW??>
            <@lib.addSubmitBare "Zopakuj náhled", "preview" />
            <@lib.addSubmitBare "Publikuj", "finish" />
            <@lib.addSubmitBare "Do konceptů", "delay" />
        <#else>
            <@lib.addSubmitBare "Náhled", "preview" />
            <@lib.addSubmitBare "Do konceptů", "delay" />
        </#if>
    </@lib.addFormField>
    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">
<p>
    Na HTML značky v perexu jsou uplatňována ještě další omezení. Zde nejsou povoleny
    značky B, H1-H5 a IMG. Navíc je možné použít tagy P a BR maximálně jednou.
</p>

<#include "../footer.ftl">
