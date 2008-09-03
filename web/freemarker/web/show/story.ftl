<#import "../macros.ftl" as lib>
<#assign intro=TOOL.xpath(BLOG,"//custom/intro")?default("UNDEF"),
        title=BLOG.title?default("UNDEF"),
        owner=TOOL.createUser(BLOG.owner),
        ITEM=STORY.child,
        CHILDREN=TOOL.groupByType(ITEM.children),
        story_url = TOOL.getUrlForBlogStory(STORY)>


<#assign plovouci_sloupec>
    <div class="s_nadpis">
        <@lib.showUser owner/>
        <#if title!="UNDEF"> - <a href="/blog/${BLOG.subType}">${title}</a></#if>
    </div>

    <div class="s_sekce">
        <#if intro!="UNDEF">${intro}</#if>
    </div>

    <div class="s_nadpis">
        <#if USER?exists && USER.id==BLOG.owner>
            <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Kategorie zápisů</a>
        <#else>
            Kategorie zápisů
        </#if>
    </div>

    <div class="s_sekce">
    <ul>
        <#list CATEGORIES as cat>
            <#if cat.url?exists>
                <li><a href="/blog/${BLOG.subType + "/" + cat.url}">${cat.name}</a></li>
            </#if>
        </#list>
    </div>

    <#if UNPUBLISHED_STORIES?exists>
        <div class="s_nadpis">Rozepsané zápisky</div>

        <div class="s_sekce">
            <ul>
            <#list UNPUBLISHED_STORIES as relation>
                <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
                <li>
                    <a href="${url}">${story.title}</a>
                </li>
            </#list>
            </ul>
        </div>
    </#if>

    <#if WAITING_STORIES?exists>
        <div class="s_nadpis">Čekající zápisky</div>

        <div class="s_sekce">
            <ul>
            <#list WAITING_STORIES as relation>
                <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
                <li>
                    <a href="${url}">${story.title}</a>
                </li>
            </#list>
            </ul>
        </div>
    </#if>

    <#if USER?exists && (USER.id==BLOG.owner || USER.hasRole("root") || USER.hasRole("blog digest admin"))>
        <div class="s_nadpis">
            Správa zápisku
        </div>

        <div class="s_sekce">
            <ul>
                <#if USER.hasRole("blog digest admin")>
                    <#if (ITEM.getProperty("banned_blog")?size > 0)>
                        <#assign banMsg='Není nevhodný pro HP'>
                    <#else>
                        <#assign banMsg='Nevhodný pro HP'>
                        <#if (ITEM.getProperty("digest")?size > 0)>
                            <#assign digestMsg='Odstranit z digestu'>
                        <#else>
                            <#assign digestMsg='Přidat do digestu'>
                        </#if>
                        <li>
                            <a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=toggleDigest"+TOOL.ticket(USER, false))}">${digestMsg}</a>
                        </li>
                    </#if>
                    <li>
                        <a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=toggleBlogBan"+TOOL.ticket(USER, false))}">${banMsg}</a>
                        <hr>
                    </li>
                </#if>
                <#if USER.id==BLOG.owner || USER.hasRole("root")>
                    <li>
                        <a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=edit")}">Uprav zápis</a>
                    </li>
                    <#if ITEM.type==15>
                        <li>
                            <a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=publish"+TOOL.ticket(USER, false))}">Publikuj zápis</a>
                        </li>
                    </#if>
                    <li>
                        <a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=remove")}">Smaž zápis</a>
                    </li>
                </#if>
                <#if USER.id==BLOG.owner>
                    <li>
                        <a href="${URL.make("/inset/"+STORY.id+"?action=addScreenshot")}">Přidej obrázek</a>
                    </li>
                    <li>
                        <a href="${URL.noPrefix("/videa/edit/"+STORY.id+"?action=add&amp;redirect="+STORY.url?if_exists)}">Přidej video</a>
                    </li>
                    <#if !CHILDREN.poll?exists>
                        <li>
                            <a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+STORY.id)}">Vlož anketu</a>
                        </li>
                    </#if>
                </#if>
                <#if USER.hasRole("root") || USER.id==BLOG.owner>
                    <li>
                        <a href="${URL.make("/inset/"+STORY.id+"?action=manage")}">Správa příloh</a>
                    </li>
                </#if>
            </ul>
        </div>

        <#if USER.id==BLOG.owner || USER.hasRole("root")>
            <div class="s_nadpis">
                Správa blogu
            </div>

            <div class="s_sekce">
                <ul>
                    <li>
                        <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlož nový zápis</a>
                    </li>
                    <li>
                        <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavit blog</a>
                    </li>
                    <li>
                        <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Přejmenovat blog</a>
                    </li>
                    <li>
                        <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit odkazy</a>
                    </li>
                    <li>
                        <a href="${URL.noPrefix("/blog/"+BLOG.subType+"/export")}">Export do Movable Type</a>
                    </li>
                </ul>
            </div>
        </#if>
    </#if>

    <div class="s_nadpis">Aktuální zápisy</div>

    <div class="s_sekce">
        <ul>
        <#list CURRENT_STORIES as relation>
            <#assign story=relation.child, url=TOOL.getUrlForBlogStory(relation)>
            <li>
                <a href="${url}">${story.title}</a>
            </li>
        </#list>
        </ul>
    </div>

    <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Přístup k archivovaným zápisům za jednotlivé měsíce.</span></a>
            <a href="/blog/${BLOG.subType}/archiv">Archív</a>
    </div>

    <div class="s_sekce">
        <ul>
        <#list ARCHIVE as item>
            <li>
                <a href="/blog/${BLOG.subType}/${item.year}/${item.month}/"><@lib.month month=""+item.month/>
                ${item.year} (${item.size})</a>
            </li>
        </#list>
        </ul>
    </div>

    <#assign links = BLOG_XML.data.custom.links.link>
    <#if links?size!=0>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Seznam mých oblíbených stránek, které pravidelně navštěvuji.</span></a>
            Oblíbené stránky
        </div>

        <div class="s_sekce">
            <ul>
            <#list links as link>
                <li><a href="${link}" rel="nofollow">${link.@caption}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <#if LAST_DESKTOP?exists>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Poslední screenshot mého desktopu.</span></a>
            Současný desktop
        </div>

        <div class="s_sekce" align="center">
            <#assign desktop_title=LAST_DESKTOP.title>
            <a href="${LAST_DESKTOP.url}" title="${desktop_title?html}" class="thumb">
                <img src="${LAST_DESKTOP.thumbnailListingUrl}" alt="${desktop_title?html}" border="0">
            </a>
        </div>
    </#if>

    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Přístup na osobní hlavní stranu a na hlavní stranu všech blogů.</span></a>
        Navigace
    </div>

    <div class="s_sekce">
        <ul>
            <#if title!="UNDEF">
                <li><a href="/blog/${BLOG.subType}">${title}, hlavní strana</a></li>
            </#if>
            <li><a href="/blog/${BLOG.subType}/souhrn"><#if title!="UNDEF">${title}, </#if>stručný souhrn</a></li>
            <li><a href="/auto/blog/${BLOG.subType}.rss">RSS kanál</a></li>
            <li><a href="/blog">Všechny blogy</a></li>
	    <li><a href="/blog/souhrn">Všechny blogy, stručný souhrn</a></li>
        </ul>
    </div>
    <div class="s_nadpis"><a href="/nej">Nej blogů na AbcLinuxu</a></div>
    <div class="s_sekce">
        <#if VARS.recentMostReadStories?exists>
            <b>Nejčtenější za poslední měsíc</b>
            <ul>
                <#list VARS.recentMostReadStories.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>

        <#if VARS.recentMostCommentedStories?exists>
            <b>Nejkomentovanější za poslední měsíc</b>
            <ul>
                <#list VARS.recentMostCommentedStories.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>
    </div>
  <#--<hr id="arbo-sq-cara" />
  <@lib.advertisement id="arbo-sq" />-->
  <@lib.advertisement id="gg-sq-blog" />

</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h2>${ITEM.title}</h2>
<p class="meta-vypis">
    <#if ITEM.type==15>Odloženo<#else>${DATE.show(ITEM.created, "SMART")}</#if> |
    Přečteno: ${TOOL.getCounterValue(ITEM,"read")}&times;
    <#if CATEGORY?exists>|
        <#if CATEGORY.url?exists>
            <a href="/blog/${BLOG.subType + "/" + CATEGORY.url}" title="Kategorie zápisu">${CATEGORY.name}</a>
        <#else>
            ${CATEGORY.name}
        </#if>
    </#if>
    <#if (ITEM.type==12 && ITEM.created.time!=ITEM.updated.time)>
        | poslední úprava: ${DATE.show(ITEM.updated, "SMART")}
    </#if>
    <#if DATE.now().compareTo(ITEM.created) lt 0>
        | <span style="color: red; font-weight: bold">Čeká na čas publikování</span>
    </#if>
</p>

<#assign text = TOOL.xpath(ITEM, "/data/perex")?default("UNDEF")>
<#if text!="UNDEF">${text}</#if>
${TOOL.xpath(ITEM, "/data/content")}

<@lib.showRating STORY/>

<#if CHILDREN.poll?exists>
<br />
    <h3>Anketa</h3>
    <div class="anketa">
        <@lib.showPoll CHILDREN.poll[0], story_url />
    </div>
</#if>

<#assign images = TOOL.screenshotsFor(ITEM)>
<#if (images?size > 0)>
    <#assign wrote_section=false>

        <#list images as image>
            <#if !image.hidden>
                <#if !wrote_section><h3>Obrázky</h3><p class="galerie"><#assign wrote_section=true></#if>

                <#if image.thumbnailPath?exists>
                    <a href="${image.path}"><img src="${image.thumbnailPath}" alt="Obrázek ${image_index}" border="0"></a>
                <#else>
                    <img src="${image.path}" alt="${ITEM.title}, obrázek ${image_index}">
                </#if>
            <#elseif USER?exists && (USER.id==BLOG.owner || TOOL.permissionsFor(USER, RELATION).canModify())>
                <#if !wrote_section><h3>Obrázky</h3><p class="galerie"><#assign wrote_section=true></#if>
                <li><a href="${image.path}">${image.originalFilename}</a> <#if image.thumbnailPath?exists>(<a href="${image.thumbnailPath}">náhled</a>)</#if></li>
            </#if>
        </#list>
    <#if wrote_section></p></#if>
</#if>

<#if CHILDREN.video?exists>
    <h3>Videa</h3>
    <#list CHILDREN.video as video>
        <div>
        <#assign showVideoManagement=false>
        <#if USER?exists>
            <#if TOOL.permissionsFor(USER,video).canModify() || video.child.owner == USER.id><#assign showVideoManagement=true></#if>
        </#if>
        <@lib.showVideo video, 300, 300, showVideoManagement />
        </div>
    </#list>
</#if>

<p><b>Nástroje</b>: <a rel="nofollow" href="${story_url}?varianta=print">Tisk</a></p>

<#if (ITEM.type==12)>
    <h3>Komentáře</h3>
    <#if CHILDREN.discussion?exists>
        <@lib.showDiscussion CHILDREN.discussion[0]/>
    <#else>
       <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+STORY.id)}">Vložit první komentář</a>
    </#if>
</#if>


<#include "../footer.ftl">
