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
obsahovat pouze text, z HTML znaèek je povolen pouze odkaz a
pøípadnì nový odstavec. Doporuèujeme, aby zprávièka mìla aspoò dvì vìty,
jinak bude pøíli¹ krátká. Pokud bude zprávièka pøíli¹ chudá na
informace nebo nebude obsahovat diakritiku (háèky a èárky),
administrátoøi vás po¾ádají o doplnìní. Jsme portál o Linuxu,
tak¾e zprávièky ze svìta Microsoftu nás opravdu nezajímají.
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
 <h1 class="st_nadpis">Náhled</h1>
 <@lib.showNews RELATION />
</#if>

<form action="${URL.make("/edit")}" method="POST">
  <p>
   <span class="required">Obsah zprávièky</span><br>
   <textarea name="content" cols="60" rows="10" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
   <div class="error">${ERRORS.content?if_exists}</div>
  </p>
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
       <input name="preview" type="submit" value="Náhled">
       <input type="submit" value="Dokonèi">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
