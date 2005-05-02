<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">�vod</h1>

<p>Tento formul�� slou�� pro vkl�d�n� nov�ch z�pis� do va�eho blogu.
Ka�d� z�pis mus� m�t titulek. Ten by m�l stru�n� a jasn� popisovat,
o �em v� z�pis bude pojedn�vat. Volba titulku ovliv�uje �tennost
va�eho blogu, nebo� titulek bude zobrazen ve va�em RSS. Obsah va�eho
z�pisu pi�te ve validn�m HTML. Pokud tento jednoduch� jazyk neovl�d�te,
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
    <h2>${TOOL.xpath(PREVIEW, "/data/name")?if_exists}</h2>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        P�e�teno: 0x
        <#if PARAMS.cid?exists>| ${CATEGORIES[PARAMS.cid]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/perex")?if_exists}
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h1 class="st_nadpis">Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST" name="form">
<table cellpadding="5">
    <tr>
        <td>
            <span class="required">Titulek z�pisu</span>
            <input tabindex="1" type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">&nbsp;
	        <a class="info" href="#">?<span class="tooltip">Zde nastav�te titulek va�eho z�pisu. Je d�le�it� pro RSS.</span></a>
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            Kategorie z�pisu
            <select name="cid">
                <#list CATEGORIES?keys as category>
                    <option value="${category}"<#if category==PARAMS.cid?default("UNDEF")> selected</#if>>${CATEGORIES[category]}</option>
                </#list>
            </select>&nbsp;
    	    <a class="info" href="#">?<span class="tooltip">Zde nastav�te kategorii va�eho z�pisu. M��ete tak �lenit z�pisy do r�zn�ch kategori�.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            Aktivovat sledov�n� diskuse
            <input type="checkbox" name="watchDiz" value="yes"<#if PARAMS.watchDiz?exists> checked</#if>>
	        <a class="info" href="#">?<span class="tooltip">Zde m��ete aktivovat sledov�n� diskuse k tomuto z�pisu. Ciz� koment��e v�m budou chodit emailem.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            <span class="required">Obsah z�pisu</span>
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
            <textarea tabindex="2" name="content" cols="80" rows="30">${PARAMS.content?default("<p></p>")?html}</textarea>
        </td>
    </tr>
    <tr>
        <td>
            <#if PREVIEW?exists>
                <input tabindex="3" type="submit" name="preview" value="Zopakuj n�hled">
                <input tabindex="4" type="submit" name="finish" value="Dokon�i">
            <#else>
                <input tabindex="3" type="submit" name="preview" value="N�hled">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
