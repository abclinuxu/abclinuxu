<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>Vkládání contentu</h1>

<p>Tento formuláø slou¾í pro vkládání obsahu. Obvykle jde jen
o obyèejný text, který má pevné, hezké URL. Napøíklad nápovìda,
podmínky u¾ití èi reklama. Obsah ale mù¾e být i dynamický,
pak v¹ak potøebuje podporu programátora, který naplní data.</p>

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Titulek stránky</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size=60 tabindex=1>
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Adresa stránky</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?if_exists}" size=60 tabindex=2>
    <p>Zadejte absolutní, ale lokální URL. Vìt¹ina obsahu by mìla
    být dostupná pod adresáøem /doc/*</p>
    <div class="error">${ERRORS.url?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Java FQCN</td>
   <td>
    <input type="text" name="java_class" value="${PARAMS.java_class?if_exists}" size=60 tabindex=3>
    <div class="error">${ERRORS.java_class?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Zpracovat freemarkerem</td>
   <td>
    <input type="checkbox" name="forbid_discussions" <#if PARAMS.forbid_discussions?exists>checked</#if> value="yes">
    <p>Pokud za¹krtnete tuto volbu, systém obsah èlánku zpracuje skrze
    <a href="http://freemarker.sourceforge.net/">Freemarker</a>. U¾iteèné pro dynamický obsah.</p>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Obsah stránky</td>
   <td>
    <p>V¹echna URL na èlánky, obrázky a soubory z na¹eho serveru musí být relativní!</p>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h1&gt;', '&lt;/h1&gt;');" id="mono" title="Vlo¾it znaèku nadpisu">&lt;h1&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="mono" title="Vlo¾it znaèku nadpisu">&lt;h2&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="mono" title="Vlo¾it znaèku nadpisu">&lt;h3&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo¾it formátovaný text. Vhodné pouze pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
    </div>
    <textarea name="content" cols="100" rows="30" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
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
 <input type="hidden" name="rid" value="${PARAMS.rid?if_exists}">
</form>


<#include "../footer.ftl">
