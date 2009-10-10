<@lib.addRTE textAreaId="content" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>Úprava dokumentu</h1>

<p>Pokud chcete vylepšit obsah dokumentu nebo opravit chybu, jste na
správné adrese. Všechny změny se automaticky ukládají do databáze, takže
je možné prohlížet obsah tohoto dokumentu v průběhu času nebo vrátit
změny zpět.</p>

<#if PREVIEW??>
    <fieldset>
        <legend>Náhled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

 <table class="siroka" border="0" cellpadding="5">
  <tr>
   <td width="90" class="required">Titulek stránky</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title!}" size=60 tabindex=1>
    <div class="error">${ERRORS.title!}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Obsah stránky</td>
   <td>
    <p>Všechna URL na články, obrázky a soubory z našeho serveru musí být relativní!</p>
    <@lib.showRTEControls "content"/>
    <textarea name="content" class="siroka" rows="30" tabindex="2">${PARAMS.content!?html}</textarea>
    <@lib.showError key="content"/>
   </td>
  </tr>
  <tr>
    <td>
        Popis změny
        <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
    </td>
   <td>
    <input tabindex="3" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr!?html}">
    <div class="error">${ERRORS.rev_descr!}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td>
     <input tabindex="4" type="submit" name="preview" value="<#if PREVIEW??>Zopakuj náhled<#else>Náhled</#if>">
     <input tabindex="5" type="submit" name="finish" value="Dokonči">
   </td>
  </tr>
 </table>

 <input type="hidden" name="action" value="editPublicContent2">
 <input type="hidden" name="rid" value="${PARAMS.rid!}">
  <#if PARAMS.startTime??><#assign value=PARAMS.startTime><#else><#assign value=START_TIME?c></#if>
  <input type="hidden" name="startTime" value="${value}">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
