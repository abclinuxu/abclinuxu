<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Úvod</h1>

<p>Tento formuláø slou¾í pro úpravu existujících zápisù ve va¹em blogu.
Titulek by mìl struènì a jasnì popisovat, o èem vá¹ zápis bude pojednávat.
Obsah va¹eho zápisu pi¹te ve validním HTML. Pokud tento jednoduchý jazyk neovládáte,
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
    <h2>${TOOL.xpath(PREVIEW, "/data/name")}</h2>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        Pøeèteno: ${TOOL.getCounterValue(PREVIEW.child)}x
        <#if PREVIEW.subType?exists>| ${CATEGORIES[PREVIEW.subType]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/content")}
 </div>
</#if>

<h1 class="st_nadpis">Zde mù¾ete provést své úpravy</h1>

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
            <a href="javascript:insertAtCursor(document.form.content, '<b></b>');" title="Vlo¾it znaèku tuènì"><img src="/images/actions/bold.gif" width="16" height="16" border="0" alt="Vlo¾it znaèku tuènì"></a>
            <a href="javascript:insertAtCursor(document.form.content, '<i></i>');" title="Vlo¾it znaèku italic"><img src="/images/actions/italic.gif" width="16" height="16" border="0" alt="Vlo¾it znaèku italic"></a>
            <a href="javascript:insertAtCursor(document.form.content, '<u></u>');" title="Vlo¾it znaèku podtrhnout"><img src="/images/actions/under.gif" width="16" height="16" border="0" alt="Vlo¾it znaèku podtrhnout"></a>
            <a href="javascript:insertAtCursor(document.form.content, '<break>');" title="Vlo¾it znaèku zlomu"><img src="/images/actions/story_break.gif" width="16" height="16" border="0" alt="Vlo¾it znaèku zlomu"></a>
            <br>
            <textarea name="content" cols="80" rows="30">${PARAMS.content?if_exists?html}</textarea>
            <div class="error">${ERRORS.content?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            <input type="submit" name="preview" value="Náhled">
            <input type="submit" name="finish" value="Dokonèi">
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="edit2">
</form>


<#include "../footer.ftl">
