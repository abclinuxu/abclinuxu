<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nový zápis do blogu</h1>

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

<p>Pokud nechcete příspěvek ihned publikovat, použijte tlačítko
Odlož. Můžete se k příspěvku kdykoliv vrátit a vydat jej, až budete
spokojeni. Najdete jej v pravém menu.
</p>

<#if PREVIEW?exists>
 <h2>Náhled vašeho zápisu</h2>

 <div style="padding-left: 30pt">
    <h2>${TOOL.xpath(PREVIEW, "/data/name")?if_exists}</h2>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Přečteno: 0x
        <#if PARAMS.cid?exists>| ${CATEGORIES[PARAMS.cid]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")?if_exists}
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h2>Zde můžete provést své úpravy</h2>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form" enctype="multipart/form-data">
<table class="siroka" cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek zápisu</span>
            <input tabindex="1" type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">&nbsp;
	        <a class="info" href="#">?<span class="tooltip">Zde nastavíte titulek vašeho zápisu. Je důležitý pro RSS.</span></a>
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            Kategorie zápisu:
            <#if (CATEGORIES?size>0)>
                <select name="cid">
                    <#list CATEGORIES as category>
                        <option value="${category.id}"<#if category.id==PARAMS.cid?default("UNDEF")> selected</#if>>${category.name}</option>
                    </#list>
                </select>&nbsp;
            <#else>
                nemáte nastaveny žádné kategorie
            </#if>
    	    <a class="info" href="#">?<span class="tooltip">Zde nastavíte kategorii vašeho zápisu. Můžete tak členit zápisy do různých kategorií.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            <label>Aktivovat sledování diskuse
            <input type="checkbox" name="watchDiz" value="yes"<#if PARAMS.watchDiz?exists> checked</#if>></label>
	        <a class="info" href="#">?<span class="tooltip">Zde můžete aktivovat sledování diskuse
		k tomuto zápisu. Komentáře čtenářů vám budou chodit emailem.</span></a>
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
            <div class="error">${ERRORS.contentFile?if_exists}</div>
            <div class="form-edit">
                <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit formátovaný text. Vhodné pouze pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
		        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;break&gt;', '');" id="mono" title="Vložit značku zlomu">&lt;break&gt;</a>
            </div>
            <div class="error">${ERRORS.content?if_exists}</div>
            <textarea tabindex="2" name="content" class="siroka" rows="30">${PARAMS.content?default("<p></p>")?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <#if PREVIEW?exists>
                <input tabindex="5" type="submit" name="preview" value="Zopakuj náhled">
                <input tabindex="6" type="submit" name="finish" value="Dokonči">
                <input tabindex="7" type="submit" name="delay" value="Odlož">
            <#else>
                <input tabindex="5" type="submit" name="preview" value="Náhled">
                <input tabindex="6" type="submit" name="delay" value="Odlož">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
