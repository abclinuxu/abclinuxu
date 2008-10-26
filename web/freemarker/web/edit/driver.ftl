<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="note" formId="form" inputMode="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava ovladače</h1>

<p>
    Pokud vyšla nová verze ovladače nebo jste našli nějakou chybku
    v popisu ovladače, tento formulář slouží pro  zadání úprav.
    V případě nové verze se nebojte smazat celý text a opravdu popsat
    jen novinky oproti minulé verzi, v historii změn si čtenář může
    přečíst popisy předchozích verzí. Pokud má ovladač více vývojových
    řad, do políčka verze dávejte jen číslo aktuální řady.
</p>

<#if PARAMS.preview?exists>
 <h2>Náhled příspěvku</h2>

 <table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jméno</td><td>${PARAMS.name?if_exists}</td>
  </tr>
  <tr>
    <td>Kategorie ovladače</td><td><#if CATEGORY?exists>${CATEGORY.name}</#if></td>
  <tr>
  <tr>
    <td>Verze</td><td>${PARAMS.version?if_exists}</td>
  </tr>
  <tr>
    <td>Adresa</td>
    <td>
      <a href="${PARAMS.url?if_exists?html}">${TOOL.limit(PARAMS.url?if_exists,50," ..")?html}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(PARAMS.note?if_exists,USER?if_exists)}</td>
  </tr>
 </table>
</#if>

<h2>Zde zadejte své úpravy</h2>

<form action="${URL.make("/edit")}" method="POST" name="form">
 <table cellpadding="0" border="0" style="margin-top: 1em;">
  <tr>
   <td class="required">Jméno</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="30" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Kategorie</td>
   <td>
    <select name="category" tabindex="2">
     <#assign selected = PARAMS.category?default("NONE")>
     ${PARAMS.category?if_exists}
     <#list CATEGORIES as category>
      <option value="${category.key}"
       <#if category.key=selected>selected</#if> >
       ${category.name}
      </option>
     </#list>
    </select>
   </td>
  </tr>
  <tr>
   <td class="required">Verze</td>
   <td>
    <input type="text" name="version" value="${PARAMS.version?if_exists}" size="30" tabindex="2">
    <div class="error">${ERRORS.version?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">URL</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?default("http://")?html}" size="70" tabindex="4">
    <div class="error">${ERRORS.url?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2" class="required">Poznámka</td>
  </tr>
  <tr>
   <td colspan="2">
    <@rte.showFallback "note"/>
    <textarea name="note" cols="70" rows="15" tabindex="5">${PARAMS.note?if_exists?html}</textarea>
    <@lib.showError key="note"/>
   </td>
  </tr>
  <tr>
    <td>
        Popis změny
        <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
    </td>
   <td>
    <input tabindex="6" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists?html}">
    <div class="error">${ERRORS.rev_descr?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2" align="center">
    <input type="submit" name="preview" value="Náhled">
    <input type="submit" name="submit" value="Dokonči">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
