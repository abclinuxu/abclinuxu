<#include "../header.ftl">

<@lib.showMessages/>

<#if ! USER?exists>
 <p>Pokud jste ${PROFILE.name}, <a href="${URL.noPrefix("/Profile?action=login")}">p�ihlaste se</a>
 a bude v�m zobrazena va�e domovsk� str�nka.
 </p>
<#elseif USER.id==PROFILE.id>
 <#if PARAMS.LOGIN?exists>
  <h1>V�tejte</h1>
  <p>D�kujeme v�m za dal�� n�v�t�vu port�lu abclinuxu.cz. Douf�me, �e v�m
  p�inese cenn� informace. Pokud budete spokojeni, doporu�te n�s sv�m p��tel�m
  a zn�m�m.</p>
 </#if>

 <h1>Moje domovsk� str�nka</h1>

 <p>Nach�z�te se na sv� ve�ejn� domovsk� str�nce, kter� slou�� pro va�i prezentaci.
 Pokud ji chcete doplnit �i n�jak upravit, p�ejd�te na
 <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">tuto str�nku</a>.
 Tam tak� m��ete zm�nit nastaven� sv�ho u�ivatele (nap��klad se p�ihl�sit k odb�ru zpravodaje).
 Text nad �arou vid�te pouze vy, ostatn�m n�v�t�vn�k�m nen� zobrazen.
 </p>
 <hr>
<#elseif USER.hasRole("user admin")>
 <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">Upravit</a>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/photo")?exists>
 <#assign photo=TOOL.xpath(PROFILE,"/data/profile/photo")>
<#else>
 <#assign photo="/images/faces/default_"+TOOL.xpath(PROFILE,"/data/personal/sex")+".gif">
</#if>
<img src="${photo}" style="float: right; margin: 0.5em">

<h1>${PROFILE.name}</h1>

<#if PROFILE.nick?exists><p>P�ezd�vka: ${PROFILE.nick}</p></#if>

<#if TOOL.xpath(PROFILE,"/data/profile/home_page")?exists>
  <p>Moje domovsk� str�nka: <a href="${TOOL.xpath(PROFILE,"/data/profile/home_page")}" rel="nofollow">
  ${TOOL.xpath(PROFILE,"/data/profile/home_page")}</a></p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
 <p>O mn�: ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/birth_year")?exists>
 <p>Rok narozen�: ${TOOL.xpath(PROFILE,"/data/personal/birth_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/city")?exists>
 <p>Bydli�t�: ${TOOL.xpath(PROFILE,"/data/personal/city")}</p>
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
    <li>${dist}</li>
   </#list>
  </ul>
</#if>

<#if BLOG?exists>
    M�j blog: <a href="/blog/${BLOG.subType}">${TOOL.xpath(BLOG,"//custom/title")?default("blog")}</a>
    <ul>
        <#list STORIES as relation>
            <#assign story=relation.child, url=TOOL.getUrlForBlogStory(BLOG.subType, story.created, relation.id)>
            <#assign CHILDREN=TOOL.groupByType(story.children)>
            <#if CHILDREN.discussion?exists>
                <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
            <#else>
                <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
            </#if>
            <li>
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a> | ${DATE.show(story.created, "CZ_DMY")}
                | <span title="<#if diz.responseCount gt 0>posledn� ${DATE.show(diz.updated, "CZ_SHORT")}</#if>">
                    koment���: ${diz.responseCount}
                  </span>
            </li>
        </#list>
    </ul>
</#if>

<p><a href="/muj_obsah/${PROFILE.id}">Seznam p��sp�vk� na abclinuxu.cz</a><br>
(�l�nky, koment��e, dotazy, zpr�vi�ky, hardwarov� a softwarov� z�znamy, pojmy ve slovn�ku).</p>
<br>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='yes']")?exists>
 <form action="${URL.noPrefix("/Profile")}">
  <input type="hidden" name="action" value="sendEmail">
  <input type="hidden" name="uid" value="${PROFILE.id}">
  <input type="submit" value="Po�lete mi email">
 </form>
<#else>
 <p class="error">Administr�to�i ozna�ili email u�ivatele za neplatn�!</p>
</#if>

<#include "../footer.ftl">
