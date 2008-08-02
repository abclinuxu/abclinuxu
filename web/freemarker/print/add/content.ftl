<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>Vkládání dokumentu</h1>

<p>Tento formulář slouží pro vkládání obsahu. Obvykle jde jen
o obyčejný text, který má pevné, hezké URL. Například nápověda,
podmínky užití či reklama. Obsah ale může být i dynamický,
pak však potřebuje podporu programátora, který připraví data.</p>

<#if PREVIEW?exists>
    <fieldset>
        <legend>Náhled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

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
    <p>Zadejte absolutní, ale lokální URL.</p>
    <div class="error">${ERRORS.url?if_exists}</div>
   </td>
  </tr>
  <#if USER.hasRole("root")>
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
    <p>Pokud zaškrtnete tuto volbu, systém obsah článku zpracuje skrze
    <a href="http://freemarker.sourceforge.net/">Freemarker</a>. Užitečné pro dynamický obsah.</p>
   </td>
  </tr>
  </#if>
  <tr>
   <td width="90" class="required">Obsah stránky</td>
   <td>
    <p>Všechna URL na články, obrázky a soubory z našeho serveru musí být relativní!</p>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h1&gt;', '&lt;/h1&gt;');" id="mono" title="Vložit značku nadpisu">&lt;h1&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="mono" title="Vložit značku nadpisu">&lt;h2&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="mono" title="Vložit značku nadpisu">&lt;h3&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit formátovaný text. Vhodné pouze pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
    </div>
    <textarea name="content" cols="100" rows="30" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
    <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
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
    </#if>
  <tr>
   <td width="90">&nbsp;</td>
   <td>
     <input tabindex="8" type="submit" name="preview" value="<#if PREVIEW?exists>Zopakuj náhled<#else>Náhled</#if>">
     <input tabindex="9" type="submit" name="finish" value="Dokonči">
   </td>
  </tr>
 </table>

 <#if PARAMS.action=="add" || PARAMS.action="add2" >
  <input type="hidden" name="action" value="add2">
  <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
 <input type="hidden" name="rid" value="${PARAMS.rid?if_exists}">
 <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
  <#if PARAMS.startTime?exists><#assign value=PARAMS.startTime><#else><#assign value=START_TIME?c></#if>
  <input type="hidden" name="startTime" value="${value}">
 </#if>
</form>

<p>Povolené HTML <a href="http://www.w3.org/TR/html4/index/elements.html">značky</a>:
 A,  B, BLOCKQUOTE, BR, CENTER, CITE, CODE, DD, DEL, DIV, DL, DT, EM, IMG, H1, H2, H3, H4, HR, I,
 INS, KBD, LI, OL, P, PRE, Q, SMALL, SPAN, STRONG, SUB, SUP, TABLE, TBODY, TD, TFOOT, TH, THEAD,
 TR, TT, U, UL, VAR. Značky P, PRE, DIV, SPAN, H1-H4 a A povolují atrubity ID a CLASS.
</p>


<#include "../footer.ftl">
