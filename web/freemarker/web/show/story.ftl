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

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1><a href="/Profile/${owner.id}">${owner.nick?default(owner.name)}</a>
        <#if title!="UNDEF"> - <a href="/blog/${BLOG.subType}">${title}</a></#if>
        </h1>
    </div></div>

    <div class="s_sekce">
        <#if intro!="UNDEF">${intro}</#if>
    </div>

    <#if UNPUBLISHED_STORIES?exists>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <h1>Rozepsané zápisy</h1>
        </div></div>

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

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Aktuální zápisy</h1>
    </div></div>

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

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Pøístup k archivovaným zápisùm za jednotlivé mìsíce.</span></a>
            <h1>Archív</h1>
    </div></div>

    <div class="s_sekce">
        <#list BLOG_XML.data.archive.year as year>
            <ul>
            <#list year.month as month>
                <li>
                    <a href="/blog/${BLOG.subType}/${year.@value}/${month.@value}/"><@lib.month month=month.@value/>${year.@value} (${month})</a>
                </li>
            </#list>
            </ul>
        </#list>
    </div>

    <#assign links = BLOG_XML.data.custom.links.link>
    <#if links?size!=0>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Seznam mých oblíbených stránek, které pravidelnì nav¹tìvuji.</span></a>
            <h1>Oblíbené stránky</h1>
        </div></div>

        <div class="s_sekce">
            <ul>
            <#list links as link>
                <li><a href="${link}" rel="nofollow">${link.@caption}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Pøístup na osobní hlavní stranu a na hlavní stranu v¹ech blogù.</span></a>
        <h1>Navigace</h1>
    </div></div>

    <div class="s_sekce">
        <ul>
            <#if title!="UNDEF">
                <li><a href="/blog/${BLOG.subType}">${title}, hlavní strana</a></li>
            </#if>
            <li><a href="/blog/${BLOG.subType}/souhrn"><#if title!="UNDEF">${title}, </#if>struèný souhrn</a></li>
            <li><a href="/auto/blog/${BLOG.subType}.rss">RSS kanál</a></li>
            <li><a href="/blog">V¹echny blogy</a></li>
	    <li><a href="/blog/souhrn">V¹echny blogy, struèný souhrn</a></li>
        </ul>
    </div>

    <#if (USER?exists && (USER.id==BLOG.owner || USER.hasRole("root"))) || (! USER?exists)>
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Tato sekce sdru¾uje akce pro majitele blogu.</span></a>
            <h1>Nastavení</h1>
        </div></div>
    </#if>

  <div class="s_sekce">
    <ul>
    <#if USER?exists>
        <#if USER.id==BLOG.owner>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=add")}">Vlo¾ nový zápis</a></li>
            <#if !CHILDREN.poll?exists>
                <li><a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+STORY.id)}">Vytvoø anketu</a></li>
            </#if>
        </#if>
        <#if USER.id==BLOG.owner || USER.hasRole("root")>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=edit")}">Uprav zápis</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+STORY.id+"?action=remove")}">Sma¾ zápis</a></li>
        </#if>
        <#if USER.id==BLOG.owner>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=custom")}">Nastavení blogu</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">Pøejmenovat blog</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=categories")}">Upravit kategorie</a></li>
            <li><a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=links")}">Upravit odkazy</a></li>
        </#if>
    <#else>
        <li><a href="${URL.noPrefix("/Profile?action=login&amp;url="+REQUEST_URI)}">Pøihlásit se</a></li>
    </#if>
    </ul>
  </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h2>${TOOL.xpath(ITEM, "/data/name")}</h2>
<p class="cl_inforadek">
    <#if ITEM.type==15>Odlo¾eno<#else>${DATE.show(ITEM.created, "CZ_SHORT")}</#if> |
    Pøeèteno: ${TOOL.getCounterValue(ITEM)}x
    <#if category!="UNDEF">| ${category}</#if>
    <#if (ITEM.type==12 && ITEM.created.time!=ITEM.updated.time)>
        | poslední úprava: ${DATE.show(ITEM.updated, "CZ_SHORT")}
    </#if>
</p>

<#assign text = TOOL.xpath(ITEM, "/data/perex")?default("UNDEF")>
<#if text!="UNDEF">${text}</#if>
${TOOL.xpath(ITEM, "/data/content")}

<#if CHILDREN.poll?exists>
<br />
    <h3>Anketa</h3>
    <div class="s_sekce">
        <@lib.showPoll CHILDREN.poll[0], story_url />
    </div>
</#if>

<#assign rating=TOOL.ratingFor(ITEM.data,"story")?default("UNDEF")>
 <div class="cl_rating">
     <h3>Hodnocení&nbsp;&nbsp;<iframe name="rating" width="300" frameborder="0" height="20" scrolling="no" class="rating"></iframe></h3>
     <div class="hdn">
     <div class="text">Stav: <#if rating!="UNDEF">${rating.result?string["#0.00"]} <#else>bez hodnocení</#if></div>
     <div class="tpm">
        <img src="/images/site2/teplomerrtut.gif" alt="hodnoceni" height="5" width="<#if rating!="UNDEF">${3+(rating.result/3)*191} <#else>3</#if>" title="<#if rating!="UNDEF">${rating.result?string["#0.00"]}</#if>">
        <#if USER?exists>
            <div class="stup">
                <img id="spatny" src="/images/site2/palec_spatny.gif" alt="¹patné">
                <a class="s0" href="${URL.make("/rating/"+STORY.id+"?action=rate&amp;rtype=story&amp;rvalue=0")}" target="rating" title="Va¹e hodnocení: 0">0</a>
                <a class="s1" href="${URL.make("/rating/"+STORY.id+"?action=rate&amp;rtype=story&amp;rvalue=1")}" target="rating" title="Va¹e hodnocení: 1">1</a>
                <a class="s2" href="${URL.make("/rating/"+STORY.id+"?action=rate&amp;rtype=story&amp;rvalue=2")}" target="rating" title="Va¹e hodnocení: 2">2</a>
                <a class="s3" href="${URL.make("/rating/"+STORY.id+"?action=rate&amp;rtype=story&amp;rvalue=3")}" target="rating" title="Va¹e hodnocení: 3">3</a>
                <img id="dobry" src="/images/site2/palec_dobry.gif" alt="dobré">
            </div>
        </#if>
     </div>
     <#if rating!="UNDEF">
        <div class="text">Poèet hlasù: ${rating.count}</div>
     </#if>
     <br /><br /><br />
     </div>
 </div>

<p><b>Nástroje</b>: <a href="${story_url}?varianta=print">Tisk</a></p>

<#if (ITEM.type==12)>
    <h2>Komentáøe</h2>
    <#if CHILDREN.discussion?exists>
        <#assign diz = TOOL.createDiscussionTree(CHILDREN.discussion[0].child,USER?if_exists,CHILDREN.discussion[0].id,true)>
        <#if diz.frozen>Diskuse byla administrátory uzamèena</#if>
        <#if USER?exists && USER.hasRole("discussion admin")>
            <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+diz.relationId+"&amp;dizId="+diz.id)}">
            <#if diz.frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
        </#if>

        <p>
        <#if diz.hasUnreadComments>
            <a href="#${diz.firstUnread}" title="Skoèit na první nepøeètený komentáø">První nepøeètený komentáø</a>
        </#if>

        <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+diz.id+"&amp;threadId=0&amp;rid="+diz.relationId)}">
        Vlo¾it dal¹í komentáø</a>

        <#if diz.monitored>
            <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj">
        </#if>
        <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+CHILDREN.discussion[0].id)}"
        title="AbcMonitor za¹le emailem zprávu, dojde-li v diskusi ke zmìnì">${monitorState}</a>
        <span title="Poèet lidí, kteøí sledují tuto diskusi">(${diz.monitorSize})</span>
        </p>

        <#list diz.threads as thread>
           <@lib.showThread thread, 0, diz, !diz.frozen />
        </#list>
    <#else>
       <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+STORY.id)}">Vlo¾it první komentáø</a>
    </#if>
</#if>


<#include "../footer.ftl">
