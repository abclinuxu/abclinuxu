<#include "../header.ftl">

<#if TOOL.xpath(PROFILE,"/data/profile/photo")?exists>
 <#assign photo=TOOL.xpath(PROFILE,"/data/profile/photo")>
<#else>
 <#assign photo="/images/faces/default_"+TOOL.xpath(PROFILE,"/data/personal/sex")+".gif">
</#if>
<img src="${photo}" style="float: right; padding: 3px">

<h1>${PROFILE.name}</h1>

<#if PROFILE.nick?exists><p>P�ezd�vka: ${PROFILE.nick}</p></#if>

<#if TOOL.xpath(PROFILE,"/data/profile/home_page")?exists>
  <p>Moje domovsk� str�nka: <a href="${TOOL.xpath(PROFILE,"/data/profile/home_page")}">
  ${TOOL.xpath(PROFILE,"/data/profile/home_page")}</a></p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
 <p>O mn�: ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/birth_year")?exists>
 <p>Rok narozen�: ${TOOL.xpath(PROFILE,"/data/personal/birth_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/city")?exists>
 <p>M�sto: ${TOOL.xpath(PROFILE,"/data/personal/city")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/area")?exists>
 <p>Kraj: ${TOOL.xpath(PROFILE,"/data/personal/area")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/country")?exists>
 <p>Zem�: ${TOOL.xpath(PROFILE,"/data/personal/country")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")?exists>
 <p>Linux pou��v�m od roku: ${TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/distributions")?exists>
 <p>Pou��v�m tyto distribuce:</p>
  <ul>
   <#list TOOL.xpaths(PROFILE.data,"/data/profile/distributions/distribution") as dist>
    <li>${dist}
   </#list>
  </ul>
</#if>

<#include "../footer.ftl">
