<#include "../header.ftl">

<@lib.showMessages/>

<#if ! USER?exists>
 <p>Pokud jste ${PROFILE.name}, <a href="${URL.noPrefix("/Profile?action=login")}">přihlašte se</a>
 a bude vám zobrazena vaše domovská stránka.</p>

<#elseif USER.id==PROFILE.id>
 <h2>Moje domovská stránka</h2>
 <p>
     Nacházíte se na své veřejné domovské stránce, která slouží pro vaši prezentaci.
     Tento text je zobrazen pouze vám.
 </p>
 <p>
     <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">Nastavení účtu</a>
 </p>
 <hr />
<#elseif USER.hasRole("user admin")>
 <a href="${URL.noPrefix("/Profile/"+PROFILE.id+"?action=myPage")}">Upravit</a>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/photo")?exists>
 <#assign photo=TOOL.xpath(PROFILE,"/data/profile/photo")>
<#else>
 <#assign photo="/images/faces/default_"+TOOL.xpath(PROFILE,"/data/personal/sex")+".gif">
</#if>
<img src="${photo}" style="float: right; margin: 0.5em" alt="${PROFILE.name}">

<h1>${PROFILE.name}</h1>

<#if PROFILE.nick?exists><p>Přezdívka: ${PROFILE.nick}</p></#if>

<#if TOOL.xpath(PROFILE,"/data/profile/home_page")?exists>
  <p>Moje domovská stránka: <a href="${TOOL.xpath(PROFILE,"/data/profile/home_page")}" rel="nofollow">
  ${TOOL.xpath(PROFILE,"/data/profile/home_page")}</a></p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
 <p>O mně: ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/birth_year")?exists>
 <p>Rok narození: ${TOOL.xpath(PROFILE,"/data/personal/birth_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/city")?exists>
 <p>Bydliště: ${TOOL.xpath(PROFILE,"/data/personal/city")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/area")?exists>
 <p>Kraj: ${TOOL.xpath(PROFILE,"/data/personal/area")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/country")?exists>
 <p>Země: ${TOOL.xpath(PROFILE,"/data/personal/country")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")?exists>
 <p>Linux používám od roku: ${TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/distributions")?exists>
 <p>Používám tyto distribuce:</p>
  <ul>
   <#list TOOL.xpaths(PROFILE.data,"/data/profile/distributions/distribution") as dist>
    <li>${dist}</li>
   </#list>
  </ul>
</#if>

<#if TOOL.xpath(PROFILE,"/data/personal/signature")?exists>
 <p>Patička: ${TOOL.xpath(PROFILE,"/data/personal/signature")}</p>
</#if>

<#if BLOG?exists>
    Můj blog: <a href="/blog/${BLOG.subType}">${TOOL.xpath(BLOG,"//custom/title")?default("blog")}</a>
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
                    komentářů: ${diz.responseCount}<@lib.markNewComments diz/>
                  </span>
            </li>
        </#list>
    </ul>
</#if>

<p><a href="/muj_obsah/${PROFILE.id}">Seznam příspěvků na abclinuxu.cz</a><br>
(články, komentáře, dotazy, zprávičky, softwarové záznamy a pojmy ve slovníku).</p>
<br>

<#if TOOL.xpath(PROFILE,"/data/communication/email[@valid='yes']")?exists>
 <form action="${URL.noPrefix("/Profile")}">
  <input type="hidden" name="action" value="sendEmail">
  <input type="hidden" name="uid" value="${PROFILE.id}">
  <input type="submit" value="Pošlete mi email">
 </form>
<#else>
 <p class="error">Administrátoři označili email uživatele za neplatný!</p>
</#if>

<#include "../footer.ftl">
