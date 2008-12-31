<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>
<#include "../header.ftl">

<#if PARAMS.action=="add" || PARAMS.action="add2" >
<h1>Přidání článku</h1>
<#else>
<h1>Úprava článku</h1>
</#if>

<@lib.showMessages/>

<#macro selected id><#t>
    <#list PARAMS.authors! as author><#if id?string==author> selected</#if></#list><#t>
</#macro>

<form action="${URL.make("/edit")}" method="POST" name="theForm">

 <table class="siroka" border="0" cellpadding="5">
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title!?html}" size="60" tabindex="1">
    <div class="error">${ERRORS.title!}</div>
   </td>
  </tr>
  <#if AUTHORS??>
  <tr>
   <td class="required">Autor</td>
   <td>
    <select name="authors" size="6" multiple tabindex="2">
        <#list AUTHORS as relation>
            <#assign author=relation.child>
            <option value="${relation.id}"<@selected relation.id/>>
                ${TOOL.childName(author)}
            </option>
        </#list>
    </select>
    <div class="error">${ERRORS.authors!}</div>
   </td>
  </tr>
  </#if>
  <tr>
   <td class="required">Zveřejni dne</td>
   <td>
    <input type="text" name="published" id="datetime_input" value="${PARAMS.published!}" size="40" tabindex="3">
    <input type="button" id="datetime_btn" value="..."><script type="text/javascript">cal_setupDateTime()</script>
    <div class="error">${ERRORS.published!}</div>
   </td>
  </tr>
  <#if SECTIONS??>
      <tr>
       <td class="required">Rubrika</td>
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
   <td class="required">Perex</td>
   <td>
    <textarea name="perex" class="siroka" rows="4" tabindex="4">${PARAMS.perex!?html}</textarea>
    <div class="error">${ERRORS.perex!}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Obsah článku</td>
   <td>
    <p>Rozdělit článek na více podstránek můžete pomocí následující direktivy: <br>
    <i>&lt;page title="Nastavení programu LILO"&gt;</i> <br>
    Pokud použijete tuto funkci, pojmenujte i první stránku, text před první značkou bude ignorován!</p>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.theForm.content, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.theForm.content, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.theForm.content, '&lt;h2&gt;', '&lt;/h2&gt;');" id="serif" title="Vložit značku H2">H2</a>
        <a href="javascript:insertAtCursor(document.theForm.content, '&lt;h3&gt;', '&lt;/h3&gt;');" id="serif" title="Vložit značku H3">H3</a>
        <a href="javascript:insertAtCursor(document.theForm.content, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.theForm.content, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.theForm.content, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
	    <a href="javascript:insertAtCursor(document.theForm.content, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
	    <a href="javascript:insertAtCursor(document.theForm.content, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
	    <a href="javascript:insertAtCursor(document.theForm.content, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
    </div>

    <textarea name="content" class="siroka" rows="40" tabindex="5">${PARAMS.content!?html}</textarea>
    <div class="error">${ERRORS.content!}</div>
   </td>
  </tr>
  <tr>
   <td>Související články</td>
   <td>
    Zde můžete zadat související články z našeho portálu. Na první řádek vložte
    relativní URL odkazu, na druhý jeho popis. Liché řádky jsou URL, sudé popisy. Popis může obsahovat
    znak |, zbytek textu řádky bude sloužit jako komentář, nebude součástí odkazu. <br>
    <textarea name="related" class="siroka" rows="5" tabindex="6">${PARAMS.related!}</textarea>
    <div class="error">${ERRORS.related!}</div>
   </td>
  </tr>
  <tr>
   <td>Zdroje a odkazy</td>
   <td>
    Zde můžete zadat odkazy a zdroje. Místní URL vkládejte jako relativní! Na první řádek vložte
    URL odkazu, na druhý jeho popis. Liché řádky jsou URL, sudé popisy. Popis může obsahovat
    znak |, zbytek textu řádky bude sloužit jako komentář, nebude součástí odkazu. <br>
    <textarea name="resources" class="siroka" rows="5" tabindex="7">${PARAMS.resources!}</textarea>
    <div class="error">${ERRORS.resources!}</div>
   </td>
  </tr>
  <#if AUTHORS??>
  <tr>
   <td>Volby</td>
   <td>
    <label>
        <input type="checkbox" name="forbid_discussions" <#if PARAMS.forbid_discussions??>checked</#if> value="yes">
        Zakázat diskuse
    </label>
    <label>
        <input type="checkbox" name="forbid_rating" <#if PARAMS.forbid_rating??>checked</#if> value="yes">
        Zakázat hodnocení
    </label>
    <label>
        <input type="checkbox" name="notOnIndex" <#if PARAMS.notOnIndex??>checked</#if> value="yes">
        Nezobrazovat na hlavní stránce
    </label>
   </td>
  </tr>
  </#if>
  <tr>
   <td>URL</td>
   <td>
      /clanky/nejaka-sekce/<input type="text" name="url" value="${PARAMS.url!}"> (nepovinné; je-li ponecháno prázdné, systém vygeneruje URL podle názvu článku)
      <div class="error">${ERRORS.url!}</div>
   </td>
  </tr>
  <tr>
   <td>Ikonka</td>
   <td>
    Pokud chcete, aby se ve výpise článků zobrazovala ikonka, vložte zde její HTML kód.
    Nedávejte zde formátování, to se řeší v šabloně. Jen definici tagu IMG. <br>
    <textarea name="thumbnail" class="siroka" rows="2" tabindex="7">${PARAMS.thumbnail!}</textarea>
    <div class="error">${ERRORS.thumbnail!}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td><input type="submit" value="Dokonči" tabindex="8"></td>
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
