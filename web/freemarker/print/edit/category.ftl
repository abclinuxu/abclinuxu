<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/EditCategory")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="120" class="required">Jm�no sekce</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
    <tr>
        <td width="120" class="required">Typ</td>
        <td>
            <select name="type" tabindex="2">
                <option value="faq"<#if PARAMS.type?default("software")=="software"> SELECTED</#if>>Sekce Software</option>
                <option value="hw_closed"<#if PARAMS.type?if_exists=="hw_closed"> SELECTED</#if>>Otev�en� hardwarov� sekce</option>
                <option value="hw_open"<#if PARAMS.type?if_exists=="open"> SELECTED</#if>>Uzav�en� hardwarov� sekce</option>
                <option value="faq"<#if PARAMS.type?if_exists=="faq"> SELECTED</#if>>Sekce FAQ</option>
                <option value="forum"<#if PARAMS.type?if_exists=="forum"> SELECTED</#if>>Diskusn� f�rum</option>
                <option value="section"<#if PARAMS.type?if_exists=="section"> SELECTED</#if>>Rubrika pro �l�nky</option>
                <option value="blog"<#if PARAMS.type?if_exists=="blog"> SELECTED</#if>>Blog</option>
            </select>
        </td>
    </tr>
  <tr>
   <td width="120">Pozn�mka</td>
   <td>
    <textarea name="note" cols="80" rows="15" tabindex="3">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Ikona</td>
   <td>
    <input type="text" name="icon" value="${PARAMS.icon?if_exists}" size="40" tabindex="4">
    <input type="submit" name="iconChooser" value="Vyb�r ikon">
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokra�uj" TABINDEX="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="categoryId" value="${PARAMS.categoryId}">
 <input type="hidden" name="url" value="${URL.make("/EditCategory")}">
</form>


<#include "../footer.ftl">
