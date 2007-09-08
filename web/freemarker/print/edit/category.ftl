<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/EditCategory")}" method="POST">

 <table width="100" border="0" cellpadding="5">
  <tr>
   <td width="120" class="required">Jméno sekce</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
    <tr>
        <td width="120" class="required">Typ</td>
        <td>
            <select name="type" tabindex="2">
                <option value="software"<#if PARAMS.type?if_exists=="software"> SELECTED</#if>>Sekce software</option>
                <option value="hardware"<#if PARAMS.type?if_exists=="hardware"> SELECTED</#if>>Sekce hardware</option>
                <option value="forum"<#if PARAMS.type?if_exists=="forum"> SELECTED</#if>>Diskusní fórum</option>
                <option value="faq"<#if PARAMS.type?if_exists=="faq"> SELECTED</#if>>Sekce FAQ</option>
                <option value="section"<#if PARAMS.type?if_exists=="section"> SELECTED</#if>>Rubrika</option>
                <option value="blog"<#if PARAMS.type?if_exists=="blog"> SELECTED</#if>>Blog</option>
                <option value="generic"<#if ! PARAMS.type?exists> SELECTED</#if>>Sekce</option>
            </select>
        </td>
    </tr>
    <tr>
        <td width="120">Otevřená</td>
        <td>
            <input type="checkbox" name="open" value="true"<#if PARAMS.open?default("")=="true"> checked</#if> tabindex="3">
        </td>
    </tr>
  <tr>
   <td width="120">Poznámka</td>
   <td>
    <textarea name="note" cols="80" rows="15" tabindex="4">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Dokonči" TABINDEX="6"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="url" value="${URL.make("/EditCategory")}">
</form>


<#include "../footer.ftl">
