<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nápovìda</h1>

<p>Vyplòte údaje záznamu. Sna¾te se pøitom zapsat co nejvíce podrobností,
ale dávejte si pozor, aby vá¹ pøíspìvek nebyl pøíli¹
zamìøen na pou¾ití nástrojù va¹í distribuce. Linux je toti¾ jeden, tak¾e
pokud vyplníte podstatné údaje (napøíklad název jaderného modulu),
vá¹ pøíspìvek pomù¾e i u¾ivatelùm ostatních distribucí.</p>

<h1>Formátování</h1>

<p>Smíte pou¾ívat základní HTML znaèky. Pokud je nepou¾ijete,
prázdné øádky budou nahrazeny novým odstavcem.
</p>

<form action="${URL.make("/edit")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td class="required">Ovladaè je dodáván </td>
   <td>
    <select name="driver" tabindex=8>
     <#assign driver=PARAMS.driver?if_exists>
     <option value="kernel"<#if driver=="kernel"> SELECTED</#if>>v jádøe</option>
     <option value="xfree"<#if driver=="xfree"> SELECTED</#if>>v XFree86</option>
     <option value="maker"<#if driver=="maker"> SELECTED</#if>>výrobcem</option>
     <option value="other"<#if driver=="other"> SELECTED</#if>>nìkým jiným</option>
     <option value="none"<#if driver=="none"> SELECTED</#if>>neexistuje</option>
     <option value="unknown"<#if driver=="unknown"> SELECTED</#if>>netu¹ím</option>
    </select>
   </td>
  </tr>

  <tr>
   <td class="required">Cena výrobku je </td>
   <td>
    <select name="price" tabindex=8>
     <#assign driver=PARAMS.price?if_exists>
     <option value="verylow"<#if driver=="verylow"> SELECTED</#if>>velmi nízká</option>
     <option value="low"<#if driver=="low"> SELECTED</#if>>nízká</option>
     <option value="good"<#if driver=="good"> SELECTED</#if>>pøimìøená</option>
     <option value="high"<#if driver=="high"> SELECTED</#if>>vysoká</option>
     <option value="toohigh"<#if driver=="toohigh"> SELECTED</#if>>pøemr¹tìná</option>
     <option value="unknown"<#if driver=="unknown"> SELECTED</#if>>nehodnotím</option>
    </select>
   </td>
  </tr>

  <tr>
   <td>Identifikace pod Linuxem</td>
   <td>
    <div>
     Zadejte, jak toto zaøízení identifikuje Linux. K tomu vám pomohou pøíkazy
     <code>lspci</code>, <code>lsusb</code> èi <code>dmesg</code>, tyto informace
     bývají také k dispozici v adresáøi <code>/proc</code>. Nicménì buïte struèní
     a vkládejte jen skuteènì zajímavé údaje týkající se tohoto zaøízení, napøíklad
     <code>lsusb</code> vám vrátí desítky kilobajtù textù u¾iteèných jen vývojáøùm
     jádra, z nich¾ pro na¹e úèely se hodí jen dvì tøi øádky.
    </div>
    <textarea name="identification" cols="50" rows="4">${PARAMS.identification?if_exists?html}</textarea>
    <div class="error">${ERRORS.identification?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td>Technické parametry</td>
   <td>
    <textarea name="params" cols="50" rows="4">${PARAMS.params?if_exists?html}</textarea>
    <div class="error">${ERRORS.params?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td class="required">Postup zprovoznìní</td>
   <td>
    <textarea name="setup" cols="50" rows="10">${PARAMS.setup?if_exists?html}</textarea>
    <div class="error">${ERRORS.setup?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td class="required">Poznámka</td>
   <td>
    <textarea name="note" cols="50" rows="10">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="4"></td>
  </tr>

 </table>

 <#assign action=PARAMS.action?if_exists>
 <#if action.startsWith("addItem")>
  <input type="hidden" name="action" value="addItem3">
  <input type="hidden" name="name" value="${PARAMS.name?if_exists}">
  <input type="hidden" name="icon" value="${PARAMS.icon?if_exists}">
 <#else>
  <input type="hidden" name="action" value="${action}">
 </#if>

 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <#if PARAMS.recordId?exists>
  <input type="hidden" name="recordId" value="${PARAMS.recordId}">
 </#if>
</form>


<#include "../footer.ftl">
