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

<#assign photo = TOOL.xpath(PROFILE,"/data/profile/photo")?default("UNDEFINED")>
<#if photo=="UNDEFINED">
    <#assign photo="/images/faces/default_"+TOOL.xpath(PROFILE,"/data/personal/sex")+".gif">
</#if>
<img src="${photo}" style="float: right; margin: 0.5em" alt="${PROFILE.name}">

<h1>${PROFILE.name}</h1>

<!-- sekce osobni udaje -->

<#if PROFILE.nick?exists>
    <p>Přezdívka: ${PROFILE.nick}</p>
</#if>

<#assign homePage = TOOL.xpath(PROFILE,"/data/profile/home_page")?default("UNDEFINED")>
<#if homePage != "UNDEFINED">
  <p>
      Moje domovská stránka: <a href="${homePage}" rel="nofollow">${homePage}</a>
  </p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/about_myself")?exists>
    <div>
        O mně:
        ${TOOL.render(TOOL.element(PROFILE.data,"/data/profile/about_myself"),USER?if_exists)}
    </div>
</#if>

<#assign birth = TOOL.xpath(PROFILE,"/data/personal/birth_year")?default("UNDEFINED")>
<#if birth != "UNDEFINED">
    <p>Rok narození: ${birth}</p>
</#if>

<#assign city = TOOL.xpath(PROFILE,"/data/personal/city")?default("UNDEFINED")>
<#if city != "UNDEFINED">
    <p>Bydliště: ${city}</p>
</#if>

<#assign area = TOOL.xpath(PROFILE,"/data/personal/area")?default("UNDEFINED")>
<#if area != "UNDEFINED">
    <p>Kraj: ${area}</p>
</#if>

<#assign country = TOOL.xpath(PROFILE,"/data/personal/country")?default("UNDEFINED")>
<#if country != "UNDEFINED">
    <p>Země: ${country}</p>
</#if>

<!-- sekce linux -->

<#assign linuxUserFrom = TOOL.xpath(PROFILE,"/data/profile/linux_user_from_year")?default("UNDEFINED")>
<#if linuxUserFrom != "UNDEFINED">
    <p>Linux používám od roku: ${linuxUserFrom}</p>
</#if>

<#if TOOL.xpath(PROFILE,"/data/profile/distributions")?exists>
    <p>Používám tyto distribuce:</p>
    <ul>
        <#list TOOL.xpaths(PROFILE.data,"/data/profile/distributions/distribution") as dist>
            <li>${dist}</li>
        </#list>
    </ul>
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
    <p>
        <a href="/faq/abclinuxu.cz/co-je-to-skore">Skóre</a>: ${score}
    </p>
</#if>

<#if SOFTWARE?size gt 0>
    <p>Používám tento software:</p>
    <ul>
        <#list SORT.byName(SOFTWARE) as sw>
            <li>
                <a href="${sw.url}">${sw.child.title}</a>
            </li>
        </#list>
    </ul>
</#if>

<#if BLOG?exists>
    <p>Můj blog: <a href="/blog/${BLOG.subType}">${BLOG.title?default("blog")}</a></p>
    <ul>
        <#list STORIES as relation>
            <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
            <#assign CHILDREN=TOOL.groupByType(story.children)>
            <#if CHILDREN.discussion?exists>
                <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
            <#else>
                <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
            </#if>
            <li>
                <a href="${url}">${story.title}</a> | ${DATE.show(story.created, "CZ_DMY")}
                | <span title="<#if diz.responseCount gt 0>poslední ${DATE.show(diz.updated, "CZ_SHORT")}</#if>">
                    komentářů: ${diz.responseCount}<@lib.markNewComments diz/>
                  </span>
            </li>
        </#list>
    </ul>
</#if>

<#if LAST_DESKTOP?exists>
    <p>Můj současný desktop:</p>
    <p>
        <#assign desktop_title=LAST_DESKTOP.title>
        <a href="${LAST_DESKTOP.url}" title="${desktop_title?html}" class="thumb">
            <img src="${LAST_DESKTOP.thumbnailListingUrl}" alt="${desktop_title?html}" border="0">
        </a>
    </p>
</#if>

<#if DESKTOPS?size gt 0>
    <p>Desktopy, které se mi líbí:</p>
    <ul>
        <#list DESKTOPS as ds>
            <li>
                <a href="${ds.url}">${ds.child.title}</a>
            </li>
        </#list>
    </ul>
</#if>

<p>
    <a href="/muj_obsah/${PROFILE.id}" rel="nofollow">Seznam příspěvků na abclinuxu.cz</a><br>
    (články, komentáře, dotazy, zprávičky, softwarové a hardwarové záznamy, pojmy ve slovníku a texty v učebnici)
</p>
<br>

<#if PROFILE.email?exists>
    <#if ! INVALID_EMAIL?if_exists>
        <form action="${URL.noPrefix("/Profile")}">
            <input type="hidden" name="action" value="sendEmail">
            <input type="hidden" name="uid" value="${PROFILE.id}">
            <input type="submit" value="Pošlete mi email">
        </form>
    <#elseif TOOL.xpath(PROFILE,"/data/communication/email[@valid='no']")?exists>
        <p class="error">Administrátoři označili email uživatele za neplatný!</p>
    </#if>
</#if>

<#include "../footer.ftl">
