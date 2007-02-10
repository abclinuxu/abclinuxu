<#import "../macros.ftl" as lib>
<#assign intro=TOOL.xpath(BLOG,"//custom/intro")?default("UNDEF"),
        title=TOOL.xpath(BLOG,"//custom/title")?default("UNDEF"),
        owner=TOOL.createUser(BLOG.owner),
        ITEM=STORY.child,
        CHILDREN=TOOL.groupByType(ITEM.children),
        category = ITEM.subType?default("UNDEF"),
        story_url = TOOL.getUrlForBlogStory(BLOG.subType, ITEM.created, STORY.id)>
<#if category!="UNDEF"><#assign category=TOOL.xpath(BLOG, "//category[@id='"+category+"']/@name")?default("UNDEF")></#if>

<#assign plovouci_sloupec>

    <div class="s_nadpis">
        <a href="/Profile/${owner.id}">${owner.nick?default(owner.name)}</a>
        <#if title!="UNDEF"> - <a href="/blog/${BLOG.subType}">${title}</a></#if>
    </div>

    <div class="s_sekce">
        <#if intro!="UNDEF">${intro}</#if>
    </div>

    <#if UNPUBLISHED_STORIES?exists>
        <div class="s_nadpis">Rozepsané zápisy</div>

        <div class="s_sekce">
            <ul>
            <#list UNPUBLISHED_STORIES as relation>
                <#assign story=relation.child, url=TOOL.getUrlForBlogStory(BLOG.subType, story.created, relation.id)>
                <li>
                    <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
                </li>
            </#list>
            </ul>
        </div>
    </#if>

    <div class="s_nadpis">Aktuální zápisy</div>

    <div class="s_sekce">
        <ul>
        <#list CURRENT_STORIES as relation>
            <#assign story=relation.child, url=TOOL.getUrlForBlogStory(BLOG.subType, story.created, relation.id)>
            <li>
                <a href="${url}">${TOOL.xpath(story, "/data/name")}</a>
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

    <#if (USER?exists && (USER.id==BLOG.owner || USER.hasRole("root"))) || (! USER?exists)>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdružuje akce pro majitele blogu.</span></a>
            Nastavení
        </div>
    </#if>

  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.hasRole("blog digest admin")>
            <#if (ITEM.getProperty("digest")?size > 0)><#assign digestMsg='Odstranit z digestu'><#else><#assign digestMsg='Přidat do digestu'></#if>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=toggleDigest")}">${digestMsg}</a></li>
        </#if>
        <#if USER.id==BLOG.owner || USER.hasRole("root")>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=edit")}">Uprav zápis</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=remove")}">Smaž zápis</a></li>
        </#if>
        <#if USER.hasRole("attachment admin")>
            <li><a href="${URL.make("/inset/"+STORY.id+"?action=manage")}">Správa příloh</a></li>
        </#if>
        <#if USER.id==BLOG.owner>
            <#if !CHILDREN.poll?exists>
                <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+STORY.id)}">Vlož anketu</a></li>
            </#if>
            <li>
                <a href="${URL.make("/inset/"+STORY.id+"?action=addScreenshot")}">Přidej obrázek</a>
                <hr>
            </li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlož nový zápis</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavit blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Přejmenovat blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Upravit kategorie</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit odkazy</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">Přihlásit se</a></li>
    </#if>
    </ul>
  </div>
  <#--<hr id="arbo-sq-cara" />
  <@lib.advertisement id="arbo-sq" />-->
  <@lib.advertisement id="gg-sq-blog" />

</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h2>${TOOL.xpath(ITEM, "/data/name")}</h2>
<p class="cl_inforadek">
    <#if ITEM.type==15>Odloženo<#else>${DATE.show(ITEM.created, "CZ_SHORT")}</#if> |
    Přečteno: ${TOOL.getCounterValue(ITEM,"read")}&times;
    <#if category!="UNDEF">| ${category}</#if>
    <#if (ITEM.type==12 && ITEM.created.time!=ITEM.updated.time)>
        | poslední úprava: ${DATE.show(ITEM.updated, "CZ_SHORT")}
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
    <h3>Obrázky</h3>

    <p class="galerie">
        <#list images as image>
            <#if image.thumbnailPath?exists>
                <a href="${image.path}"><img src="${image.thumbnailPath}" alt="Obrázek ${image_index}" border="0"></a>
            <#else>
                <img src="${image.path}" alt="Obrázek ${image_index}">
            </#if>
        </#list>
    </p>
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
