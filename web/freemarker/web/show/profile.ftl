<#include "../header.ftl">

<@lib.showMessages/>

<#if ! USER?exists>
 <p>Pokud jste ${PROFILE.name}, <a href="${URL.noPrefix("/Profile?action=login")}">pøihla¹te se</a>
 a bude vám zobrazena va¹e domovská stránka.
 </p>
<#elseif USER.id==PROFILE.id>
 <#if PARAMS.LOGIN?exists>
  <h1 class="st_nadpis">Vítejte</h1>
  <p>Dìkujeme vám za dal¹í náv¹tìvu portálu AbcLinuxu. Doufáme, ¾e vám
  pøinese cenné informace. Pokud budete spokojeni, doporuète nás svým pøátelùm
  a známým.
  </p>
 </#if>

 <h1 class="st_nadpis">Moje domovská stránka</h1>

 <p>Nacházíte se ve své veøejné domovské stránce, která slou¾í pro va¹i prezentaci.
 Pokud ji chcete doplnit èi nìjak upravit, pøejdìte na
 <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">tuto stránku</a>.
 Tam také mù¾ete zmìnit nastavení svého u¾ivatele, jako napøíklad pøihlásit se k odbìru zpravodaje.
 Text nad èarou vidíte pouze vy, ostatním náv¹tìvníkùm není zobrazen.
 </p>
 <hr width="80%" size="1" align="center">
<#elseif USER.hasRole("user admin")>
 <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">Upravit</a>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/photo")?exists>
 <#assign photo=TOOL.xpath(PROFILE,"/data/profile/photo")>
<#else>
 <#assign photo="/images/faces/default_"+TOOL.xpath(PROFILE,"/data/personal/sex")+".gif">
</#if>
<img src="${photo}" style="float: right; padding: 3px">

<h1 class="st_nadpis">${PROFILE.name}</h1>

<#if PROFILE.nick?exists><p>Pøezdívka: ${PROFILE.nick}</p></#if>

<#if TOOL.xpath(PROFILE,"/data/profile/home_page")?exists>
  <p>Moje domovská stránka: <a href="${TOOL.xpath(PROFILE,"/data/profile/home_page")}">
  ${TOOL.xpath(PROFILE,"/data/profile/home_page")}</a></p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
 <div>O mnì: ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}</div>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/birth_year")?exists>
 <p>Rok narození: ${TOOL.xpath(PROFILE,"/data/personal/birth_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/city")?exists>
 <p>Mìsto: ${TOOL.xpath(PROFILE,"/data/personal/city")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/area")?exists>
 <p>Kraj: ${TOOL.xpath(PROFILE,"/data/personal/area")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/country")?exists>
 <p>Zemì: ${TOOL.xpath(PROFILE,"/data/personal/country")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")?exists>
 <p>Linux pou¾ívám od roku: ${TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/distributions")?exists>
 <div>Pou¾ívám tyto distribuce:
  <ul>
   <#list TOOL.xpaths(PROFILE.data,"/data/profile/distributions/distribution") as dist>
    <li>${dist}
   </#list>
  </ul>
 </div>
</#if>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='yes']")?exists>
 <form action="${URL.noPrefix("/Profile")}">
  <input type="hidden" name="action" value="sendEmail">
  <input type="hidden" name="uid" value="${PROFILE.id}">
  <input type="submit" value="Po¹lete mi email">
 </form>
<#else>
 <p class="error">Administrátoøi oznaèili email u¾ivatele za neplatný!</p>
</#if>

<h1 class="st_nadpis">Mé</h1>
<ol>
  <li><a href="${URL.noPrefix("/History?type=articles&amp;uid="+PROFILE.id)}">èlánky</a>
  (${COUNTS.article})
  <li><a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zprávièky</a>
  (${COUNTS.news})
  <li><a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">otázky ve fóru</a>
  (${COUNTS.question})
  <li><a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">komentáøe</a>
  (${COUNTS.comment})
  <li><a href="${URL.noPrefix("/History?type=hardware&amp;uid="+PROFILE.id)}">hardwarové záznamy</a>
  (${COUNTS.hardware})
  <li><a href="${URL.noPrefix("/History?type=software&amp;uid="+PROFILE.id)}">softwarové záznamy</a>
  (${COUNTS.software})
  <li><a href="${URL.noPrefix("/History?type=dictionary&amp;uid="+PROFILE.id)}">pojmy ve slovníku</a>
  (${COUNTS.dictionary})
</ol>

<#include "../footer.ftl">
