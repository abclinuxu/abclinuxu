<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Úvod</h1>

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

<p>Pokud nechcete pøíspìvek ihned publikovat, pou¾ijte tlaèítko
Odlo¾. Mù¾ete se k pøíspìvku kdykoliv vrátit a vydat jej, a¾ budete
spokojeni. Najdete jej v pravém menu.
</p>

<#if PREVIEW?exists>
 <h1 class="st_nadpis">Náhled va¹eho zápisu</h1>

 <div style="padding-left: 30pt">
    <h2>${TOOL.xpath(PREVIEW, "/data/name")?if_exists}</h2>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Pøeèteno: 0x
        <#if PARAMS.cid?exists>| ${CATEGORIES[PARAMS.cid]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")?if_exists}
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h1 class="st_nadpis">Zde mù¾ete provést své úpravy</h1>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
<table cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek zápisu</span>
            <input tabindex="1" type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">&nbsp;
	        <a class="info" href="#">?<span class="tooltip">Zde nastavíte titulek va¹eho zápisu. Je dùle¾itý pro RSS.</span></a>
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            Kategorie zápisu:
            <#if (CATEGORIES?size>0)>
                <select name="cid">
                    <#list CATEGORIES?keys as category>
                        <option value="${category}"<#if category==PARAMS.cid?default("UNDEF")> selected</#if>>${CATEGORIES[category]}</option>
                    </#list>
                </select>&nbsp;
            <#else>
                nemáte nastaveny ¾ádné kategorie
            </#if>
    	    <a class="info" href="#">?<span class="tooltip">Zde nastavíte kategorii va¹eho zápisu. Mù¾ete tak èlenit zápisy do rùzných kategorií.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            <label>Aktivovat sledování diskuse
            <input type="checkbox" name="watchDiz" value="yes"<#if PARAMS.watchDiz?exists> checked</#if>></label>
	        <a class="info" href="#">?<span class="tooltip">Zde mù¾ete aktivovat sledování diskuse
		k tomuto zápisu. Komentáøe ètenáøù vám budou chodit emailem.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            <span class="required">Obsah zápisu</span>
            <div class="form-edit">
                <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo¾it formátovaný text. Vhodné pouze pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
		        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;break&gt;', '');" id="mono" title="Vlo¾it znaèku zlomu">&lt;break&gt;</a>
            </div>
            <div class="error">${ERRORS.content?if_exists}</div>
            <textarea tabindex="2" name="content" cols="80" rows="30">${PARAMS.content?default("<p></p>")?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <#if PREVIEW?exists>
                <input tabindex="3" type="submit" name="preview" value="Zopakuj náhled">
                <input tabindex="4" type="submit" name="finish" value="Dokonèi">
                <input tabindex="5" type="submit" name="delay" value="Odlo¾">
            <#else>
                <input tabindex="3" type="submit" name="preview" value="Náhled">
                <input tabindex="4" type="submit" name="delay" value="Odlo¾">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>

<p>Povolené HTML <a href="http://www.w3.org/TR/html4/index/elements.html">znaèky</a>:
 A, ACRONYM, B, BLOCKQUOTE, BR, CENTER, CITE, CODE, DD, DEL, DIV, DL, DT, EM, IMG, H1, H2, H3, H4, HR, I,
 INS, KBD, LI, OL, P, PRE, Q, SMALL, SPAN, STRONG, SUB, SUP, TABLE, TBODY, TD, TFOOT, TH, THEAD,
 TR, TT, U, UL, VAR. Znaèky P, PRE, DIV, SPAN, H1-H4 a A povolují atrubity ID a CLASS.
</p>

<#include "../footer.ftl">
