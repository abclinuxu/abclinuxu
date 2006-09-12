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
        <div class="s_nadpis">Rozepsan� z�pisy</div>

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

    <div class="s_nadpis">Aktu�ln� z�pisy</div>

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
            <a class="info" href="#">?<span class="tooltip">P��stup k archivovan�m z�pis�m za jednotliv� m�s�ce.</span></a>
            <a href="/blog/${BLOG.subType}/archiv">Arch�v</a>
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
            <a class="info" href="#">?<span class="tooltip">Seznam m�ch obl�ben�ch str�nek, kter� pravideln� nav�t�vuji.</span></a>
            Obl�ben� str�nky
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
        <a class="info" href="#">?<span class="tooltip">P��stup na osobn� hlavn� stranu a na hlavn� stranu v�ech blog�.</span></a>
        Navigace
    </div>

    <div class="s_sekce">
        <ul>
            <#if title!="UNDEF">
                <li><a href="/blog/${BLOG.subType}">${title}, hlavn� strana</a></li>
            </#if>
            <li><a href="/blog/${BLOG.subType}/souhrn"><#if title!="UNDEF">${title}, </#if>stru�n� souhrn</a></li>
            <li><a href="/auto/blog/${BLOG.subType}.rss">RSS kan�l</a></li>
            <li><a href="/blog">V�echny blogy</a></li>
	    <li><a href="/blog/souhrn">V�echny blogy, stru�n� souhrn</a></li>
        </ul>
    </div>

    <#if (USER?exists && (USER.id==BLOG.owner || USER.hasRole("root"))) || (! USER?exists)>
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdru�uje akce pro majitele blogu.</span></a>
            Nastaven�
        </div>
    </#if>

  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.id==BLOG.owner || USER.hasRole("root")>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=edit")}">Uprav z�pis</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=remove")}">Sma� z�pis</a></li>
        </#if>
        <#if USER.hasRole("blog digest admin")>
            <#if (ITEM.getProperty("digest")?size > 0)><#assign digestMsg='Odstranit z digestu'><#else><#assign digestMsg='P�idat do digestu'></#if>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=toggleDigest")}">${digestMsg}</a></li>
        </#if>
        <#if USER.id==BLOG.owner>
            <#if !CHILDREN.poll?exists>
                <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+STORY.id)}">Vlo� anketu</a></li>
            </#if>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlo� nov� z�pis</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavit blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">P�ejmenovat blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Upravit kategorie</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit odkazy</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">P�ihl�sit se</a></li>
    </#if>
    </ul>
  </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h2>${TOOL.xpath(ITEM, "/data/name")}</h2>
<p class="cl_inforadek">
    <#if ITEM.type==15>Odlo�eno<#else>${DATE.show(ITEM.created, "CZ_SHORT")}</#if> |
    P�e�teno: ${TOOL.getCounterValue(ITEM,"read")}x
    <#if category!="UNDEF">| ${category}</#if>
    <#if (ITEM.type==12 && ITEM.created.time!=ITEM.updated.time)>
        | posledn� �prava: ${DATE.show(ITEM.updated, "CZ_SHORT")}
    </#if>
</p>

<#assign text = TOOL.xpath(ITEM, "/data/perex")?default("UNDEF")>
<#if text!="UNDEF">${text}</#if>
${TOOL.xpath(ITEM, "/data/content")}

<#if CHILDREN.poll?exists>
<br />
    <h3>Anketa</h3>
    <div class="anketa">
        <@lib.showPoll CHILDREN.poll[0], story_url />
    </div>
</#if>

<@lib.showRating STORY/>

<p><b>N�stroje</b>: <a href="${story_url}?varianta=print">Tisk</a></p>

<#if (ITEM.type==12)>
    <h3>Koment��e</h3>
    <#if CHILDREN.discussion?exists>
        <@lib.showDiscussion CHILDREN.discussion[0]/>
    <#else>
       <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+STORY.id)}">Vlo�it prvn� koment��</a>
    </#if>
</#if>


<#include "../footer.ftl">
