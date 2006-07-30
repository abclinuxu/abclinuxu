<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stránce si mù¾ete upravit svùj profil.
Profil slou¾í jako va¹e veøejná domovská stránka,
na které máte mo¾nost zveøejnit informace o své
osobì. O tom, kdo jste, odkud jste, co máte rád,
jaké je va¹e krédo. Fantazii se meze nekladou.
</p>

<p>
Pro va¹i ochranu nejdøíve zadejte souèasné heslo.
Pokud máte na internetu svou domovskou stránku,
vyplòte její URL. Dal¹í polo¾kou je rok, kdy jste
zaèal pou¾ívat Linux. Následuje mo¾nost ulo¾it
a¾ pìt distribucí, které v souèasnosti pou¾íváte.
Posledním políèkem je text <i>O&nbsp;mnì</i>. Do nìj
mù¾ete napsat informace o sobì, které chcete sdìlit
ètenáøùm. Mù¾e to být jen pár slov, ale i del¹í
povídání.</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="120">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="20" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">Domovská stránka</td>
   <td>
    <input type="text" name="www" value="${PARAMS.www?if_exists}" size="40" tabindex="2">
    <div class="error">${ERRORS.www?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">Linux pou¾ívám<br>od roku</td>
   <td>
    <input type="text" name="linuxFrom" value="${PARAMS.linuxFrom?if_exists}" size="40" tabindex="2">
   </td>
  </tr>
  <tr>
   <td width="120" valign="middle">Pou¾ívám tyto distribuce</td>
   <td>
    <#assign distros=PARAMS.distribution?if_exists>
    <#if distros?size gte 1 >
     <input type="text" name="distribution" value="${distros[0]}" size="40" tabindex="3"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="3"><br>
    </#if>
    <#if distros?size gte 2 >
     <input type="text" name="distribution" value="${distros[1]}" size="40" tabindex="4"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="4"><br>
    </#if>
    <#if distros?size gte 3 >
     <input type="text" name="distribution" value="${distros[2]}" size="40" tabindex="5"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="5"><br>
    </#if>
    <#if distros?size gte 4 >
     <input type="text" name="distribution" value="${distros[3]}" size="40" tabindex="6"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="6"><br>
    </#if>
    <#if distros?size gte 5 >
     <input type="text" name="distribution" value="${distros[4]}" size="40" tabindex="7"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="7"><br>
    </#if>
   </td>
  </tr>
  <tr>
   <td width="60">Patièka</td>
   <td>
    <textarea name="signature" rows="4" cols="54" tabindex="8">${PARAMS.signature?if_exists?html}</textarea>
    <div class="error">${ERRORS.signature?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">O mnì</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea name="about" rows="25" cols="70" tabindex="9">${PARAMS.about?if_exists?html}</textarea>
    <div class="error">${ERRORS.about?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="10"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editProfile2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
