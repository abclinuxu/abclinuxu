<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zprávièka?</h2>

<p>Zprávièka je krátký text, který upozoròuje na¹e ètenáøe
na zajímavou informaci, stránky èi událost ve svìtì Linuxu,
Open Source èi obecnì IT. Zprávièky neslou¾í pro soukromou
inzerci èi oznámení, firemní oznámení musí být pøedem schváleny
provozovatelem portálu.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Do textového pole napi¹te text va¹í zprávièky. Zprávièka smí
obsahovat pouze text (vèetnì diakritiky), z HTML znaèek
je povolen pouze odkaz a pøípadnì nový odstavec. Doporuèujeme,
aby zprávièka mìla aspoò dvì vìty, jinak bude pøíli¹ krátká.
Sna¾te se podat ètenáøùm dostatek informací, ana druhou stranu
nepi¹te romány, jeden odstavec vìt¹inou bohatì staèí.
</p>

<p>Po sepsání textu zprávièky si vyberte kategorii, do které
bude zprávièka zaøazena. Buïte prosím pøesní, pøidáte tak
va¹í zprávièce na kvalitì. U¾ivatelé pak budou moci pou¾ívat
filtry pøi vyhledávání.
</p>

<h2>A co dále?</h2>

<p>Va¹e zpávièka bude ulo¾ena do systému do neveøejné kategorie
a tam bude èekat, ne¾ ji nìkterý ná¹ správce schválí èi zamítne.
Správce mù¾e upravit vá¹ text (napøíklad jej zpøesnit èi doplnit,
opravit pøeklep èi pravopisnou chybu apod.) nebo zmìnit kategorii.
V pøípadì zamítnutí vám bude poslán email s vysvìtlením. Teprve
po schválení bude zprávièka zveøejnìna.
</p>

<#if PARAMS.preview?exists>
 <h1>Náhled</h1>
 <@lib.showNews RELATION />
</#if>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Obsah zprávièky</td>
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
    <input name="preview" type="submit" value="Náhled">
    <input type="submit" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
