<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>Vkl�d�n� contentu</h1>

<p>Tento formul�� slou�� pro vkl�d�n� obsahu. Obvykle jde jen
o oby�ejn� text, kter� m� pevn�, hezk� URL. Nap��klad n�pov�da,
podm�nky u�it� �i reklama. Obsah ale m��e b�t i dynamick�,
pak v�ak pot�ebuje podporu program�tora, kter� napln� data.</p>

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Titulek str�nky</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size=60 tabindex=1>
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Adresa str�nky</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?if_exists}" size=60 tabindex=2>
    <p>Zadejte absolutn�, ale lok�ln� URL. V�t�ina obsahu by m�la
    b�t dostupn� pod adres��em /doc/*</p>
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
    <p>Pokud za�krtnete tuto volbu, syst�m obsah �l�nku zpracuje skrze
    <a href="http://freemarker.sourceforge.net/">Freemarker</a>. U�ite�n� pro dynamick� obsah.</p>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Obsah str�nky</td>
   <td>
    <p>V�echna URL na �l�nky, obr�zky a soubory z na�eho serveru mus� b�t relativn�!</p>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h1&gt;', '&lt;/h1&gt;');" id="mono" title="Vlo�it zna�ku nadpisu">&lt;h1&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="mono" title="Vlo�it zna�ku nadpisu">&lt;h2&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="mono" title="Vlo�it zna�ku nadpisu">&lt;h3&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vlo�it form�tovan� text. Vhodn� pouze pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
    </div>
    <textarea name="content" cols="100" rows="30" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
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
 <input type="hidden" name="rid" value="${PARAMS.rid?if_exists}">
</form>


<#include "../footer.ftl">
