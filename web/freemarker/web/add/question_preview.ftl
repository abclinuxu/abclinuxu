<#include "../header.ftl">

<@lib.showMessages/>

<h1>Upozornìní</h1>

<p>Nyní si prohlédnìte vzhled va¹eho dotazu. Zkontrolujte
si pravopis, obsah i tón va¹eho textu. Uvìdomte si, ¾e
toto není placená technická podpora, ale dobrovolná
a neplacená práce ochotných lidí. Pokud se vám text nìjak nelíbí,
opravte jej a zvolte Náhled. Pokud jste s ním spokojeni,
zvolte OK.</p>

<#if PREVIEW?exists>
 <h1>Náhled va¹eho dotazu</h1>
 <@lib.showComment PREVIEW, 0, 0, false />
</#if>

<h1>Zde mù¾ete provést své úpravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table cellpadding="5">
  <#if ! USER?exists>
   <tr>
    <td class="required">Login a heslo</td>
    <td>
     <input type="text" name="LOGIN" size="8">
     <input type="password" name="PASSWORD" size="8">
    </td>
   </tr>
   <tr>
    <td class="required">nebo va¹e jméno</td>
    <td>
     <input type="text" size="30" name="author" value="${PARAMS.author?if_exists}">
     <div class="error">${ERRORS.author?if_exists}</div>
    </td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists}">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Dotaz</td>
   <td>
    <textarea name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div>Smíte pou¾ívat základní HTML znaèky. Pokud je nepou¾ijete,
    prázdé øádky budou nahrazeny novým odstavcem.</div>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj náhled">
    <input type="submit" name="finish" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez4">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
