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

<#if PREVIEW?exists>
 <h1 class="st_nadpis">N�hled va�eho z�pisu</h1>

 <div style="padding-left: 30pt">
    <h2>${TOOL.xpath(PREVIEW, "/data/name")?if_exists}</h2>
    <p class="cl_inforadek">${DATE.show(PREVIEW.created, "CZ_SHORT")} |
        P�e�teno: 0x
        <#if PARAMS.cid?exists>| ${CATEGORIES[PARAMS.cid]?if_exists}</#if>
    </p>
    ${TOOL.xpath(PREVIEW, "/data/content")?if_exists}
 </div>
</#if>

<h1 class="st_nadpis">Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST">
<table cellpadding="5">
    <tr>
        <td>
            <a class="info" href="#">?<span class="tooltip">Zde nastav�te titulek va�eho z�pisu. Je d�le�it� pro RSS.</span></a>	
            <span class="required">Titulek z�pisu</span>
            <input type="text" name="title" size="60" value="${PARAMS.title?if_exists?html}">
            <div class="error">${ERRORS.title?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            <a class="info" href="#">?<span class="tooltip">Zde nastav�te kategorii va�eho z�pisu. M��ete tak �lenit z�pisy do r�zn�ch kategori�.</span></a>	
            Kategorie z�pisu
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
            <textarea name="content" cols="80" rows="30">${PARAMS.content?if_exists?html}</textarea>
            <div class="error">${ERRORS.content?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>
            <#if PREVIEW?exists>
                <input type="submit" name="preview" value="Zopakuj n�hled">
                <input type="submit" name="finish" value="Dokon�i">
            <#else>
                <input type="submit" name="preview" value="N�hled">
            </#if>
        </td>
    </tr>
</table>
<input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
