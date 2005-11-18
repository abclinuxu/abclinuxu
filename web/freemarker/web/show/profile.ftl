<#include "../header.ftl">

<@lib.showMessages/>

<#if ! USER?exists>
 <p>Pokud jste ${PROFILE.name}, <a href="${URL.noPrefix("/Profile?action=login")}">pøihlaste se</a>
 a bude vám zobrazena va¹e domovská stránka.
 </p>
<#elseif USER.id==PROFILE.id>
 <#if PARAMS.LOGIN?exists>
  <h1>Vítejte</h1>
  <p>Dìkujeme vám za dal¹í náv¹tìvu portálu abclinuxu.cz. Doufáme, ¾e vám
  pøinese cenné informace. Pokud budete spokojeni, doporuète nás svým pøátelùm
  a známým.</p>
 </#if>

 <h1>Moje domovská stránka</h1>

 <p>Nacházíte se na své veøejné domovské stránce, která slou¾í pro va¹i prezentaci.
 Pokud ji chcete doplnit èi nìjak upravit, pøejdìte na
 <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">tuto stránku</a>.
 Tam také mù¾ete zmìnit nastavení svého u¾ivatele (napøíklad se pøihlásit k odbìru zpravodaje).
 Text nad èarou vidíte pouze vy, ostatním náv¹tìvníkùm není zobrazen.
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

<#if PROFILE.nick?exists><p>Pøezdívka: ${PROFILE.nick}</p></#if>

<#if TOOL.xpath(PROFILE,"/data/profile/home_page")?exists>
  <p>Moje domovská stránka: <a href="${TOOL.xpath(PROFILE,"/data/profile/home_page")}" rel="nofollow">
  ${TOOL.xpath(PROFILE,"/data/profile/home_page")}</a></p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
 <p>O mnì: ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/birth_year")?exists>
 <p>Rok narození: ${TOOL.xpath(PROFILE,"/data/personal/birth_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/city")?exists>
 <p>Bydli¹tì: ${TOOL.xpath(PROFILE,"/data/personal/city")}</p>
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
 <p>Pou¾ívám tyto distribuce:</p>
  <ul>
   <#list TOOL.xpaths(PROFILE.data,"/data/profile/distributions/distribution") as dist>
    <li>${dist}</li>
   </#list>
  </ul>
</#if>

<#if BLOG?exists>
    Mùj blog: <a href="/blog/${BLOG.subType}">${TOOL.xpath(BLOG,"//custom/title")?default("blog")}</a>
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
                | <span title="<#if diz.responseCount gt 0>poslední ${DATE.show(diz.updated, "CZ_SHORT")}</#if>">
                    komentáøù: ${diz.responseCount}
                  </span>
            </li>
        </#list>
    </ul>
</#if>

<p><a href="/muj_obsah/${PROFILE.id}">Seznam pøíspìvkù na abclinuxu.cz</a><br>
(èlánky, komentáøe, dotazy, zprávièky, hardwarové a softwarové záznamy, pojmy ve slovníku).</p>
<br>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='yes']")?exists>
 <form action="${URL.noPrefix("/Profile")}">
  <input type="hidden" name="action" value="sendEmail">
  <input type="hidden" name="uid" value="${PROFILE.id}">
  <input type="submit" value="Po¹lete mi email">
 </form>
<#else>
 <p class="error">Administrátoøi oznaèili email u¾ivatele za neplatný!</p>
</#if>

<#include "../footer.ftl">
