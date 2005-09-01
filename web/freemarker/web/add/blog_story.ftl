<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">�vod</h1>

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

<p>Pokud nechcete p��sp�vek ihned publikovat, pou�ijte tla��tko
Odlo�. M��ete se k p��sp�vku kdykoliv vr�tit a vydat jej, a� budete
spokojeni. Najdete jej v prav�m menu.
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
            Kategorie z�pisu:
            <#if (CATEGORIES?size>0)>
                <select name="cid">
                    <#list CATEGORIES?keys as category>
                        <option value="${category}"<#if category==PARAMS.cid?default("UNDEF")> selected</#if>>${CATEGORIES[category]}</option>
                    </#list>
                </select>&nbsp;
            <#else>
                nem�te nastaveny ��dn� kategorie
            </#if>
    	    <a class="info" href="#">?<span class="tooltip">Zde nastav�te kategorii va�eho z�pisu. M��ete tak �lenit z�pisy do r�zn�ch kategori�.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            <label>Aktivovat sledov�n� diskuse
            <input type="checkbox" name="watchDiz" value="yes"<#if PARAMS.watchDiz?exists> checked</#if>></label>
	        <a class="info" href="#">?<span class="tooltip">Zde m��ete aktivovat sledov�n� diskuse
		k tomuto z�pisu. Koment��e �ten��� v�m budou chodit emailem.</span></a>
        </td>
    </tr>
    <tr>
        <td>
            <span class="required">Obsah z�pisu</span>
            <div class="form-edit">
                <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo�it form�tovan� text. Vhodn� pouze pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
		        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
                <a href="javascript:insertAtCursor(document.form.content, '&lt;break&gt;', '');" id="mono" title="Vlo�it zna�ku zlomu">&lt;break&gt;</a>
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
                <input tabindex="5" type="submit" name="delay" value="Odlo�">
            <#else>
                <input tabindex="3" type="submit" name="preview" value="N�hled">
                <input tabindex="4" type="submit" name="delay" value="Odlo�">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>

<p>Povolen� HTML <a href="http://www.w3.org/TR/html4/index/elements.html">zna�ky</a>:
 A, ACRONYM, B, BLOCKQUOTE, BR, CENTER, CITE, CODE, DD, DEL, DIV, DL, DT, EM, IMG, H1, H2, H3, H4, HR, I,
 INS, KBD, LI, OL, P, PRE, Q, SMALL, SPAN, STRONG, SUB, SUP, TABLE, TBODY, TD, TFOOT, TH, THEAD,
 TR, TT, U, UL, VAR. Zna�ky P, PRE, DIV, SPAN, H1-H4 a A povoluj� atrubity ID a CLASS.
</p>

<#include "../footer.ftl">
