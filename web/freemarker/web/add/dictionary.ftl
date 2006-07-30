<#include "../header.ftl">

<@lib.showMessages/>

<h1>Slovník</h1>

<p>Cílem této slu¾by je vytvoøit rozsáhlý výkladový slovník
nejrùznìj¹ích pojmù týkajících se Linuxu èi Unixu.  Èím více
pojmù bude kvalitnì pokrývat, tím snáze se nováèci zorientují
v Linuxu a zvý¹í se ¹ance, ¾e nebudou klást otázky vyplývající
z nepochopení základních principù tohoto operaèního systému.
</p>

<h2>Nový pojem</h2>

<p>Ka¾dý pojem ve slovníku se skládá z názvu a popisu. Název
odpovídá pojmu v prvním pádì jednotného èísla. Napøíklad
souborový systém, kernel, speciální zaøízení èi symbolický odkaz.
Popis pak obsahuje vysvìtlení tohoto pojmu. Pokud v popisu nepou¾ijete
formatovací znaky nový øádek a odstavec, popis bude ulo¾en v takzvaném
zjednodu¹eném formátu, kdy prázdný øádek bude nahrazen znaèkou
pro nový odstavec.
</p>

<#if PARAMS.preview?exists>
 <h2>Náhled</h2>
 <h3>${PARAMS.name?if_exists}</h3>
 <#if PARAMS.desc?exists>
  <p class="slovnik">
   ${TOOL.render(PARAMS.desc,USER?if_exists)}
  </p>
 </#if>
 <br><br>
</#if>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="0" border="0" width="100%">
  <tr>
   <td class="required">Pojem</td>
   <td>
    <input tabindex="1" type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2" class="required">Popis</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea tabindex="2" name="desc" cols="70" rows="20" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
    <div class="error">${ERRORS.desc?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <input tabindex="3" type="submit" name="preview" value="Náhled">
    <#if PARAMS.preview?exists><input tabindex="4" type="submit" name="submit" value="Dokonèi"></#if>
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add2">
</form>

<#include "../footer.ftl">
