<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zpr�vi�ka?</h2>

<p>Zpr�vi�ka je kr�tk� text, kter� upozor�uje na�e �ten��e
na zaj�mavou informaci, str�nky �i ud�lost ve sv�t� Linuxu,
Open Source, hnut� Free Software �i obecn� IT. Zpr�vi�ky
neslou�� pro soukromou inzerci �i ozn�men�, firemn� ozn�men�
schvaluje i ma�e pouze <a href="/Profile/1">Leo� Liter�k</a>.
</p>

<h2>Jak ji m�m napsat?</h2>

<p>Zpr�vi�ka by m�la obsahovat pouze text bez form�tov�n�, z HTML zna�ek
je povolen jen odkaz a p��padn� paragraf. Form�tovac� zna�ky (font,
italick� �i tu�n� p�smo) a obr�zky jsou zapov�zeny.
Pokud u�ivatel zvolil nevhodnou kategorii, vyberte jinou.</p>

<h1>N�hled</h1>

<@lib.showNews RELATION />

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Obsah zpr�vi�ky</td>
   <td>
    <textarea name="content" cols="80" rows="15" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <#if USER?exists && USER.hasRole("news admin")>
    <tr>
        <td>Datum zve�ejn�n�</td>
        <td>
            <input type="text" size="16" name="publish" value="${PARAMS.publish?if_exists}">
            (zat�m jen nastav� datum) Form�t 2005-01-25 07:12
            <div class="error">${ERRORS.publish?if_exists}</div>
        </td>
    </tr>
  </#if>
  <tr>
   <td>Kategorie</td>
   <td>
   <#assign selected = PARAMS.category?if_exists>
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
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input name="preview" type="submit" value="N�hled">
    <input type="submit" value="Schv�lit">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
