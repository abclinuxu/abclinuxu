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

<#if PREVIEW?exists>
 <h1 class="st_nadpis">Náhled va¹eho zápisu</h1>

 <div style="padding-left: 30pt">
    <h2>${TOOL.xpath(PREVIEW, "/data/name")?if_exists}</h2>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Pøeèteno: 0x
        <#if PARAMS.cid?exists>| ${CATEGORIES[PARAMS.cid]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h1 class="st_nadpis">Zde mù¾ete provést své úpravy</h1>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST">
<table cellpadding="5">
    <tr>
        <td>
            <a class="info" href="#">?<span class="tooltip">Zde nastavíte titulek va¹eho zápisu. Je dùle¾itý pro RSS.</span></a>	
            <span class="required">Titulek zápisu</span>
            <input type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            <a class="info" href="#">?<span class="tooltip">Zde nastavíte kategorii va¹eho zápisu. Mù¾ete tak èlenit zápisy do rùzných kategorií.</span></a>	
            Kategorie zápisu
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
            <textarea name="content" cols="80" rows="30">${PARAMS.content?if_exists?html}</textarea>
            <div class="error">${ERRORS.content?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            <#if PREVIEW?exists>
                <input type="submit" name="preview" value="Zopakuj náhled">
                <input type="submit" name="finish" value="Dokonèi">
            <#else>
                <input type="submit" name="preview" value="Náhled">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
