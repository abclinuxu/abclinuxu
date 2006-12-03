<#include "../header.ftl">

<@lib.showMessages/>

<#macro selected id><#t>
    <#list PARAMS.authors?if_exists as author><#if id==author> selected</#if></#list><#t>
</#macro>

<form action="${URL.make("/edit")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Titulek</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists?html}" size=60 tabindex=1>
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Autor</td>
   <td>
    <select name="authors" size="6" multiple tabindex=2>
        <#list AUTHORS as relation>
            <#assign author=relation.child>
            <option value="${relation.id}"<@selected relation.id/>>
                ${TOOL.childName(author)}
            </option>
        </#list>
    </select>
    <div class="error">${ERRORS.authors?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Zveøejni dne</td>
   <td>
    <input type="text" name="published" value="${PARAMS.published?if_exists}" size=40 tabindex=3>
    <div class="error">${ERRORS.published?if_exists}</div>
   </td>
  </tr>
  <#if SECTIONS?exists>
      <tr>
       <td width="90" class="required">Rubrika</td>
       <td>
        <select name="section">
            <#list SECTIONS as section>
                <option value="${section.id}"<#if PARAMS.section?default(0)==section.id> selected</#if>>${TOOL.childName(section)}</option>
            </#list>
        </select>
       </td>
      </tr>
  </#if>
  <tr>
   <td width="90" class="required">Perex</td>
   <td>
    <textarea name="perex" cols="100" rows="4" tabindex="4">${PARAMS.perex?if_exists?html}</textarea>
    <div class="error">${ERRORS.perex?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Obsah èlánku</td>
   <td>
    <p>Rozdìlit èlánek na více podstránek mù¾ete pomocí následující direktivy: <br>
    <i>&lt;page title="Nastavení programu LILO"&gt;</i> <br>
    Pokud pou¾ijete tuto funkci, pojmenujte i první stránku, text pøed první znaèkou bude ignorován!</p>

    <p>V¹echna URL na èlánky, obrázky a soubory z na¹eho serveru musí být relativní!</p>

    <textarea name="content" cols="100" rows="45" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Související èlánky</td>
   <td>
    Zde mù¾ete zadat související èlánky z na¹eho portálu. Na první øádek vlo¾te
    relativní URL odkazu, na druhý jeho popis. Liché øádky jsou URL, sudé popisy. Popis mù¾e obsahovat
    znak |, zbytek textu øádky bude slou¾it jako komentáø, nebude souèástí odkazu. <br>
    <textarea name="related" cols="80" rows="5" tabindex="6">${PARAMS.related?if_exists}</textarea>
    <div class="error">${ERRORS.related?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Zdroje a odkazy</td>
   <td>
    Zde mù¾ete zadat odkazy a zdroje. Místní URL vkládejte jako relativní! Na první øádek vlo¾te
    URL odkazu, na druhý jeho popis. Liché øádky jsou URL, sudé popisy. Popis mù¾e obsahovat
    znak |, zbytek textu øádky bude slou¾it jako komentáø, nebude souèástí odkazu. <br>
    <textarea name="resources" cols="80" rows="5" tabindex="7">${PARAMS.resources?if_exists}</textarea>
    <div class="error">${ERRORS.resources?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Volby</td>
   <td>
    <input type="checkbox" name="forbid_discussions" <#if PARAMS.forbid_discussions?exists>checked</#if> value="yes">
    Zakázat diskuse
    <input type="checkbox" name="forbid_rating" <#if PARAMS.forbid_rating?exists>checked</#if> value="yes">
    Zakázat hodnocení
    <input type="checkbox" name="notOnIndex" <#if PARAMS.notOnIndex?exists>checked</#if> value="yes">
    Nezobrazovat na hlavní stránce
   </td>
  </tr>
  <tr>
   <td width="90">Ikonka</td>
   <td>
    Pokud chcete, aby se ve výpise èlánkù zobrazovala ikonka, vlo¾te zde její HTML kód.
    Nedávejte zde formátování, to se øe¹í v ¹ablonì. Jen definici tagu IMG. <br>
    <textarea name="thumbnail" cols="80" rows="5" tabindex="7">${PARAMS.thumbnail?if_exists}</textarea>
    <div class="error">${ERRORS.thumbnail?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td><input type="submit" value="Pokraèuj" tabindex="8"></td>
  </tr>
 </table>

 <#if PARAMS.action=="add" || PARAMS.action="add2" >
  <input type="hidden" name="action" value="add2">
  <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
