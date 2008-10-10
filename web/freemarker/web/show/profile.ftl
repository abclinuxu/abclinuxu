<#include "../header.ftl">

<@lib.showMessages/>

<#if ! USER?exists>
    <p>
        Pokud jste ${PROFILE.name}, <a href="${URL.noPrefix("/Profile?action=login")}">přihlaste se</a>
        a bude vám zobrazena vaše domovská stránka.
    </p>
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

<#assign photo = TOOL.xpath(PROFILE,"/data/profile/photo")?default("UNDEFINED"),
    sex = TOOL.xpath(PROFILE,"/data/personal/sex")?default("UNDEFINED")>
<#if photo=="UNDEFINED" && sex!="UNDEFINED">
    <#assign photo="/images/faces/default_"+sex+".gif">
</#if>
<#if photo!="UNDEFINED">
    <img src="${photo}" style="float: right; margin: 0.5em" alt="${PROFILE.name}">
</#if>

<h1>${PROFILE.name}</h1>

<!-- sekce osobni udaje -->

<#if PROFILE.nick?exists><p>Přezdívka: ${PROFILE.nick}</p></#if>

<#assign homePage = TOOL.xpath(PROFILE,"/data/profile/home_page")?default("UNDEFINED")>
<#if homePage != "UNDEFINED">
    <p>Moje domovská stránka: <a href="${homePage}" rel="nofollow">${homePage}</a></p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
    <p>O mně: ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}</p>
</#if>

<p>
<#assign birth = TOOL.xpath(PROFILE,"/data/personal/birth_year")?default("UNDEFINED")>
<#if birth != "UNDEFINED">Rok narození: ${birth}<br /></#if>

<#assign city = TOOL.xpath(PROFILE,"/data/personal/city")?default("UNDEFINED")>
<#if city != "UNDEFINED">Bydliště: ${city}<br /></#if>

<#assign area = TOOL.xpath(PROFILE,"/data/personal/area")?default("UNDEFINED")>
<#if area != "UNDEFINED">Kraj: ${area}<br /></#if>

<#assign country = TOOL.xpath(PROFILE,"/data/personal/country")?default("UNDEFINED")>
<#if country != "UNDEFINED">Země: ${country}</#if>
</p>

<!-- sekce linux -->

<#assign linuxUserFrom = TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")?default("UNDEFINED")>
<#if linuxUserFrom != "UNDEFINED">
    <p>Linux používám od roku: ${linuxUserFrom}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/distributions")?exists>
<div class="profile_list reverse_anchor">
    <h2>Používám tyto distribuce:</h2>
    <ul>
        <#list TOOL.xpaths(PROFILE.data,"/data/profile/distributions/distribution") as dist>
            <li>${dist}</li>
        </#list>
    </ul>
</div>
</#if>

<!-- sekce abclinuxu -->

<#assign signature = TOOL.xpath(PROFILE,"/data/personal/signature")?default("UNDEFINED")>
<#if signature != "UNDEFINED">
    <p>Patička: ${signature}</p>
</#if>

<#assign registered = TOOL.xpath(PROFILE,"/data/system/registration_date")?default("UNDEFINED")>
<p>
    Datum registrace:
    <#if registered != "UNDEFINED">
        ${DATE.show(registered, "CZ_DMY")}
    <#else>
        starší než 12. 7. 2003
    </#if>
</p>

<#assign score=PROFILE.getIntProperty("score")?default(-1)>
<#if score != -1>
    <p><a href="/faq/abclinuxu.cz/co-je-to-skore">Skóre</a>: ${score}</p>
</#if>

<#if SOFTWARE?size gt 0>
<div class="ri profile_list reverse_anchor" style="clear: right;">
    <h2>Používám tento software:</h2>
    <ul>
      <#list SORT.byName(SOFTWARE) as sw>
        <li><a href="${sw.url}" title="${sw.child.title}">${sw.child.title}</a></li>
      </#list>
    </ul>
</div>
</#if>

<#if DESKTOPS?size gt 0>
<div class="ri profile_list reverse_anchor" style="clear: right;">
    <h2>Desktopy, které se mi líbí:</h2>
    <ul>
        <#list DESKTOPS as ds>
            <li><a href="${ds.url}">${ds.child.title?html}</a></li>
        </#list>
    </ul>
</div>
</#if>

<#if VIDEOS?size gt 0>
<div class="ri profile_list reverse_anchor" style="clear: right;">
    <h2>Videa, která se mi líbí:</h2>
    <ul>
        <#list VIDEOS as vd>
            <li><a href="${vd.url}">${vd.child.title?html}</a></li>
        </#list>
    </ul>
</div>
</#if>

<#if BLOG?exists>
<div class="profile_list reverse_anchor">
    <h2>Můj blog: <a href="/blog/${BLOG.subType}">${BLOG.title?default("blog")}</a></h2>
    <ul>
      <#list STORIES as relation>
        <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
        <#assign CHILDREN=TOOL.groupByType(story.children)>
        <#if CHILDREN.discussion?exists>
          <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
        <#else>
          <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
        </#if>
        <li><a href="${url}">${story.title}</a> | ${DATE.show(story.created, "CZ_DMY")} | <span title="<#if diz.responseCount gt 0>poslední ${DATE.show(diz.updated, "CZ_SHORT")}</#if>"> komentářů: ${diz.responseCount}<@lib.markNewComments diz/></span></li>
      </#list>
    </ul>
</div>
</#if>

<#if SUBPORTALS?size gt 0>
<div class="profile_list reverse_anchor">
    <h2>Jsem členem těchto skupin:</h2>
    <ul>
        <#list SUBPORTALS as rel>
            <li><a href="${rel.url}">${TOOL.childName(rel)}</a></li>
        </#list>
    </ul>
</div>
</#if>

<#if LAST_DESKTOP?exists>
    <h3>Můj současný desktop:</h3>
    <p>
        <#assign desktop_title=LAST_DESKTOP.title>
        <a href="${LAST_DESKTOP.url}" title="${desktop_title?html}" class="thumb">
            <img src="${LAST_DESKTOP.thumbnailListingUrl}" alt="${desktop_title?html}" border="0">
        </a>
    </p>
</#if>

<div class="profile_list reverse_anchor">
    <h2>Ostatní</h2>
    <ul>
      <#if TOOL.xpath(PROFILE,"/data/profile/gpg")?default("UNDEF")!="UNDEF">
        <li><a href="/lide/${PROFILE.login}/gpg">Můj veřejný GPG klíč</a></li>
      </#if>
        <li><a href="/lide/${PROFILE.login}/zalozky">Moje záložky</a></li>
        <li><a href="/lide/${PROFILE.login}/objekty" rel="nofollow">Seznam příspěvků na abclinuxu.cz</a><br />
        (články, komentáře, dotazy, zprávičky, softwarové a hardwarové záznamy, pojmy ve slovníku a texty v učebnici)</li>
    </ul>
</div>

<#if PROFILE.email?exists>
  <div>
    <#if ! INVALID_EMAIL?if_exists>
        <form action="${URL.noPrefix("/Profile")}">
            <input type="hidden" name="action" value="sendEmail">
            <input type="hidden" name="uid" value="${PROFILE.id}">
            <input type="submit" value="Pošlete mi email">
        </form>
    <#elseif TOOL.xpath(PROFILE,"/data/communication/email[@valid='no']")?exists>
        <p class="error">Administrátoři označili email uživatele za neplatný!</p>
    </#if>
  </div>
</#if>

<#include "../footer.ftl">
