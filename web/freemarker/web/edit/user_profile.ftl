<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stránce si můžete upravit svůj profil.
Profil slouží jako vaše veřejná domovská stránka,
na které máte možnost zveřejnit informace o své
osobě. O tom, kdo jste, odkud jste, co máte rád,
jaké je vaše krédo. Fantazii se meze nekladou.
</p>

<p>
Pro vaši ochranu nejdříve zadejte současné heslo.
Pokud máte na internetu svou domovskou stránku,
vyplňte její URL. Další položkou je rok, kdy jste
začal používat Linux. Následuje možnost uložit
až pět distribucí, které v současnosti používáte.
Posledním políčkem je text <i>O&nbsp;mně</i>. Do něj
můžete napsat informace o sobě, které chcete sdělit
čtenářům. Může to být jen pár slov, ale i delší
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
   <td width="120">Linux používám<br>od roku</td>
   <td>
    <input type="text" name="linuxFrom" value="${PARAMS.linuxFrom?if_exists}" size="40" tabindex="2">
   </td>
  </tr>
  <tr>
   <td width="120" valign="middle">Používám tyto distribuce</td>
   <td>
    <#assign distros=TOOL.asList(PARAMS.distribution)>
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
   <td width="60">Patička</td>
   <td>
    <textarea name="signature" rows="4" cols="54" tabindex="8"
    onkeyup="writeRemainingCharsCount(this);">${PARAMS.signature?if_exists?html}</textarea>
    <div id="signatureTextCounter">&nbsp;</div>
    <div class="error">${ERRORS.signature?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2">O mně</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea name="about" rows="25" cols="70" tabindex="9">${PARAMS.about?if_exists?html}</textarea>
    <div class="error">${ERRORS.about?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokonči" tabindex="10"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editProfile2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
