<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h2>Co je to zpr�vi�ka?</h2>

<p>Zpr�vi�ka je kr�tk� text, kter� upozor�uje na�e �ten��e
na zaj�mavou informaci, str�nky �i ud�lost ve sv�t� Linuxu,
Open Source, hnut� Free Software �i obecn� IT. Zpr�vi�ky
neslou�� pro soukromou inzerci �i ozn�men�, firemn� ozn�men�
mus� b�t p�edem schv�leny provozovatelem port�lu.
</p>

<h2>Jak ji m�m napsat?</h2>

<p>Do textov�ho pole napi�te text va�� zpr�vi�ky. Z HTML zna�ek
m��ete pou��vat nap��klad A pro odkazy, form�tovac� zna�ky (FONT,
italick� �i tu�n� p�smo) a obr�zky IMG jsou zapov�zeny. Doporu�ujeme,
aby zpr�vi�ka m�la aspo� dv� v�ty a nejm�n� 20 slov, jinak bude
p��li� kr�tk� a nebude vypadat hezky. Sna�te se podat �ten���m dostatek
informac�, aby shledali va�i zpr�vi�ku u�ite�nou. Na druhou stranu
nepi�te rom�ny, jeden odstavec v�t�inou bohat� sta��.
</p>

<p>Po seps�n� textu zpr�vi�ky si vyberte kategorii, do kter�
bude zpr�vi�ka za�azena. Bu�te pros�m p�esn�, p�id�te tak
va�� zpr�vi�ce na kvalit�. U�ivatel� pak budou moci pou��vat
filtry p�i vyhled�v�n�.
</p>

<form action="${URL.make("/EditItem")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Obsah zpr�vi�ky</td>
   <td>
    <textarea name="content" cols="80" rows="15" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Kategorie</td>
   <td>
   <#global selected = PARAMS.category?if_exists>
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
   <td><input type="submit" value="Schv�lit"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="relationId" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
