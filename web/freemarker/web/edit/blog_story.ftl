<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">�vod</h1>

<p>Tento formul�� slou�� pro �pravu existuj�c�ch z�pis� ve va�em blogu.
Titulek by m�l stru�n� a jasn� popisovat, o �em v� z�pis bude pojedn�vat.
Obsah va�eho z�pisu pi�te ve validn�m HTML. Pokud tento jednoduch� jazyk neovl�d�te,
p�e�t�te si <a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>
od Jirky Koska.
</p>

<p>Pokud p�ete del�� p��sp�vek, m�li byste jej rozd�lit na �vod
a zbytek textu. U�in�te tak vlo�en�m speci�ln� zna�ky <code>&lt;break&gt;</code>
kdekoliv do textu z�pisu. Text p�ed zna�kou se bude zobrazovat
jako upout�vka na v� p��sp�vek, dohromady se pak zobraz� na str�nce
tohoto z�pisu. Nicm�n� pokud p�ete jen p�r odstavc�, nen� t�eba
text l�mat. Syst�m zlom vy�aduje a� od limitu stopades�ti slov.
</p>

<#if PREVIEW?exists>
 <h1 class="st_nadpis">N�hled va�eho z�pisu</h1>

 <div style="padding-left: 30pt">
    <h2>${TOOL.xpath(PREVIEW, "/data/name")}</h2>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        P�e�teno: ${TOOL.getCounterValue(PREVIEW.child)}x
        <#if PREVIEW.subType?exists>| ${CATEGORIES[PREVIEW.subType]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/content")}
 </div>
</#if>

<h1 class="st_nadpis">Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/blog/edit/"+STORY.id)}" method="POST" name="form">
<table cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek z�pisu</span>
            <a class="info" href="#">?<span class="tooltip">Zde nastav�te titulek va�eho z�pisu. Je d�le�it� pro RSS.</span></a>
            <input type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            Kategorie z�pisu
            <a class="info" href="#">?<span class="tooltip">Zde nastav�te kategorii va�eho z�pisu. M��ete tak �lenit z�pisy do r�zn�ch kategori�.</span></a>
            <select name="cid">
                <#list CATEGORIES?keys as category>
                    <option value="${category}"<#if category==PARAMS.cid?default("UNDEF")> selected</#if>>${CATEGORIES[category]}</option>
                </#list>
            </select>
        </td>
    </tr>
    <tr>
        <td>
            <p class="required">Obsah z�pisu</p>
            <a href="javascript:insertAtCursor(document.form.content, '<b></b>');" title="Vlo�it zna�ku tu�n�"><img src="/images/actions/bold.gif" width="16" height="16" border="0" alt="Vlo�it zna�ku tu�n�"></a>
            <a href="javascript:insertAtCursor(document.form.content, '<i></i>');" title="Vlo�it zna�ku italic"><img src="/images/actions/italic.gif" width="16" height="16" border="0" alt="Vlo�it zna�ku italic"></a>
            <a href="javascript:insertAtCursor(document.form.content, '<u></u>');" title="Vlo�it zna�ku podtrhnout"><img src="/images/actions/under.gif" width="16" height="16" border="0" alt="Vlo�it zna�ku podtrhnout"></a>
            <a href="javascript:insertAtCursor(document.form.content, '<break>');" title="Vlo�it zna�ku zlomu"><img src="/images/actions/story_break.gif" width="16" height="16" border="0" alt="Vlo�it zna�ku zlomu"></a>
            <br>
            <textarea name="content" cols="80" rows="30">${PARAMS.content?if_exists?html}</textarea>
            <div class="error">${ERRORS.content?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            <input type="submit" name="preview" value="N�hled">
            <input type="submit" name="finish" value="Dokon�i">
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="edit2">
</form>


<#include "../footer.ftl">
