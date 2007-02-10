<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tento formulář slouží pro vkládání nových zápisů do vašeho blogu.
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

<#if PREVIEW?exists>
 <h2>Náhled vašeho zápisu</h2>

 <div style="padding-left: 30pt">
    <h3>${TOOL.xpath(PREVIEW, "/data/name")}</h3>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Přečteno: ${TOOL.getCounterValue(PREVIEW,"read")}x
        <#if PREVIEW.subType?exists>| ${CATEGORIES[PREVIEW.subType]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")?if_exists}
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h2>Zde můžete provést své úpravy</h2>

<form action="${URL.make("/blog/edit/"+STORY.id)}" method="POST" name="form">
<table class="siroka" cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek zápisu</span>
            <a class="info" href="#">?<span class="tooltip">Zde nastavíte titulek vašeho zápisu. Je důležitý pro RSS.</span></a>
            <input type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            Kategorie zápisu
            <a class="info" href="#">?<span class="tooltip">Zde nastavíte kategorii vašeho zápisu. Můžete tak členit zápisy do různých kategorií.</span></a>
            <select name="cid">
                <#list CATEGORIES?keys as category>
                    <option value="${category}"<#if category==PARAMS.cid?default("UNDEF")> selected</#if>>${CATEGORIES[category]}</option>
                </#list>
            </select>
        </td>
    </tr>
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
            <div class="error">${ERRORS.content?if_exists}</div>
            <textarea tabindex="2" name="content" class="siroka" rows="30">${PARAMS.content?if_exists?html}</textarea>
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

<#include "../footer.ftl">
