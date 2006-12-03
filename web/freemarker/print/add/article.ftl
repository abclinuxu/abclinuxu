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
   <td width="90" class="required">Zve�ejni dne</td>
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
   <td width="90" class="required">Obsah �l�nku</td>
   <td>
    <p>Rozd�lit �l�nek na v�ce podstr�nek m��ete pomoc� n�sleduj�c� direktivy: <br>
    <i>&lt;page title="Nastaven� programu LILO"&gt;</i> <br>
    Pokud pou�ijete tuto funkci, pojmenujte i prvn� str�nku, text p�ed prvn� zna�kou bude ignorov�n!</p>

    <p>V�echna URL na �l�nky, obr�zky a soubory z na�eho serveru mus� b�t relativn�!</p>

    <textarea name="content" cols="100" rows="45" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Souvisej�c� �l�nky</td>
   <td>
    Zde m��ete zadat souvisej�c� �l�nky z na�eho port�lu. Na prvn� ��dek vlo�te
    relativn� URL odkazu, na druh� jeho popis. Lich� ��dky jsou URL, sud� popisy. Popis m��e obsahovat
    znak |, zbytek textu ��dky bude slou�it jako koment��, nebude sou��st� odkazu. <br>
    <textarea name="related" cols="80" rows="5" tabindex="6">${PARAMS.related?if_exists}</textarea>
    <div class="error">${ERRORS.related?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Zdroje a odkazy</td>
   <td>
    Zde m��ete zadat odkazy a zdroje. M�stn� URL vkl�dejte jako relativn�! Na prvn� ��dek vlo�te
    URL odkazu, na druh� jeho popis. Lich� ��dky jsou URL, sud� popisy. Popis m��e obsahovat
    znak |, zbytek textu ��dky bude slou�it jako koment��, nebude sou��st� odkazu. <br>
    <textarea name="resources" cols="80" rows="5" tabindex="7">${PARAMS.resources?if_exists}</textarea>
    <div class="error">${ERRORS.resources?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Volby</td>
   <td>
    <input type="checkbox" name="forbid_discussions" <#if PARAMS.forbid_discussions?exists>checked</#if> value="yes">
    Zak�zat diskuse
    <input type="checkbox" name="forbid_rating" <#if PARAMS.forbid_rating?exists>checked</#if> value="yes">
    Zak�zat hodnocen�
    <input type="checkbox" name="notOnIndex" <#if PARAMS.notOnIndex?exists>checked</#if> value="yes">
    Nezobrazovat na hlavn� str�nce
   </td>
  </tr>
  <tr>
   <td width="90">Ikonka</td>
   <td>
    Pokud chcete, aby se ve v�pise �l�nk� zobrazovala ikonka, vlo�te zde jej� HTML k�d.
    Ned�vejte zde form�tov�n�, to se �e�� v �ablon�. Jen definici tagu IMG. <br>
    <textarea name="thumbnail" cols="80" rows="5" tabindex="7">${PARAMS.thumbnail?if_exists}</textarea>
    <div class="error">${ERRORS.thumbnail?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td><input type="submit" value="Pokra�uj" tabindex="8"></td>
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
