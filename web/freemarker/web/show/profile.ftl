<#include "../header.ftl">

<@lib.showMessages/>

<#if ! USER?exists>
 <p>Pokud jste ${PROFILE.name}, <a href="${URL.noPrefix("/Profile?action=login")}">p�ihla�te se</a>
 a bude v�m zobrazena va�e domovsk� str�nka.
 </p>
<#elseif USER.id==PROFILE.id>
 <#if PARAMS.LOGIN?exists>
  <h1 class="st_nadpis">V�tejte</h1>
  <p>D�kujeme v�m za dal�� n�v�t�vu port�lu AbcLinuxu. Douf�me, �e v�m
  p�inese cenn� informace. Pokud budete spokojeni, doporu�te n�s sv�m p��tel�m
  a zn�m�m.
  </p>
 </#if>

 <h1 class="st_nadpis">Moje domovsk� str�nka</h1>

 <p>Nach�z�te se ve sv� ve�ejn� domovsk� str�nce, kter� slou�� pro va�i prezentaci.
 Pokud ji chcete doplnit �i n�jak upravit, p�ejd�te na
 <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">tuto str�nku</a>.
 Tam tak� m��ete zm�nit nastaven� sv�ho u�ivatele, jako nap��klad p�ihl�sit se k odb�ru zpravodaje.
 Text nad �arou vid�te pouze vy, ostatn�m n�v�t�vn�k�m nen� zobrazen.
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

<#if PROFILE.nick?exists><p>P�ezd�vka: ${PROFILE.nick}</p></#if>

<#if TOOL.xpath(PROFILE,"/data/profile/home_page")?exists>
  <p>Moje domovsk� str�nka: <a href="${TOOL.xpath(PROFILE,"/data/profile/home_page")}">
  ${TOOL.xpath(PROFILE,"/data/profile/home_page")}</a></p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
 <div>O mn�: ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}</div>
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
 <div>Pou��v�m tyto distribuce:
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
  <input type="submit" value="Po�lete mi email">
 </form>
<#else>
 <p class="error">Administr�to�i ozna�ili email u�ivatele za neplatn�!</p>
</#if>

<h1 class="st_nadpis">M�</h1>
<ol>
  <li><a href="${URL.noPrefix("/History?type=articles&amp;uid="+PROFILE.id)}">�l�nky</a>
  (${COUNTS.article})
  <li><a href="${URL.noPrefix("/History?type=news&amp;uid="+PROFILE.id)}">zpr�vi�ky</a>
  (${COUNTS.news})
  <li><a href="${URL.noPrefix("/History?type=questions&amp;uid="+PROFILE.id)}">ot�zky ve f�ru</a>
  (${COUNTS.question})
  <li><a href="${URL.noPrefix("/History?type=comments&amp;uid="+PROFILE.id)}">koment��e</a>
  (${COUNTS.comment})
  <li><a href="${URL.noPrefix("/History?type=hardware&amp;uid="+PROFILE.id)}">hardwarov� z�znamy</a>
  (${COUNTS.hardware})
  <li><a href="${URL.noPrefix("/History?type=software&amp;uid="+PROFILE.id)}">softwarov� z�znamy</a>
  (${COUNTS.software})
  <li><a href="${URL.noPrefix("/History?type=dictionary&amp;uid="+PROFILE.id)}">pojmy ve slovn�ku</a>
  (${COUNTS.dictionary})
</ol>

<#include "../footer.ftl">
