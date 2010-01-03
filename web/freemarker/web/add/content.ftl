<@lib.addRTE textAreaId="content" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vkládání dokumentu</h1>

<p>Tento formulář slouží pro vkládání obsahu. Obvykle jde jen
o obyčejný text, který má pevné, hezké URL. Například nápověda,
podmínky užití či reklama. Obsah ale může být i dynamický,
pak však potřebuje podporu programátora, který připraví data.</p>

<#if PREVIEW??>
    <fieldset>
        <legend>Náhled</legend>
        <#if PARAMS.execute?default("no")!="yes">
            ${TOOL.xpath(PREVIEW,"/data/content")}
        <#else>
            <@TOOL.xpath(PREVIEW,"/data/content")?interpret />
        </#if>
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
   <td width="90" class="required">Adresa stránky</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url!}" size=60 tabindex=2>
    <p>Zadejte absolutní, ale lokální URL.</p>
    <div class="error">${ERRORS.url!}</div>
   </td>
  </tr>
  <#if USER.hasRole("root")>
  <tr>
   <td width="90" class="required">Java FQCN</td>
   <td>
    <input type="text" name="java_class" value="${PARAMS.java_class!}" size=60 tabindex=3>
    <div class="error">${ERRORS.java_class!}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Zpracovat freemarkerem</td>
   <td>
    <input type="checkbox" name="execute" <#if PARAMS.execute?default("no")=="yes">checked</#if> value="yes">
    <p>Pokud zaškrtnete tuto volbu, systém obsah článku zpracuje skrze
    <a href="http://freemarker.sourceforge.net/">Freemarker</a>. Užitečné pro dynamický obsah.</p>
   </td>
  </tr>
  </#if>
  <tr>
   <td width="90" class="required">Obsah stránky</td>
   <td>
        <p>Všechna URL na články, obrázky a soubory z našeho serveru musí být relativní!</p>
        <@lib.showError key="content"/>
        <@lib.showRTEControls "content"/>
        <textarea name="content" id="content" class="siroka" rows="30" tabindex="5">${PARAMS.content!?html}</textarea>
   </td>
  </tr>
    <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
        <tr>
            <td>
                Popis změny
                <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
            </td>
            <td>
                <input tabindex="6" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr!?html}">
                <div class="error">${ERRORS.rev_descr!}</div>
            </td>
        </tr>
    </#if>
  <tr>
   <td width="90">&nbsp;</td>
   <td>
     <input tabindex="8" type="submit" name="preview" value="<#if PREVIEW??>Zopakuj náhled<#else>Náhled</#if>">
     <input tabindex="9" type="submit" name="finish" value="Dokonči">
   </td>
  </tr>
 </table>

 <#if PARAMS.action=="add" || PARAMS.action="add2" >
  <input type="hidden" name="action" value="add2">
  <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
 <input type="hidden" name="rid" value="${PARAMS.rid!}">
 <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
  <#if PARAMS.startTime??><#assign value=PARAMS.startTime><#else><#assign value=START_TIME?c></#if>
  <input type="hidden" name="startTime" value="${value}">
 </#if>
</form>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
