<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<form action="${URL.make("/edit")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Titulek</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size=60 tabindex=1>
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Autor</td>
   <td>
    <input type="text" name="uid" value="${PARAMS.uid?if_exists}" size=4 tabindex=2>
    <div class="error">${ERRORS.uid?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Zve�ejni dne</td>
   <td>
    <input type="text" name="published" value="${PARAMS.published?if_exists}" size=40 tabindex=3>
    <div class="error">${ERRORS.published?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Zak�zat diskuse</td>
   <td>
    <input type="checkbox" name="forbid_discussions" <#if PARAMS.forbid_discussions?exists>checked</#if> value="yes">
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Perex</td>
   <td>
    <textarea name="perex" cols="80" rows="4" tabindex="4">${PARAMS.perex?if_exists?html}</textarea>
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

    <textarea name="content" cols="80" rows="30" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Souvisej�c� �l�nky</td>
   <td>
    Zde m��ete zadat souvisej�c� �l�nky z na�eho port�lu. Na prvn� ��dek vlo�te
    relativn� URL odkazu, na druh� jeho popis. Lich� ��dky jsou URL, sud� popisy. <br>
    <textarea name="related" cols="80" rows="5" tabindex="6">${PARAMS.related?if_exists}</textarea>
    <div class="error">${ERRORS.related?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Zdroje a odkazy</td>
   <td>
    Zde m��ete zadat odkazy a zdroje. M�stn� URL vkl�dejte jako relativn�! Na prvn� ��dek vlo�te
    URL odkazu, na druh� jeho popis. Lich� ��dky jsou URL, sud� popisy. <br>
    <textarea name="resources" cols="80" rows="5" tabindex="7">${PARAMS.resources?if_exists}</textarea>
    <div class="error">${ERRORS.resources?if_exists}</div>
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
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
