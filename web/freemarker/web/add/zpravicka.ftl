<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zpr�vi�ka?</h2>

<p>Zpr�vi�ka je kr�tk� text, kter� upozor�uje na�e �ten��e
na zaj�mavou informaci, str�nky �i ud�lost ve sv�t� Linuxu,
Open Source �i IT. Zpr�vi�ky o Microsoftu ma�eme, stejn�
jako dotazy, ��dosti o pomoc �i p�edem nedomluven� reklamy.
</p>

<h2>Jak ji m�m napsat?</h2>

<p>Povolen�mi HTML zna�kami jsou odkaz (A), URL mus� b�t absolutn�
(za��nat http://) a ACRONYM. Obsah v�dy pi�te s h��ky a ��rkami.
Zpr�vi�ka by m�la m�t alespo� dv� v�ty a obsahovat takov� podrobnosti,
aby d�vala smysl, ani� by �lov�k musel nav�t�vit zm�n�n� URL.
Titulek by m�l kr�tce popsat hlavn� t�ma zpr�vi�ky, bude pou�it v RSS
a vygeneruje se z n�j URL (ve v�pisu zpr�vi�ek v�ak zobrazen nebude).</p>

<h2>A co d�le?</h2>

<p>Va�e zpr�vi�ka bude �ekat, ne� ji n�kter� spr�vce schv�l�.
Spr�vce m��e upravit v� text (nap��klad jej doplnit, opravit p�eklep, ...)
nebo zm�nit kategorii. V p��pad� zam�tnut� v�m bude posl�n email
s vysv�tlen�m. Teprve po schv�len� bude zpr�vi�ka zve�ejn�na.</p>

<#if PARAMS.preview?exists>
 <h2>N�hled</h2>
 <@lib.showNews RELATION />
</#if>

<form action="${URL.make("/edit")}" method="POST" name="newsForm">
  <p>
   <span class="required">Titulek</span><br>
   <input tabindex="1" type="text" name="title" size="40" maxlength="50" value="${PARAMS.title?if_exists?html}">
   <div class="error">${ERRORS.title?if_exists}</div>
   <span class="required">Obsah</span>
   <div class="form-edit">
     <a href="javascript:insertAtCursor(document.newsForm.content, '&lt;a href=&quot;&quot;&gt;', '</a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
   </div>
   <textarea tabindex="2" name="content" cols="60" rows="10" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
   <div class="error">${ERRORS.content?if_exists}</div>
  </p>
  <#if USER?exists && USER.hasRole("news admin")>
    Datum zve�ejn�n�: <input type="text" size="16" name="publish" value="${PARAMS.publish?if_exists}">
    (zat�m jen nastav� datum) Form�t 2005-01-25 07:12
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
       <input tabindex="3" name="preview" type="submit" value="N�hled">
       <input tabindex="4" type="submit" value="Dokon�i">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
