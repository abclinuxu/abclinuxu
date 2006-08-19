<#include "../header.ftl">

<@lib.showMessages/>

<h2>�vod</h2>

<p>Tento formul�� slou�� pro vkl�d�n� nov�ch z�pis� do va�eho blogu.
Ka�d� z�pis mus� m�t titulek, kter� by m�l stru�n� a jasn� popisovat,
o �em v� z�pis bude pojedn�vat. Titulek bude zobrazen i ve va�em RSS.
Text z�pisu pi�te ve validn�m HTML
(<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>,
<a href="http://www.jakpsatweb.cz/html/">p��ru�ka</a>).
</p>

<p>Del�� p��sp�vky lze rozd�lit na �vodn� ��st, kter� se zobraz�
ve v�pisu a zbytek textu. P�i zobrazen� z�pisu budou ob� ��sti automaticky
spojeny do jednoho celku. Pro d�len� pou�ijte speci�ln� zna�ku <code>&lt;break&gt;</code>.
D�vejte si pozor na to, aby tato zna�ka nebyla mezi p�rov�mi HTML zna�kami.
Syst�m zlom vy�aduje a� od limitu stopades�ti slov.
</p>

<#if DELAYED>
    <p>Pokud nechcete p��sp�vek ihned publikovat, pou�ijte tla��tko
    Ulo�. Tla��tko Publikuj okam�it� p��sp�vek zve�ejn�.
    </p>
</#if>

<#if PREVIEW?exists>
 <h2>N�hled va�eho z�pisu</h2>

 <div style="padding-left: 30pt">
    <h3>${TOOL.xpath(PREVIEW, "/data/name")}</h3>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        P�e�teno: ${TOOL.getCounterValue(PREVIEW,"read")}x
        <#if PREVIEW.subType?exists>| ${CATEGORIES[PREVIEW.subType]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")?if_exists}
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h2>Zde m��ete prov�st sv� �pravy</h2>

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
            <div class="form-edit">
                <a href="javascript:insertAtCursor(document.form.content, '<b>', '</b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
                <a href="javascript:insertAtCursor(document.form.content, '<i>', '</i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
                <a href="javascript:insertAtCursor(document.form.content, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<p>', '</p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<pre>', '</pre>');" id="mono" title="Vlo�it form�tovan� text. Vhodn� pouze pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
		        <a href="javascript:insertAtCursor(document.form.content, '<code>', '</code>');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '<break>', '');" id="mono" title="Vlo�it zna�ku zlomu">&lt;break&gt;</a>
            </div>
            <div class="error">${ERRORS.content?if_exists}</div>
            <textarea tabindex="2" name="content" cols="80" rows="30">${PARAMS.content?if_exists?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <input type="submit" name="preview" value="N�hled">
            <#if DELAYED>
                <input tabindex="3" type="submit" name="delay" value="Ulo�">
                <input tabindex="4" type="submit" name="finish" value="Publikuj">
            <#else>
                <input tabindex="3" type="submit" name="finish" value="Dokon�i">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="edit2">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
