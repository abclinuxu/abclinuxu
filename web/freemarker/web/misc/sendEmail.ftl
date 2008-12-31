<#include "../header.ftl">

<@lib.showMessages/>

<p>Chráníme naše uživatele před roboty spammerů, proto zásadně
nezveřejňujeme jejich emailové adresy. Pokud chcete některého
našeho uživatele kontaktovat emailem, tento formulář vám to umožní.
Zároveň můžete zadat adresu, kam se má poslat (slepá) kopie emailu.
</p>

<#if !USER?? || !USER.hasRole("root")>
    <p>Jako malá prevence před zneužíváním slouží kontrolní
    kód. Ve vašem případě má hodnotu ${KOD}. Vyplňte jej prosím
    níže do formuláře. Do políčka pro email zadávajte maximálně jednu
    adresu.</p>
</#if>

<form action="${URL.noPrefix("/Mail")}" method="POST">

 <table class="siroka" border="0" cellpadding="5">
  <#if !USER?? || !USER.hasRole("root")>
      <tr>
       <td class="required">Kontrolní kód</td>
       <td><input type="text" name="KOD" value="${PARAMS.KOD!}" size="4">
       <#if ERRORS.KOD??><span class="error">${ERRORS.KOD}</span></#if>
       </td>
      </tr>
  </#if>
  <tr>
   <td class="required">Váš email</td>
   <td><input type="text" name="sender" size="30" value="${PARAMS.sender!}">
   <#if ERRORS.sender??><span class="error">${ERRORS.sender}</span></#if>
   </td>
  </tr>
  <tr>
   <td>Kopie</td>
   <td><input type="text" name="cc" size="30" value="${PARAMS.cc!}">
   <#if ERRORS.cc??><span class="error">${ERRORS.cc}</span></#if>
   </td>
  </tr>
  <tr>
   <td>Slepá kopie</td>
   <td><input type="text" name="bcc" size="30" value="${PARAMS.bcc!}">
   <#if ERRORS.bcc??><span class="error">${ERRORS.bcc}</span></#if>
   </td>
  </tr>
  <tr>
   <td class="required">Předmět zprávy</td>
   <td><input type="text" name="subject" size="30" value="${PARAMS.subject!}">
   <#if ERRORS.subject??><span class="error">${ERRORS.subject}</span></#if>
   </td>
  </tr>
  <tr>
   <td colspan="2">Text zprávy<br>
   <textarea name="message" class="siroka" rows="20">${PARAMS.message!}</textarea>
   <#if ERRORS.message??><br><span class="error">${ERRORS.message}</span></#if>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <input type="submit" name="finish" value="Odeslat">
    <#if PARAMS.url??><input type="hidden" name="url" value="${PARAMS.url}"></#if>
   </td>
  </tr>
 </table>

 <input type="hidden" name="action" value="finish">
</form>

<#include "../footer.ftl">
