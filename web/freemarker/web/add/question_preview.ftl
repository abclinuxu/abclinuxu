<#include "../header.ftl">

<@lib.showMessages/>

<h1>Upozorn�n�</h1>

<p>Nyn� si prohl�dn�te vzhled va�eho dotazu. Zkontrolujte
si pravopis, obsah i t�n va�eho textu. Uv�domte si, �e
toto nen� placen� technick� podpora, ale dobrovoln�
a neplacen� pr�ce ochotn�ch lid�. Pokud se v�m text n�jak nel�b�,
opravte jej a zvolte N�hled. Pokud jste s n�m spokojeni,
zvolte OK.</p>

<#if PREVIEW?exists>
 <h1>N�hled va�eho dotazu</h1>
 <@lib.showComment PREVIEW, 0, 0, false />
</#if>

<h1>Zde m��ete prov�st sv� �pravy</h1>

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
    <td class="required">nebo va�e jm�no</td>
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
    <div>Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
    pr�zd� ��dky budou nahrazeny nov�m odstavcem.</div>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj n�hled">
    <input type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez4">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>


<#include "../footer.ftl">
