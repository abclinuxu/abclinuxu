<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h2>Co je to zprávièka?</h2>

<p>Zprávièka je krátký text, který upozoròuje na¹e ètenáøe
na zajímavou informaci, stránky èi událost ve svìtì Linuxu,
Open Source, hnutí Free Software èi obecnì IT. Zprávièky
neslou¾í pro soukromou inzerci èi oznámení, firemní oznámení
musí být pøedem schváleny provozovatelem portálu.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Do textového pole napi¹te text va¹í zprávièky. Z HTML znaèek
mù¾ete pou¾ívat napøíklad A pro odkazy, formátovací znaèky (FONT,
italické èi tuèné písmo) a obrázky IMG jsou zapovìzeny. Doporuèujeme,
aby zprávièka mìla aspoò dvì vìty a nejménì 20 slov, jinak bude
pøíli¹ krátká a nebude vypadat hezky. Sna¾te se podat ètenáøùm dostatek
informací, aby shledali va¹i zprávièku u¾iteènou. Na druhou stranu
nepi¹te romány, jeden odstavec vìt¹inou bohatì staèí.
</p>

<p>Po sepsání textu zprávièky si vyberte kategorii, do které
bude zprávièka zaøazena. Buïte prosím pøesní, pøidáte tak
va¹í zprávièce na kvalitì. U¾ivatelé pak budou moci pou¾ívat
filtry pøi vyhledávání.
</p>

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
   <td><input type="submit" value="Schválit"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="relationId" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
