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

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form" enctype="multipart/form-data">
<table class="siroka" cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek zápisu</span>
            <input tabindex="1" type="text" name="title" size="60" value="${PARAMS.title!?html}">&nbsp;
            <@lib.showHelp>Zde nastavíte titulek vašeho zápisu. Je důležitý pro RSS.</@lib.showHelp>
            <@lib.showError key="title" />
        </td>
    </tr>
    <tr>
        <td>
            <#if (CATEGORIES?size>0)>
                Kategorie zápisu:
                <select name="cid">
                    <#list CATEGORIES as category>
                        <option value="${category.id}"<#if category.id==PARAMS.cid!"UNDEF"> selected</#if>>${category.name}</option>
                    </#list>
                </select>&nbsp;
                <@lib.showHelp>Zde nastavíte kategorii vašeho zápisu. Můžete tak členit zápisy do různých kategorií.</@lib.showHelp>
            </#if>
            <label>
                Aktivovat sledování diskuse
                <input type="checkbox" name="watchDiz" value="yes"<#if PARAMS.watchDiz??> checked</#if>>
            </label>
	        <@lib.showHelp>Zde můžete aktivovat sledování diskuse k tomuto zápisu. Komentáře čtenářů vám budou chodit emailem.</@lib.showHelp>
        </td>
    </tr>
    <tr>
        <td>
            Datum zveřejnění
            <input type="text" size="16" name="publish" id="datetime_input" value="${PARAMS.publish!}">
            <input type="button" id="datetime_btn" value="..."><script type="text/javascript">cal_setupDateTime()</script>
            Formát 2005-01-25 07:12
            <@lib.showError key="publish" />
        </td>
    </tr>
    <tr>
        <td>
            <span class="required">Obsah zápisu</span>
            <div>
                Ze souboru
                <input type="file" name="contentFile" size="20" tabindex="3">
                <input tabindex="4" type="submit" name="upload" value="Načti">
            </div>
            <@lib.showError key="contentFile" />
            <@lib.showRTEControls "content"/>
            <@lib.showError key="content" />
            <textarea tabindex="2" name="content" id="content" class="siroka" rows="30">${PARAMS.content!"<p></p>"?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <#if PREVIEW??>
                <input tabindex="5" type="submit" name="preview" value="Zopakuj náhled">
                <input tabindex="6" type="submit" name="finish" value="Publikuj">
                <input tabindex="7" type="submit" name="delay" value="Do konceptů">
            <#else>
                <input tabindex="5" type="submit" name="preview" value="Náhled">
                <input tabindex="6" type="submit" name="delay" value="Do konceptů">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">
<p>
    Na HTML značky v perexu jsou uplatňována ještě další omezení. Zde nejsou povoleny
    značky B, BIG, STRONG, H1-H5 a IMG. Navíc je možné použít tagy P a BR maximálně
    jednou.
</p>

<#include "../footer.ftl">
