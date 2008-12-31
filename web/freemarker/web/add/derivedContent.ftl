<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="content" formId="form" inputMode="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/editContent")}" method="POST" name="form">

<h1>Vkládání dokumentu</h1>

<p>Tento formulář slouží pro vložení nového dokumentu pod existující
dokument. Typické použití bude, když chcete rozčlenit text na více
stránek nebo do kapitol. Věnujte pečlivou pozornost volbě titulku,
neboť URL se bude skládat z URL nadřazeného dokumentu a titulku
zbaveného diakritiky a speciálních znaků. Toto URL už běžnými prostředky
nebudete moci změnit.
</p>

<#if PREVIEW??>
    <fieldset>
        <legend>Náhled</legend>
        ${TOOL.xpath(PREVIEW,"/data/content")}
    </fieldset>
</#if>

 <table class="siroka" border=0 cellpadding=5>
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
        <@lib.showError key="content"/>
        <@rte.showFallback "content"/>
        <textarea name="content" class="siroka" rows="30" tabindex="5">${PARAMS.content!?html}</textarea>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td>
     <input tabindex="8" type="submit" name="preview" value="<#if PREVIEW??>Zopakuj náhled<#else>Náhled</#if>">
     <input tabindex="9" type="submit" name="finish" value="Dokonči">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addDerivedPage2">
 <input type="hidden" name="rid" value="${PARAMS.rid!}">
</form>

<#include "/include/napoveda-k-html-formatovani.txt">

<#include "../footer.ftl">
