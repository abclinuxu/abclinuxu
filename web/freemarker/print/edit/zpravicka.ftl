<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h2>Co je to zprávièka?</h2>

<p>Zprávièka je krátký text, který upozoròuje na¹e ètenáøe
na zajímavou informaci, stránky èi událost ve svìtì Linuxu,
Open Source, hnutí Free Software èi obecnì IT. Zprávièky
neslou¾í pro soukromou inzerci èi oznámení, firemní oznámení
schvaluje i ma¾e pouze Leo¹ Literák.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Zprávièka by mìla obsahovat pouze text bez formátování, z HTML znaèek
je povolen jen odkaz a pøípadnì paragraf. Formátovací znaèky (font,
italické èi tuèné písmo) a obrázky jsou zapovìzeny.
Pokud u¾ivatel zvolil nevhodnou kategorii, vyberte jinou.</p>

<h1>Náhled</h1>

<#call showNews(RELATION)>

<form action="${URL.make("/EditItem")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Obsah zprávièky</td>
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
   <td>
    <input name="preview" type="submit" value="Náhled">
    <input type="submit" value="Schválit">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
