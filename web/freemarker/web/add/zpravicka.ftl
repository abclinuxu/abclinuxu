<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zprávièka?</h2>

<p>Zprávièka je krátký text, který upozoròuje na¹e ètenáøe
na zajímavou informaci, stránky èi událost ve svìtì Linuxu,
Open Source èi IT. Zprávièky neslou¾í pro dotazy, reklamní
sdìlení musí být pøedem konzultovány s provozovatelem.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Do textového pole napi¹te text va¹í zprávièky. Zprávièka smí
obsahovat pouze text, z HTML znaèek je povolen pouze odkaz.
V¾dy pi¹te s háèky a èárkami. Zprávièka by mìla mít aspoò dvì vìty
a obsahovat podrobnosti, aby dávala smysl i po vyti¹tìní.
Nejsme portál o Windows, tak¾e Microsoft do zprávièek nepatøí.
</p>

<h2>A co dále?</h2>

<p>Va¹e zprávièka bude èekat, ne¾ ji nìkterý správce schválí.
Správce mù¾e upravit vá¹ text (napøíklad jej doplnit, opravit pøeklep ..)
nebo zmìnit kategorii. V pøípadì zamítnutí vám bude poslán email
s vysvìtlením. Teprve po schválení bude zprávièka zveøejnìna.
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
       <input name="preview" type="submit" value="Náhled">
       <input type="submit" value="Dokonèi">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
