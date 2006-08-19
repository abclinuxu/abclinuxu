<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tento formuláø slou¾í pro vkládání nových zápisù do va¹eho blogu.
Ka¾dý zápis musí mít titulek, který by mìl struènì a jasnì popisovat,
o èem vá¹ zápis bude pojednávat. Titulek bude zobrazen i ve va¹em RSS.
Text zápisu pi¹te ve validním HTML
(<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>,
<a href="http://www.jakpsatweb.cz/html/">pøíruèka</a>).
</p>

<p>Del¹í pøíspìvky lze rozdìlit na úvodní èást, která se zobrazí
ve výpisu a zbytek textu. Pøi zobrazení zápisu budou obì èásti automaticky
spojeny do jednoho celku. Pro dìlení pou¾ijte speciální znaèku <code>&lt;break&gt;</code>.
Dávejte si pozor na to, aby tato znaèka nebyla mezi párovými HTML znaèkami.
Systém zlom vy¾aduje a¾ od limitu stopadesáti slov.
</p>

<#if DELAYED>
    <p>Pokud nechcete pøíspìvek ihned publikovat, pou¾ijte tlaèítko
    Ulo¾. Tlaèítko Publikuj okam¾itì pøíspìvek zveøejní.
    </p>
</#if>

<#if PREVIEW?exists>
 <h2>Náhled va¹eho zápisu</h2>

 <div style="padding-left: 30pt">
    <h3>${TOOL.xpath(PREVIEW, "/data/name")}</h3>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Pøeèteno: ${TOOL.getCounterValue(PREVIEW,"read")}x
        <#if PREVIEW.subType?exists>| ${CATEGORIES[PREVIEW.subType]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")?if_exists}
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h2>Zde mù¾ete provést své úpravy</h2>

<form action="${URL.make("/blog/edit/"+STORY.id)}" method="POST" name="form">
<table cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek zápisu</span>
            <a class="info" href="#">?<span class="tooltip">Zde nastavíte titulek va¹eho zápisu. Je dùle¾itý pro RSS.</span></a>
            <input type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            Kategorie zápisu
            <a class="info" href="#">?<span class="tooltip">Zde nastavíte kategorii va¹eho zápisu. Mù¾ete tak èlenit zápisy do rùzných kategorií.</span></a>
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
                <a href="javascript:insertAtCursor(document.form.content, '<b>', '</b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
                <a href="javascript:insertAtCursor(document.form.content, '<i>', '</i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
                <a href="javascript:insertAtCursor(document.form.content, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<p>', '</p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<pre>', '</pre>');" id="mono" title="Vlo¾it formátovaný text. Vhodné pouze pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
		        <a href="javascript:insertAtCursor(document.form.content, '<code>', '</code>');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<break>', '');" id="mono" title="Vlo¾it znaèku zlomu">&lt;break&gt;</a>
            </div>
            <div class="error">${ERRORS.content?if_exists}</div>
            <textarea tabindex="2" name="content" cols="80" rows="30">${PARAMS.content?if_exists?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <input type="submit" name="preview" value="Náhled">
            <#if DELAYED>
                <input tabindex="3" type="submit" name="delay" value="Ulo¾">
                <input tabindex="4" type="submit" name="finish" value="Publikuj">
            <#else>
                <input tabindex="3" type="submit" name="finish" value="Dokonèi">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="edit2">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
