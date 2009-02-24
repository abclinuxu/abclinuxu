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

<form action="${URL.make("/blog/edit/"+STORY.id)}" method="POST" name="form">
<table class="siroka" cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek zápisu</span>
            <@lib.showHelp>Zde nastavíte titulek vašeho zápisu. Je důležitý pro RSS.</@lib.showHelp>
            <input type="text" name="title" size="60" value="${PARAMS.title!?html}">
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
        </td>
    </tr>
    <#if STORY.child.type==15 || PARAMS.publish??>
        <tr>
            <td>
                <div>Datum zveřejnění</div>
                <div>
                    <input type="text" size="16" name="publish" id="datetime_input" value="${PARAMS.publish!}">
                    <input type="button" id="datetime_btn" value="..."><script type="text/javascript">cal_setupDateTime()</script>
                    Formát 2005-01-25 07:12
                    <@lib.showError key="publish" />
                </div>
            </td>
        </tr>
    </#if>
    <tr>
        <td>
            <p class="required">Obsah zápisu</p>
            <div class="form-edit">
                <a href="javascript:insertAtCursor(document.form.content, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                <a href="javascript:insertAtCursor(document.form.content, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                <a href="javascript:insertAtCursor(document.form.content, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<pre>', '</pre>');" id="mono" title="Vložit formátovaný text. Vhodné pouze pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
		        <a href="javascript:insertAtCursor(document.form.content, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<break>', '');" id="mono" title="Vložit značku zlomu">&lt;break&gt;</a>
            </div>
            <@lib.showError key="content" />
            <textarea tabindex="2" name="content" class="siroka" rows="30">${PARAMS.content!?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <input type="submit" name="preview" value="Náhled">
            <#if DELAYED>
                <input tabindex="3" type="submit" name="delay" value="Ulož">
                <input tabindex="4" type="submit" name="finish" value="Publikuj">
            <#else>
                <input tabindex="3" type="submit" name="finish" value="Dokonči">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="edit2">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">
<p>
    Na HTML značky v perexu jsou uplatňována ještě další omezení. Zde nejsou povoleny
    značky B, BIG, STRONG, H1-H5 a IMG. Navíc je možné použít tagy P a BR maximálně
    jednou.
</p>

<#include "../footer.ftl">
