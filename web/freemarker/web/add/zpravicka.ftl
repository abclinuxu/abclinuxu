<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zpr�vi�ka?</h2>

<p>Zpr�vi�ka je kr�tk� text, kter� upozor�uje na�e �ten��e
na zaj�mavou informaci, str�nky �i ud�lost ve sv�t� Linuxu,
Open Source �i obecn� IT. Zpr�vi�ky neslou�� pro soukromou
inzerci �i ozn�men�, firemn� ozn�men� mus� b�t p�edem schv�leny
provozovatelem port�lu.
</p>

<h2>Jak ji m�m napsat?</h2>

<p>Do textov�ho pole napi�te text va�� zpr�vi�ky. Zpr�vi�ka sm�
obsahovat pouze text (v�etn� diakritiky), z HTML zna�ek
je povolen pouze odkaz a p��padn� nov� odstavec. Doporu�ujeme,
aby zpr�vi�ka m�la aspo� dv� v�ty, jinak bude p��li� kr�tk�.
Sna�te se podat �ten���m dostatek informac�, ana druhou stranu
nepi�te rom�ny, jeden odstavec v�t�inou bohat� sta��.
</p>

<p>Po seps�n� textu zpr�vi�ky si vyberte kategorii, do kter�
bude zpr�vi�ka za�azena. Bu�te pros�m p�esn�, p�id�te tak
va�� zpr�vi�ce na kvalit�. U�ivatel� pak budou moci pou��vat
filtry p�i vyhled�v�n�.
</p>

<h2>A co d�le?</h2>

<p>Va�e zp�vi�ka bude ulo�ena do syst�mu do neve�ejn� kategorie
a tam bude �ekat, ne� ji n�kter� n� spr�vce schv�l� �i zam�tne.
Spr�vce m��e upravit v� text (nap��klad jej zp�esnit �i doplnit,
opravit p�eklep �i pravopisnou chybu apod.) nebo zm�nit kategorii.
V p��pad� zam�tnut� v�m bude posl�n email s vysv�tlen�m. Teprve
po schv�len� bude zpr�vi�ka zve�ejn�na.
</p>

<#if PARAMS.preview?exists>
 <h1>N�hled</h1>
 <@lib.showNews RELATION />
</#if>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Obsah zpr�vi�ky</td>
   <td>
    <textarea name="content" cols="60" rows="10" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Kategorie</td>
   <td>
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
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input name="preview" type="submit" value="N�hled">
    <input type="submit" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
