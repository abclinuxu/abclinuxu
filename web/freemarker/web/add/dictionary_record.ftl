<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Úvod</h1>

<p>Cílem této slu¾by je vytvoøit rozsáhlý výkladový slovník
nejrùznìj¹ích pojmù týkajících se Linuxu èi Unixu.  Èím více
pojmù bude kvalitnì pokrývat, tím snáze se nováèci zorientují
v Linuxu a zvý¹í se ¹ance, ¾e nebudou klást otázky vyplývající
z nepochopení základních principù tohoto operaèního systému.
</p>

<h1 class="st_nadpis">Nový pojem</h1>

<p>Ka¾dý pojem ve slovníku se skládá z názvu a popisu. Název
odpovídá pojmu v prvním pádì jednotného èísla. Napøíklad
souborový systém, kernel, speciální zaøízení èi symbolický odkaz.
Popis pak obsahuje vysvìtlení tohoto pojmu. Pokud v popisu nepou¾ijete
formatovací znaky nový øádek a odstavec, popis bude ulo¾en v takzvaném
zjednodu¹eném formátu, kdy prázdný øádek bude nahrazen znaèkou
pro nový odstavec.
</p>

<#if PARAMS.preview?exists && PARAMS.desc?exists>
 <h1 class="st_nadpis">Náhled</h1>
  <p class="slovnik">
   ${TOOL.render(PARAMS.desc,USER?if_exists)}
  </p>
 <br><br>
</#if>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="0" border="0" width="100%">
  <tr>
   <td colspan="2" class="required">Popis</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea name="desc" cols="70" rows="20" tabindex="1">${PARAMS.desc?if_exists?html}</textarea>
    <div class="error">${ERRORS.desc?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <input type="submit" name="preview" value="Náhled">
    <#if PARAMS.preview?exists><input type="submit" name="submit" value="Dokonèi"></#if>
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addRecord2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "../footer.ftl">
