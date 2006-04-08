<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zprávièka?</h2>

<p>Zprávièka je krátký text, který upozoròuje na¹e ètenáøe
na zajímavou informaci, stránky èi událost ve svìtì Linuxu,
Open Source, hnutí Free Software èi obecnì IT. Zprávièky
neslou¾í pro soukromou inzerci èi oznámení, firemní oznámení
schvaluje i ma¾e pouze <a href="/Profile/1">Leo¹ Literák</a>.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Zprávièka by mìla obsahovat pouze text bez formátování, z HTML znaèek
je povolen jen odkaz a pøípadnì paragraf. Formátovací znaèky (font,
italické èi tuèné písmo) a obrázky jsou zapovìzeny.
Pokud u¾ivatel zvolil nevhodnou kategorii, vyberte jinou.
Titulek by mìl krátce popsat hlavní téma zprávièky, bude pou¾ít v RSS
a vygeneruje se z nìj URL.</p>

<h1>Náhled</h1>

    <h2>${TOOL.xpath(RELATION.child,"/data/title")?if_exists}</h2>
    <@lib.showNews RELATION />

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="5" border="0">
  <tr>
   <td class="required">Titulek</td>
   <td>
       <input type="text" name="title" size="40" maxlength="50" value="${PARAMS.title?if_exists}">
       <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Obsah</td>
   <td>
    <textarea name="content" cols="80" rows="15" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <#if USER?exists && USER.hasRole("news admin")>
    <tr>
        <td>Datum zveøejnìní</td>
        <td>
            <input type="text" size="16" name="publish" value="${PARAMS.publish?if_exists}">
            (zatím jen nastaví datum) Formát 2005-01-25 07:12
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
    <input name="preview" type="submit" value="Náhled">
    <input type="submit" value="Ulo¾it">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
