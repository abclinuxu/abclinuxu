<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zprávièka?</h2>

<p>Zprávièka je krátký text, který upozoròuje na¹e ètenáøe
na zajímavou informaci, stránky èi událost ve svìtì Linuxu,
Open Source èi IT. Zprávièky o Microsoftu ma¾eme, stejnì
jako dotazy, ¾ádosti o pomoc èi pøedem nedomluvené reklamy.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Povolenými HTML znaèkami jsou odkaz (A), URL musí být absolutní
(zaèínat http://) a ACRONYM. Obsah v¾dy pi¹te s háèky a èárkami.
Zprávièka by mìla mít alespoò dvì vìty a obsahovat takové podrobnosti,
aby dávala smysl, ani¾ by èlovìk musel nav¹tívit zmínìná URL.
Titulek by mìl krátce popsat hlavní téma zprávièky, bude pou¾it v RSS
a vygeneruje se z nìj URL (ve výpisu zprávièek v¹ak zobrazen nebude).</p>

<h2>A co dále?</h2>

<p>Va¹e zprávièka bude èekat, ne¾ ji nìkterý správce schválí.
Správce mù¾e upravit vá¹ text (napøíklad jej doplnit, opravit pøeklep, ...)
nebo zmìnit kategorii. V pøípadì zamítnutí vám bude poslán email
s vysvìtlením. Teprve po schválení bude zprávièka zveøejnìna.</p>

<#if PARAMS.preview?exists>
 <h2>Náhled</h2>
 <@lib.showNews RELATION />
</#if>

<form action="${URL.make("/edit")}" method="POST" name="newsForm">
  <p>
   <span class="required">Titulek</span><br>
   <input tabindex="1" type="text" name="title" size="40" maxlength="50" value="${PARAMS.title?if_exists?html}">
   <div class="error">${ERRORS.title?if_exists}</div>
   <span class="required">Obsah</span>
   <div class="form-edit">
     <a href="javascript:insertAtCursor(document.newsForm.content, '&lt;a href=&quot;&quot;&gt;', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
   </div>
   <textarea tabindex="2" name="content" cols="60" rows="10" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
   <div class="error">${ERRORS.content?if_exists}</div>
  </p>
  <#if USER?exists && USER.hasRole("news admin")>
    Datum zveøejnìní: <input type="text" size="16" name="publish" value="${PARAMS.publish?if_exists}">
    (zatím jen nastaví datum) Formát 2005-01-25 07:12
    <div class="error">${ERRORS.publish?if_exists}</div>
  </#if>
  <h3>Kategorie</h3>
   <#assign selected = PARAMS.category?default("RELEASE")>
   <dl>
   <#list CATEGORIES as category>
    <dt>
     <input type="radio" name="category" value="${category.key}"
     <#if category.key=selected>checked</#if> >
     <b>${category.name}</b>
    </dt>
    <dd>${category.desc}</dd>
   </#list>
   </dl>
   <p>
       <input tabindex="3" name="preview" type="submit" value="Náhled">
       <input tabindex="4" type="submit" value="Dokonèi">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
