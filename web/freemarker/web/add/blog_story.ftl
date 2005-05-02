<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Úvod</h1>

<p>Tento formuláø slou¾í pro vkládání nových zápisù do va¹eho blogu.
Ka¾dý zápis musí mít titulek. Ten by mìl struènì a jasnì popisovat,
o èem vá¹ zápis bude pojednávat. Volba titulku ovlivòuje ètennost
va¹eho blogu, nebo» titulek bude zobrazen ve va¹em RSS. Obsah va¹eho
zápisu pi¹te ve validním HTML. Pokud tento jednoduchý jazyk neovládáte,
pøeètìte si <a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>
od Jirky Koska.
</p>

<p>Pokud pí¹ete del¹í pøíspìvek, mìli byste jej rozdìlit na úvod
a zbytek textu. Uèiníte tak vlo¾ením speciální znaèky <code>&lt;break&gt;</code>
kdekoliv do textu zápisu. Text pøed znaèkou se bude zobrazovat
jako upoutávka na vá¹ pøíspìvek, dohromady se pak zobrazí na stránce
tohoto zápisu. Nicménì pokud pí¹ete jen pár odstavcù, není tøeba
text lámat. Systém zlom vy¾aduje a¾ od limitu stopadesáti slov.
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
            Kategorie zápisu
            <select name="cid">
                <#list CATEGORIES?keys as category>
                    <option value="${category}"<#if category==PARAMS.cid?default("UNDEF")> selected</#if>>${CATEGORIES[category]}</option>
                </#list>
            </select>&nbsp;
    	    <a class="info" href="#">?<span class="tooltip">Zde nastavíte kategorii va¹eho zápisu. Mù¾ete tak èlenit zápisy do rùzných kategorií.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            Aktivovat sledování diskuse
            <input type="checkbox" name="watchDiz" value="yes"<#if PARAMS.watchDiz?exists> checked</#if>>
	        <a class="info" href="#">?<span class="tooltip">Zde mù¾ete aktivovat sledování diskuse k tomuto zápisu. Cizí komentáøe vám budou chodit emailem.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            <span class="required">Obsah zápisu</span>
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
            <textarea tabindex="2" name="content" cols="80" rows="30">${PARAMS.content?default("<p></p>")?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <#if PREVIEW?exists>
                <input tabindex="3" type="submit" name="preview" value="Zopakuj náhled">
                <input tabindex="4" type="submit" name="finish" value="Dokonèi">
            <#else>
                <input tabindex="3" type="submit" name="preview" value="Náhled">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
