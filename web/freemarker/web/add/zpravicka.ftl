<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zpr�vi�ka?</h2>

<p>Zpr�vi�ka je kr�tk� text, kter� upozor�uje na�e �ten��e
na zaj�mavou informaci, str�nky �i ud�lost ve sv�t� Linuxu,
Open Source �i IT. Zpr�vi�ky neslou�� pro dotazy, reklamn�
sd�len� mus� b�t p�edem konzultov�ny s provozovatelem.
</p>

<h2>Jak ji m�m napsat?</h2>

<p>Do textov�ho pole napi�te text va�� zpr�vi�ky. Zpr�vi�ka sm�
obsahovat pouze text, z HTML zna�ek je povolen pouze odkaz.
V�dy pi�te s h��ky a ��rkami. Zpr�vi�ka by m�la m�t aspo� dv� v�ty
a obsahovat podrobnosti, aby d�vala smysl i po vyti�t�n�.
Nejsme port�l o Windows, tak�e Microsoft do zpr�vi�ek nepat��.
</p>

<h2>A co d�le?</h2>

<p>Va�e zpr�vi�ka bude �ekat, ne� ji n�kter� spr�vce schv�l�.
Spr�vce m��e upravit v� text (nap��klad jej doplnit, opravit p�eklep ..)
nebo zm�nit kategorii. V p��pad� zam�tnut� v�m bude posl�n email
s vysv�tlen�m. Teprve po schv�len� bude zpr�vi�ka zve�ejn�na.
</p>

<#if PARAMS.preview?exists>
 <h1 class="st_nadpis">N�hled</h1>
 <@lib.showNews RELATION />
</#if>

<form action="${URL.make("/edit")}" method="POST">
  <p>
   <span class="required">Obsah zpr�vi�ky</span><br>
   <textarea name="content" cols="60" rows="10" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
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
       <input name="preview" type="submit" value="N�hled">
       <input type="submit" value="Dokon�i">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
