<#macro showMessages>
    <#list MESSAGES as msg>
        <p class="message">${msg}</p>
    </#list>
    <#if ERRORS.generic??>
        <p class="error">${ERRORS.generic}</p>
    </#if>
</#macro>

<#-- settings[0] - date format pro diskuse, setting[1] - zda zobrazovat perex, settings[2] - CSS trida pro titulek -->
<#macro showArticle(relation dateFormat settings...)>
    <#local clanek=relation.child,
        autors=TOOL.createAuthorsForArticle(clanek),
        thumbnail=TOOL.xpath(clanek,"/data/thumbnail")!"UNDEF",
        tmp=TOOL.groupByType(clanek.children, "Item"),
        displayWithPerex = settings[1]!true
    >
    <#if tmp.discussion??><#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>
    <#if displayWithPerex && thumbnail!="UNDEF"><div class="cl_thumbnail">${thumbnail}</div></#if>
    <h2 class="${settings[2]!"st_nadpis"}"><a href="${URL.url(relation)}">${clanek.title}</a></h2>
    <#if displayWithPerex><p class="st_perex">${TOOL.xpath(clanek,"/data/perex")}</p></#if>
    <p class="meta-vypis">
        ${DATE.show(clanek.created, dateFormat)} |
        <#if autors?size gt 0>
            <#list autors as autor>
                <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
            </#list>
        <#else>
            <@showUserFromId clanek.owner/>
        </#if>
        <#list TOOL.createRelations(clanek.custom) as rubrika>
             | ${TOOL.childName(rubrika)}
        </#list>
        <#if diz??>| <@showCommentsInListing diz, settings[0]!dateFormat, "/clanky" /></#if>
        <#if USER?? && TOOL.permissionsFor(USER, relation).canModify()>
            | Přečteno: <@showCounter clanek, "read" />&times;
        </#if>
    </p>
</#macro>

<#-- Prints specified counter for given document -->
<#macro showCounter(item, type)><#rt>
    <#local count = -1><#t>
    <#if type == "read"><#t>
        <#local count = (.globals["READ_COUNTERS"].get(item))!(-1)><#t>
    <#elseif type == "visit"><#t>
        <#local count = (.globals["VISIT_COUNTERS"].get(item))!(-1)><#t>
    </#if><#t>
    <#if count != -1>${count}<#else>${TOOL.getCounterValue(item, type)}</#if><#t>
</#macro>

<#macro showCommentsInListing(diz dateFormat urlPrefix)>
    <a href="${diz.url!(urlPrefix+"/show/"+diz.relationId)}">Komentářů:&nbsp;${diz.responseCount}</a><@markNewComments diz/><#rt>
    <#lt><#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, dateFormat)}</#if>
</#macro>

<#macro showSoftwareList(items)>
<table class="sw-polozky">
  <thead>
    <tr>
      <td class="td-nazev">Jméno</td>
      <td class="td-meta">Hodnocení</td>
      <td class="td-meta">Uživatelů</td>
      <td class="td-meta">Navštíveno</td>
      <td class="td-datum">Poslední úprava</td>
    </tr>
  </thead>
  <tbody>
   <#list SORT.byName(ITEMS) as software>
    <tr>
      <td><a href="${software.url}" title="${TOOL.childName(software)}">${TOOL.childName(software)}</a></td>
      <td class="td-meta"><@showShortRating software, "", false /></td>
      <td class="td-meta">${software.child.getProperty("used_by")?size}</td>
      <td class="td-meta"><@showCounter software.child, "visit" />&times;</td>
      <td class="td-datum">${DATE.show(software.child.updated, "CZ_DMY2")}</td>
    </tr>
   </#list>
  </tbody>
</table>
</#macro>

<#macro listTree (objects menuid firstLevel=true)>
<#if (objects?size > 0)>
 <ul<#if firstLevel> id="${menuid}" class="treeview"</#if>>
  <#list objects as sekce>
   <li><a href="${sekce.url}">${sekce.name}</a> <span>(${sekce.size})</span>
   <@listTree sekce.children, menuid, false/>
   </li>
  </#list>
 </ul>
</#if>
</#macro>

<#macro showNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   autor=TOOL.createUser(ITEM.owner),
   diz=TOOL.findComments(ITEM),
   url=relation.url?default("/zpravicky/show/"+relation.id),
   shortened=TOOL.xpath(ITEM,"data/perex")?default("UNDEFINED")
 >
    <h3 class="st_nadpis"><a href="${url}" title="${ITEM.title}">${ITEM.title}</a></h3>
    <#if shortened!="UNDEFINED" && RELATION?? && (RELATION.upper=37672 || RELATION.upper=0)>
        <div style="padding-left: 30pt"><strong>Perex:</strong>${shortened}</div>
    </#if>
    <p>${TOOL.xpath(ITEM,"data/content")}</p>
    <p class="meta-vypis">
        <@showUser autor/> | ${DATE.show(ITEM.created,"CZ_FULL")} |
        <a href="${url}" title="${ITEM.title}">Komentáře: ${diz.responseCount}</a><#rt>
        <#lt><#if diz.responseCount gt 0><@markNewComments diz/>, poslední ${DATE.show(diz.updated, "SMART")}</#if>
    </p>
</#macro>

<#macro showTemplateNews(relation)>
  <#local item=TOOL.sync(relation.child), autor=TOOL.createUser(item.owner), diz=TOOL.findComments(item),
          text=TOOL.xpath(item,"data/content"), shortened=TOOL.xpath(item,"data/perex")!"UNDEFINED",
          url=relation.url!("/zpravicky/show/"+relation.id), showTitle="yes", longTitle="yes">
    <#if USER??>
        <#local showTitle=TOOL.xpath(USER,"/data/settings/news_titles")!"yes",
            longTitle=TOOL.xpath(USER,"/data/settings/news_multiline")!"no">
    </#if>

    <#if showTitle=="yes">
        <div class="st_nadpis<#if longTitle=="yes"> no_overflow</#if>">
            <a href="${url}" title="${item.title?html}">${item.title?html}</a>
        </div>
    </#if>
    <span>${DATE.show(item.created,"CZ_SHORT")} | ${NEWS_CATEGORIES[item.subType].name}</span>
    <#if shortened=="UNDEFINED">
        <div class="zpr_telo">${text}</div>
    <#else>
        <div class="zpr_telo">${shortened}&hellip;&nbsp;<i><a href="${url}">více&nbsp;&raquo;</a></i></div>
    </#if>
    <span><@showUser autor/>
    | <a href="${url}" title="<#if diz.responseCount gt 0>poslední&nbsp;${DATE.show(diz.updated, "SMART")}</#if>"><#rt>
      <#lt>Komentářů:&nbsp;${diz.responseCount}</a><@lib.markNewComments diz/></span>
</#macro>

<#macro showStoryInListing (story, skipUser, shortened)>
    <#if shortened><#local titleTag="h3"><#else><#local titleTag="h2"></#if>
    <div class="cl">
        <${titleTag} class="st_nadpis">
            <a href="${story.url}">${story.title}</a>
        </${titleTag}>
        <p class="meta-vypis">
            ${DATE.show(story.created, "SMART")}
            <#if ! skipUser>
                <a href="${story.blogUrl}">${story.blogTitle}</a> |
                <@showUser story.author/>
            </#if>
            <#if (story.category??)>
                <#if story.category.url??>
                    | <a href="${story.category.absoluteUrl}" title="Kategorie zápisu">${story.category.name}</a>
                <#else>
                    | ${story.category.name}
                </#if>
            </#if>
            <#if story.digest>
                | <img src="/images/site2/digest.png" class="blog_digest" alt="Výběrový blog" title="Kvalitní zápisek vybraný do výběru z blogů">
            </#if>
            <#if (story.polls > 0)>| Anketa </#if>
            <#if (story.videos > 0)>| Video </#if>
            <#if story.perex??>| Přečteno: <@showCounter story.relation.child, "read"/>&times;</#if>
            <#if story.discussion??>| <@showCommentsInListing story.discussion, "SMART_DMY", "/blog" /></#if>
            <@showShortRating story.relation, "| " />
        </p>
        <#if ! shortened>
            <#local showMore=false>
            <#if story.perex??>
                ${TOOL.render(story.perex,USER!)}
                <#local showMore=true>
            <#else>
                <#if (story.polls > 0 || story.images > 0 || story.videos > 0)><#local showMore=true></#if>
                ${story.content}
            </#if>
            <#if showMore>
                <div class="signature"><a href="${story.url}">více...</a></div>
            </#if>
        </#if>
    </div>
</#macro>

<#macro showStoryInTable (story, skipUser)>
    <#local signs="", tooltip="", linkTitle = "", spanTitle = "Počet&nbsp;komentářů", diz = story.discussion>
    <#if (story.polls > 0)><#local signs = TOOL.append(signs, "A"), tooltip = TOOL.append(tooltip, "anketa")></#if>
    <#if (story.images > 0)><#local signs = TOOL.append(signs, "O"), tooltip = TOOL.append(tooltip, "obrázek")></#if>
    <#if (story.videos > 0)><#local signs = TOOL.append(signs, "V"), tooltip = TOOL.append(tooltip, "video")></#if>
    <#if (! skipUser)>
        <#local linkTitle = TOOL.append(linkTitle, story.author.nick!story.author.name?html)>
        <#if story.blogTitle??><#local linkTitle = TOOL.append(linkTitle, story.blogTitle)></#if>
    </#if>
    <#if diz.responseCount gt 0>
        <#local spanTitle = TOOL.append(spanTitle, "poslední&nbsp;" + DATE.show(diz.updated, "CZ_SHORT"))>
    </#if>
    <#if tooltip!=""><#local spanTitle = TOOL.append(spanTitle, tooltip)></#if>


    <a href="${story.url}" title="${linkTitle}">${story.title}</a>
    <span title="${spanTitle}">
        (${diz.responseCount}<@lib.markNewComments diz/><#if signs != "">, ${signs}</#if>)
    </span>
    <#if (story.digest)>
        <img src="/images/site2/digest.png" class="blog_digest" alt="Digest blog" title="Kvalitní zápisek vybraný do digestu">
    </#if>
</#macro>

<#macro markNewComments(discussion)><#t>
<#if TOOL.hasNewComments(USER!, discussion)><#t>
    <span title="V diskusi jsou nové komentáře" class="new_comment_mark">*</span><#t>
</#if><#t>
</#macro>

<#macro markNewCommentsQuestion(discussion)><#t>
<#if TOOL.hasNewComments(USER!, discussion)><#t>
    <span title="V diskusi jsou nové komentáře" class="new_comment_state">&lowast;</span><#t>
</#if><#t>
</#macro>

<#macro separator double=false>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="--------------------"><br>
 <#if double><img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="" vspace="1"><br></#if>
</#macro>

<#macro showThread(comment level diz showControls extra...)>
    <#if comment.author??><#local who=TOOL.createUser(comment.author)></#if>
    <#local blacklisted = diz.isBlacklisted(comment), attachments = comment.attachments, mode = extra[0]!"comment">
    <#if diz.isUnread(comment)><#local css = "ds_hlavicka_novy"><#else><#local css = "ds_hlavicka"></#if>
    <#if blacklisted><#local css = css + " ds_hlavicka_blacklisted"></#if>
    <#if comment.solution && ITEM?? && comment.voters.contains(ITEM.owner)><#local css = css + " ds_author_approved"></#if>
    <#if who?? && USER?? && who.id == USER.id><#local css = css + " ds_hlavicka_me"></#if>

    <div class="${css}" id="${comment.id}">
        <div class="ds_reseni"<#if ! comment.solution> style="display:none"</#if>>
            <#if comment.solution>
                <#if ITEM?? && ITEM.owner != 0>
                    <#assign dizOwner = ITEM.owner>
                <#else>
                    <#assign dizOwner = 0>
                </#if>
                <@showCommentVoters comment.id, comment.voters, dizOwner />
            </#if>
        </div>

        <#if comment.author?? && showControls>
            <#assign avatar = TOOL.getUserAvatar(who!, USER!)!"UNDEFINED">
            <#if avatar != "UNDEFINED">
                <img src="${avatar}" id="comment${comment.id}_avatar" alt="${who.nick!(who.name)} avatar" class="ds_avatar<#if blacklisted> ds_controls_blacklisted</#if>">
            </#if>
        </#if>

        ${DATE.show(comment.created,"SMART")}

        <#if comment.author??>
            <@showUser who/>
            <#local score=who.getIntProperty("score")!(-1)><#if score != -1> | skóre: ${score}</#if>
            <#local blog=TOOL.getUserBlogAnchor(who, "blog")!"UNDEF"><#if blog!="UNDEF"> | blog: ${blog}</#if>
            <#local city=TOOL.xpath(who,"//personal/city")!"UNDEF"><#if city!="UNDEF"> | ${city}</#if>
        <#else>
            <#if !TOOL.xpath(comment.data,"//censored")??>
               ${comment.anonymName!}
            </#if>
        </#if>

        <br>

        <#if mode != "question">
            <span class="<#if blacklisted>ds_control_sbalit<#else>ds_control_sbalit2</#if>" id="comment${comment.id}_toggle2">
                <a onClick="schovej_vlakno(${comment.id})" title="Schová nebo rozbalí celé vlákno">Rozbalit</a>
                <a onClick="rozbal_vse(${comment.id})" title="Schová nebo rozbalí vše pod tímto komentářem">Rozbalit vše</a>
            </span>
        </#if>

        ${comment.title!}

        <#nested>

        <#if showControls>
            <div id="comment${comment.id}_controls"<#if blacklisted> class="ds_controls_blacklisted"</#if>>
                <#local nextUnread = diz.getNextUnread(comment)!"UNDEF">
                <#if ! nextUnread?is_string><a href="#${nextUnread}" title="Skočit na další nepřečtený komentář">Další</a> |</#if>
                <a href="${URL.make("/EditDiscussion/"+diz.relationId+"?action=add&amp;dizId="+diz.id+"&amp;threadId="+comment.id)}">Odpovědět</a>
                <#if mode = "reply">
                    |
                    <#if USER??>
                        <#if comment.hasVoted(USER.id)>
                            <a class="ds_solutionToggle ds_solutionUnset" href="${URL.make("/EditDiscussion/"+diz.relationId+"?action=unsetSolution&amp;threadId="+comment.id+TOOL.ticket(USER!, false))}">Není řešením</a>
                        <#else>
                            <a class="ds_solutionToggle ds_solutionSet" href="${URL.make("/EditDiscussion/"+diz.relationId+"?action=setSolution&amp;threadId="+comment.id+TOOL.ticket(USER!, false))}">Označit jako řešení</a>
                        </#if>
                    </#if>
                </#if>
                <#if mode != "question">
                    | <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle1" title="Schová nebo rozbalí celé vlákno"
                       class="ds_control_sbalit3"><#if ! blacklisted>Sbalit<#else>Rozbalit</#if></a>
                    <#if (comment.parent??)>| <a href="#${comment.parent}" title="Odkaz na komentář o jednu úroveň výše">Výše</a></#if>
                    | <a href="#${comment.id}" title="Přímá adresa na tento komentář">Link</a>
                    <#if comment.author??><#local blockTarget="bUid=" + who.id><#else><#local blockTarget="bName=" + comment.anonymName?url></#if>
                    <#if blacklisted>
                        <#local blockUrl="/EditUser?action=fromBlacklist&amp;"+blockTarget, title="Neblokovat", hint="Odstraní autora ze seznamu blokovaných uživatelů">
                    <#else>
                        <#local blockUrl="/EditUser?action=toBlacklist&amp;"+blockTarget, title="Blokovat", hint="Přidá autora na seznam blokovaných uživatelů">
                    </#if>
                    | <a href="${URL.noPrefix(blockUrl+TOOL.ticket(USER!, false)+"&amp;url="+URL.prefix+"/show/"+diz.relationId+"#"+comment.id)}" title="${hint}">${title}</a>
                </#if>
                | <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}" title="Žádost o přesun diskuse, stížnost na komentář">Admin</a>
            </div>
        <#elseif USER?? && USER.hasRole("discussion admin")>
            <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}">Admin</a>
        </#if>

        <#if (attachments?size > 0)>
            <div class="ds_attachments">
                <span><#if (attachments?size == 1)>Příloha:<#else>Přílohy:</#if></span>
                <ul>
                    <#list attachments as id>
                        <#if diz.attachments(id)??>
                            <li>
                                <#local attachment = diz.attachments(id)>
                                <a href="${TOOL.xpath(attachment, "/data/object/@path")}">${TOOL.xpath(attachment, "/data/object/originalFilename")}</a>
                                (${TOOL.xpath(attachment, "/data/object/size")} bytů)
                            </li>
                        </#if>
                    </#list>
                </ul>
            </div>
        </#if>
    </div>

    <div id="comment${comment.id}" <#if who??>class="ds_text_user${who.id}"</#if><#if blacklisted!> style="display: none;"</#if>>
        <#if TOOL.xpath(comment.data,"//censored")??>
            <@showCensored comment, diz.id, diz.relationId/>
        <#else>
            <div class="ds_text">
                ${TOOL.render(TOOL.element(comment.data,"//text"),USER!)}
            </div>

            <#assign signature = TOOL.getUserSignature(who!, USER!)!"UNDEFINED">
            <#if signature!="UNDEFINED"><div class="signature">${signature}</div></#if>
        </#if>

        <#local level2=level+1>
        <div class="ds_odsazeni">
            <#list comment.children! as child>
                <@showThread child, level2, diz, showControls, extra[0]! />
            </#list>
        </div>
    </div>
</#macro>

<#macro showCensored(comment dizId relId)>
    <div class="cenzura">
        <a href="/faq/abclinuxu.cz/co-znamena-symbol-u-cenzurovanych-prispevku"><img src="/images/site2/skryty-komentar-krizek.png" width="40" height="40" alt="skrytý komentář" style="float:right;"></a>
        <#assign admin = TOOL.xpath(comment.data,"//censored/@admin")!"5473">
        Náš <a href="/Profile/${admin}">administrátor</a> shledal tento komentář
        <a href="/faq/abclinuxu.cz/proc-je-komentar-oznacen-jako-zavadny">závadným</a>.
        <#assign message = TOOL.xpath(comment.data,"//censored")!"">
        <#if message?has_content>
            <p class="cenzura_duvod">${message}</p>
        </#if>
        <a href="${URL.make("/show?action=censored&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Zobrazit komentář</a>
        <#if USER?? && USER.hasRole("discussion admin")>
            <a href="${URL.make("/EditDiscussion?action=censore&amp;rid="+relId+"&amp;dizId="+dizId+"&amp;threadId="+comment.id+TOOL.ticket(USER!, false))}">Odvolat cenzuru</a>
        </#if>
    </div>
</#macro>

<#macro showDiscussion(relation)>
    <#local DIZ = TOOL.createDiscussionTree(relation.child,USER!,relation.id,true)>
    <div class="ds_toolbox">
     <b>Nástroje:</b>
       <#if DIZ.hasUnreadComments>
         <a href="#${DIZ.firstUnread}" title="Skočit na první nepřečtený komentář" rel="nofollow">První nepřečtený komentář</a>,
       </#if>
        <@showMonitor relation "Zašle upozornění na váš email při vložení nového komentáře."/>,
         <a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print" rel="nofollow">Tisk</a>
       <#if USER?? && USER.hasRole("discussion admin")>
         <br />
         <b>Admin:</b>
         <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+DIZ.relationId+"&amp;dizId="+DIZ.id+TOOL.ticket(USER!, false))}">
            <#if DIZ.frozen>Rozmrazit<#else>Zmrazit</#if></a>
       </#if>
    </div>

    <p>
        <#if DIZ.frozen>
            Diskuse byla administrátory uzamčena
        <#else>
            <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DIZ.id+"&amp;threadId=0&amp;rid="+DIZ.relationId)}" rel="nofollow">
                Vložit další komentář
            </a>
        </#if>
    </p>

    <#list DIZ.threads as thread>
       <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
    </#list>

    <#if (!DIZ.frozen && DIZ.size>3)>
        <p>
            <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+DIZ.id+"&amp;rid="+DIZ.relationId)}" rel="nofollow">
            Založit nové vlákno</a> &#8226;
            <a href="#www-abclinuxu-cz">Nahoru</a>
        </p>
    </#if>
</#macro>

<#macro showRating (relation heading=true boxCssClass="rating")>
    <#local rating=TOOL.ratingFor(relation.child.data)!"UNDEF">
    <div class="${boxCssClass}">
      <#if heading><h3>Hodnocení:
        <#if rating!="UNDEF">
          <span title="Hlasů: ${rating.count}">${rating.percent} %</span>
        <#else>-
        </#if></h3>
      </#if>
      <#if rating!="UNDEF">
        <div class="stupnice">
          <#local width = (rating.percent / 100) * 78>
          <div class="rtut" style="width: ${width?string["0"]}px"></div>
        </div>
      <#else>zatím nehodnoceno
      </#if>

        <div class="hlasy">
        <#if USER??>
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0"+TOOL.ticket(USER!, false))}" target="rating" rel="nofollow">špatné</a>
           &bull;
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3"+TOOL.ticket(USER!, false))}" target="rating" rel="nofollow">dobré</a>
        <#else>
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0&amp;return=true"+TOOL.ticket(USER!, false))}" rel="nofollow">špatné</a>
           &bull;
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3&amp;return=true"+TOOL.ticket(USER!, false))}" rel="nofollow">dobré</a>
        </#if>
        </div>
        <iframe name="rating" frameborder="0" height="20" scrolling="no" class="rating_iframe"></iframe>
    </div>
</#macro>

<#macro showShortRating (relation, separator, heading=true)>
    <#local rating=TOOL.ratingFor(relation.child.data)!"UNDEF">
    <#if rating!="UNDEF"><#if heading>${separator}Hodnocení:&nbsp;</#if>${rating.percent}&nbsp;%&nbsp;(${rating.count}&nbsp;hlasů)</#if>
</#macro>

<#macro showMonitor relation help="Zašle upozornění na váš email při úpravě záznamu.">
    <#if TOOL.isMonitored(relation.child, USER!)>
        <a href="${URL.make("/EditMonitor/"+relation.id+"?action=stop"+TOOL.ticket(USER!, false))}">Přestaň sledovat</a>
    <#else>
        <a href="${URL.make("/EditMonitor/"+relation.id+"?action=start"+TOOL.ticket(USER!, false))}">Začni sledovat</a>
    </#if>
    <span title="Počet lidí, kteří sledují tento dokument nebo sekci">(${relation.child.monitorCount})</span>
    <a class="info" href="#">?<span class="tooltip">${help}</span></a>
</#macro>

<#macro star value><#if (value>0.60)><img src="/images/site/star1.gif" alt="*"><#elseif (value<0.2)><img src="/images/site/star0.gif" alt="-"><#else><img src="/images/site/star5.gif" alt="+"></#if></#macro>

<#macro month (month)><#--
 --><#if month=="1">leden<#--
 --><#elseif month=="2">únor<#--
 --><#elseif month=="3">březen<#--
 --><#elseif month=="4">duben<#--
 --><#elseif month=="5">květen<#--
 --><#elseif month=="6">červen<#--
 --><#elseif month=="7">červenec<#--
 --><#elseif month=="8">srpen<#--
 --><#elseif month=="9">září<#--
 --><#elseif month=="10">říjen<#--
 --><#elseif month=="11">listopad<#--
 --><#elseif month=="12">prosinec<#--
 --></#if>
</#macro>

<#macro showPoll (relation url=relation.url!("/ankety/show/"+relation.id))>
    <#assign anketa = relation.child, total = anketa.totalVoters>
    <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>
    <div>
        <form action="${URL.noPrefix("/EditPoll/"+relation.id)}" method="POST">
        <div class="ank-otazka">${anketa.text}</div>
        <#list anketa.choices as choice>
            <div class="ank-odpov">
              <#assign procento = TOOL.percent(choice.count,total)>
              <label><input type="${type}" name="voteId" value="${choice.id}">${choice.text}</label>&nbsp;(<span title="${choice.count} hlasů">${procento}&nbsp;%</span>)<br>
              <div class="ank-sloup-okraj" style="width: ${procento}px">
                <div class="ank-sloup"></div>
              </div>
            </div>
        </#list>
        <input name="submit" type="submit" class="button" value="Hlasuj" src="/images/site2/vote_btn.gif" alt="Hlasuj"<#if relation.id == 0> DISABLED</#if>>
        Celkem ${total} hlasů<br>
        <input type="hidden" name="url" value="${url}">
        <input type="hidden" name="action" value="vote">
        </form>
    </div>
</#macro>

<#macro showRelated (item)>
    <#assign related = TOOL.getRelatedDocuments(item)>
    <#if (related?size > 0)>
        <div class="cl_perex souvisejici">
            <h3>Související dokumenty</h3>
            <dl>
            <#list related as link>
                <dt>
                    <a href="${link.url}">${link.title}</a>
                    <#if link.type=='article'>(článek)
                    <#elseif link.type=='content'>(dokument)
                    <#elseif link.type=='dictionary'>(pojem)
                    <#elseif link.type=='discussion'>(diskuse)
                    <#elseif link.type=='driver'>(ovladač)
                    <#elseif link.type=='external'>(externí dokument)
                    <#elseif link.type=='faq'>(FAQ)
                    <#elseif link.type=='hardware'>(hardware)
                    <#elseif link.type=='news'>(zprávička)
                    <#elseif link.type=='other'>(ostatní)
                    <#elseif link.type=='poll'>(anketa)
                    <#elseif link.type=='section'>(sekce)
                    <#elseif link.type=='software'>(software)
                    <#elseif link.type=='story'>(blog)
                    <#elseif link.type=='personality'>(osobnost)
                    </#if>
                </dt>
                <#if link.description??>
                    <dd>${link.description}</dd>
                </#if>
            </#list>
            </dl>
        </div>
    </#if>
</#macro>

<#macro showOption (param value caption type extra...)>
    <label>
        <input type="${type}" name="${param}" value="${value}"<#if TOOL.isWithin(PARAMS[param], value)> checked="checked"</#if>${" "+extra[0]!} />
        ${caption}
    </label>
</#macro>

<#macro showOption2 (param value caption type values)>
    <label>
        <input type="${type}" name="${param}" value="${value}"<#if TOOL.isWithin(values, value)> checked="checked"</#if> /> ${caption}
    </label>
</#macro>

<#macro showOption3 (param value caption type condition)>
    <label>
        <input type="${type}" name="${param}" value="${value}"<#if condition> checked="checked"</#if> /> ${caption}
    </label>
</#macro>

<#macro showOption4 (value caption values)>
    <label>
        <option value="${value}"<#if TOOL.isWithin(values, value)> selected</#if>>${caption}</option>
    </label>
</#macro>

<#macro showOption5 (value caption condition)>
    <label>
        <option value="${value}"<#if condition> selected</#if>>${caption}</option>
    </label>
</#macro>

<#macro showOption6 (param value caption type condition extra...)>
    <label>
        <input type="${type}" name="${param}" value="${value}" <#if condition> checked="checked"</#if> <#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list> />
        ${caption}
    </label>
</#macro>

<#macro filterOption (filter name value extra...) >
	<option value="${value}" <#if filter.checked("${name}", "${value}")> selected</#if> <#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list> >
	<#nested/>
	</option>
</#macro>

<#macro filterInput (filter name extra...) >
	<input type="text" name="${name}" value="${filter.value(name)}" <#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list> />
</#macro>

<#macro filterHidden (filter name extra...) >
	<input type="hidden" name="${name}" value="${filter.value(name)}" <#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list> />
</#macro>

<#macro filterRadio (filter name value extra...) >
    <label>
    	<input type="radio" name="${name}" value="${value}" <#if filter.checked("${name}", "${value}")> checked="checked"</#if> <#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list> />
        <#nested/>
    </label>
</#macro>

<#macro filterCheckBox (filter name value extra...) >
    <label>
        <input type="checkbox" name="${name}" value="${value}" <#if filter.checked("${name}", "${value}")> checked="checked"</#if> <#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list> />
        <#nested/>
    </label>
</#macro>

<#macro filterText (filter name extra...) >
	<textarea name="${name}" <#list extra?keys as attr> ${attr}="${extra[attr]?html}"</#list> >${filter.value("${name}")}</textarea>
</#macro>

<#macro advertisement (id)>${TOOL.getAdvertisement(id, .vars)}</#macro>

<#macro showDiscussionState diz>
    <@markNewCommentsQuestion diz/>
    <#if TOOL.xpath(diz.discussion,"/data/frozen")??>
        <img src="/images/site2/zamceno.gif" alt="Z" title="Diskuse byla administrátory uzamčena">
    </#if>
    <#if TOOL.isQuestionSolved(diz.discussion)>
        <img src="/images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle čtenářů vyřešena">
    </#if>
    <#if TOOL.isMonitored(diz.discussion, USER!)>
        <img src="/images/site2/sledovano.gif" alt="S" title="Tuto diskusi sledujete monitorem">
    </#if>
</#macro>

<#macro showUser user><#if (user.id > 0)><a href="/lide/${user.login}">${user.nick!(user.name)}</a><#else>${user.name}</#if></#macro>

<#macro showUserFromId uid><#rt>
    <#local user = TOOL.createUser(uid)><#t>
    <@showUser user /><#t>
</#macro>

<#macro showAuthor author><a href="${URL.make("/sprava/redakce/autori/show/" + author.relationId)}">${author.title}</a></#macro>

<#macro showRevisions relation info = TOOL.getRevisionInfo(relation.child)>
    <p class="documentHistory">
        Dokument vytvořil: <@showUser info.creator/>, ${DATE.show(relation.child.created,"SMART")}
        <#if (info.lastRevision > 1)>
            | Poslední úprava: <@showUser info.lastCommiter/>, ${DATE.show(relation.child.updated,"SMART")}
            <#if (info.committers?size > 0)>
                | Další přispěvatelé:
                <#list info.committers as committer><#rt>
                    <@showUser committer/><#rt>
                    <#lt><#if committer_has_next>,</#if>
                </#list>
            </#if>
            | <a href="/revize?rid=${relation.id}&amp;prefix=${URL.prefix}" rel="nofollow">Historie změn</a>
        </#if>
        | Zobrazeno: ${TOOL.getCounterValue(relation.child,"read")}&times;</p>
    </p>
</#macro>

<#macro showHelp>
    <a class="info" href="#">?<span class="tooltip"><#nested></span></a>
</#macro>

<#macro showError key>
    <#if ERRORS[key]??><div class="error" id="${key}Error">${ERRORS[key]}</div></#if>
</#macro>

<#macro initRTE>
    <#if (RTE.instances?size > 0)>
        <script language="javascript1.2" type="text/javascript">
            <#list RTE.instances as instance>
                <#if instance.commentedContent??>quotedText = "<blockquote>${instance.commentedContent?js_string}</blockquote>";</#if>
            </#list>
        </script>
        <#if RTE.mode != "never">
            <script language="javascript" type="text/javascript" src="/data/tiny_mce/tiny_mce.js"></script>
            <script language="javascript" type="text/javascript">
                initializeEditor('${RTE.menu}');
                $().ready(function() {
                    <#list RTE.instances as instance>
                        <#if instance.mode == "always">toggleEditor('${instance.id}')</#if>
                    </#list>
                })
            </script>
        </#if>
    </#if>
</#macro>

<#macro addRTE textAreaId formId menu commentedText="UNDEFINED">
    <#if commentedText=="UNDEFINED">
        ${TOOL.addRichTextEditor(RTE, textAreaId, formId, menu)}
    <#else>
        ${TOOL.addRichTextEditor(RTE, textAreaId, formId, menu, commentedText)}
    </#if>
</#macro>

<#macro showRTEControls textAreaId>
    <#if RTE.displayControls>
        <#local editor = RTE[textAreaId]>
        <div class="form-edit">
            <#if editor.mode != "never">
                <a href="javascript:toggleEditor('${editor.id}');" title="Přepne WYSIWYG editor pro pohodlné zadávání formátovaného textu">Editor</a>
                <input type="hidden" name="rte_${editor.id}" id="rte_${editor.id}" value="request}">
            </#if>
            <span id="jsEditorButtons">
                <#if RTE.menu = "news">
                    <@jsInsert editor, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;', "mono", "Vložit značku odkazu", "&lt;a&gt;"/>
                <#else>
                    <@jsInsert editor, '&lt;b&gt;', '&lt;/b&gt;', "serif", "Vložit značku tučně", "<b>B</b>"/>
                    <@jsInsert editor, '&lt;i&gt;', '&lt;/i&gt;', "serif", "Vložit značku kurzíva", "<i>I</i>"/>
                    <@jsInsert editor, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;', "mono", "Vložit značku odkazu", "&lt;a&gt;"/>
                    <@jsInsert editor, '&lt;blockquote&gt;', '&lt;/blockquote&gt;', "mono", "Vložit značku citace", "BQ"/>
                    <@jsInsert editor, '&lt;p&gt;', '&lt;/p&gt;', "mono", "Vložit značku odstavce", "&lt;p&gt;"/>
                    <@jsInsert editor, '&lt;pre&gt;', '&lt;/pre&gt;', "mono", "Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.", "&lt;pre&gt;"/>
                    <@jsInsert editor, '&lt;code&gt;', '&lt;/code&gt;', "mono", "Vložit značku pro písmo s pevnou šířkou", "&lt;code&gt;"/>
                    <@jsInsert editor, '&lt;ul&gt;\n&lt;li&gt;', '&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;/ul&gt;', "mono", "Vložit nečíslovaný seznam", "&lt;ul&gt;"/>
                    <@jsInsert editor, '&lt;ol&gt;\n&lt;li&gt;', '&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;li&gt;&lt;/li&gt;\n&lt;/ol&gt;', "mono", "Vložit číslovaný seznam", "&lt;ol&gt;"/>
                    <#if RTE.menu = "blog">
                        <@jsInsert editor, '&lt;!--break--&gt;', '', "mono", "Vložit značku zlomu", "break"/>
                    </#if>
                    <@jsInsert editor, '&amp;lt;', '', "mono", "Vložit písmeno &lt;", "&lt;"/>
                    <@jsInsert editor, '&amp;gt;', '', "mono", "Vložit písmeno &gt;", "&gt;"/>
                    <#if editor.commentedContent??>
                        <script language="javascript1.2" type="text/javascript">
                            function cituj(input) {
                                input.value += quotedText;
                            }
                        </script>
                        <a href="javascript:cituj(document.${editor.form}.${editor.id});" class="mono" title="Vloží komentovaný příspěvek jako citaci">Citace</a>
                    </#if>
                </#if>
            </span>
        </div>
    </#if>
</#macro>

<#macro jsInsert editor prefix suffix class hint title>
    <a href="javascript:insertAtCursor(document.${editor.form}.${editor.id}, '${prefix}', '${suffix}');" class="${class}" title="${hint}">${title}</a>
</#macro>

<#macro showTagCloud list title cssStyle>
    <div id="tagcloud_container"<#if (cssStyle?length gte 1)> style="${cssStyle}"</#if>>
    <#if title?? ><div id="title">${title}</div></#if>
	<ul id="tagcloud">
		<#list list as tag>
			<li class="${tag.cssClass}">
			    <a href="/stitky/${tag.id}" title="Štítek ${tag.title}: ${tag.usage} použití">${tag.title}</a>
			</li>
		</#list>
	</ul>
    </div>
</#macro>

<#macro showSignPost title cssStyle="">
<div class="ui-dialog ui-widget ui-widget-content ui-corner-all sign-post"<#if (cssStyle?length gte 1)> style="${cssStyle}"</#if>>
  <div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix" unselectable="on">
    <span class="ui-dialog-title" unselectable="on">${title}</span>
  </div>
  <div class="sign-post-content">
  <#nested/>
  </div>
</div>
</#macro>

<#macro repeat times>
    <#if times lt 1><#return></#if>
    <#list 1..times as temp>
        <#nested/>
    </#list>
</#macro>

<#macro showForum rid numQuestions onHP showAdvertisement showAJAXControls singleMode>
    <#if rid!=0>
        <#local forum = VARS.getFreshQuestions(numQuestions, rid),
                feed = FEEDS.getForumFeedUrl(rid)!"UNDEF">
    <#else>
        <#local forum = VARS.getFreshQuestions(USER!),
                feed = FEEDS.getForumFeedUrl()!"UNDEF">
    </#if>
    <#local FORUM=TOOL.analyzeDiscussions(forum)>

      <table class="ds" id="forum_table_${rid}">
       <#if USER??><form method="post" action="/EditUser/${USER.id}"></#if>
        <thead>
          <tr>
            <td class="td-nazev">
              <span class="meta-odkazy">
                 <#if rid gt 0>
                     <a href="/forum/EditDiscussion?action=addQuez&amp;rid=${rid}">Položit dotaz</a>,
                     <a href="/forum/dir/${rid}?from=${FORUM?size}&amp;count=20">Starší dotazy</a>
                 <#elseif rid==0>
                     <a href="/History?type=discussions&amp;from=${FORUM?size}&amp;count=20">Starší dotazy</a>
                 </#if>
              </span>
              <#if rid gt 0>
                  <#local relation=TOOL.createRelation(rid)>
                  <span class="st_nadpis"><a href="${relation.url}" title="${TOOL.childName(relation)}">${TOOL.childName(relation)}</a></span>
              <#elseif rid==0>
                  <span class="st_nadpis"><a href="/poradna" title="Poradna">Poradna</a></span>
              <#elseif rid==-1>
                  <span class="st_nadpis"><a href="/skupiny" title="Poradny ze skupin">Poradny ze skupin</a></span>
              </#if>
            </td>
            <td class="td-meta">Stav</td>
            <td class="td-meta">Reakcí</td>
            <td class="td-datum">
                Poslední
                <#if feed!="UNDEF">
                   &nbsp;<a href="${feed}"><img src="/images/site2/feed12.png" width="12" height="12" alt="<#if rid!=0>${TOOL.childName(relation)}, </#if>RSS feed"></a>
                </#if>
                <#if USER?? && rid!=0 && !singleMode>
                    <#if !onHP>
                        <#local uforums=TOOL.getUserForums(USER)>
                        <#list uforums.keySet() as key><#if key==rid><#local onHP=true></#if></#list>
                    </#if>

                    <#if onHP>
                        <input type="image" title="Odlepit z úvodní stránky" src="/images/actions/remove.png" style="background-color:transparent">
                    <#else>
                        <input type="image" title="Přilepit na úvodní stránku" src="/images/actions/add.png" style="background-color:transparent">
                    </#if>
                    <input type="hidden" name="action" value="toggleForumHP">
                    <input type="hidden" name="rid" value="${rid}">
                    <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER)}">
                </#if>
            </td>
          </tr>
        </thead>
       <#if USER??></form></#if>
        <#if showAJAXControls>
            <tfoot id="forum_tfoot_${rid}">
              <tr><td>
                <script type="text/javascript">
                  //<![CDATA[
                  new Forum(${rid}, ${FORUM?size}, ${VARS.maxSizes.question});
                  //]]>
                </script>
              </td></tr>
            </tfoot>
        </#if>
        <tbody id="forum_tbody_${rid}">
         <#list FORUM as diz>
          <tr>
            <td><a href="${diz.url}">${TOOL.limit(diz.title,60,"...")}</a></td>
            <td class="td-meta"><@showDiscussionState diz /></td>
            <td class="td-meta">${diz.responseCount}</td>
            <td class="td-datum">${DATE.show(diz.updated,"SMART")}</td>
          </tr>
         </#list>
        </tbody>
      </table>

      <#--<#if showAdvertisement>
          <div style="margin:0.5em 0 0 0; float:right">
             <@lib.advertisement id="gg-hp-blogy" />
          </div>
      </#if>

      <#if showAdvertisement><div style="clear: right"></div></#if>-->

</#macro>

<#macro showRegion region><#--
 --><#if region=="praha">Praha<#--
 --><#elseif region=="jihocesky">Jihočeský<#--
 --><#elseif region=="jihomoravsky">Jihomoravský<#--
 --><#elseif region=="karlovarsky">Karlovarský<#--
 --><#elseif region=="kralovehradecky">Královehradecký<#--
 --><#elseif region=="liberecky">Liberecký<#--
 --><#elseif region=="moravskoslezsky">Moravskoslezský<#--
 --><#elseif region=="olomoucky">Olomoucký<#--
 --><#elseif region=="pardubicky">Pardubický<#--
 --><#elseif region=="plzensky">Plzeňský<#--
 --><#elseif region=="stredocesky">Středočeský<#--
 --><#elseif region=="ustecky">Ústecký<#--
 --><#elseif region=="vysocina">Vysočina<#--
 --><#elseif region=="zlinsky">Zlínský<#--
 --><#elseif region=="banskobystricky">Banskobystrický<#--
 --><#elseif region=="bratislavsky">Bratislavský<#--
 --><#elseif region=="kosicky">Košický<#--
 --><#elseif region=="nitransky">Nitranský<#--
 --><#elseif region=="presovsky">Prešovský<#--
 --><#elseif region=="trencinsky">Trenčínský<#--
 --><#elseif region=="trnavsky">Trnavský<#--
 --><#elseif region=="zilinsky">Žilinský<#--
 --></#if>
</#macro>

<#macro showEvent relation showLogo showManagement>
    <#local item=relation.child, subtype=item.subType,
            region=item.string1!"UNDEF",
            regs=TOOL.xpathValue(item.data, "count(//registrations/registration)")>

    <#if subtype=="community"><#local subtype="Komunitní">
    <#elseif subtype=="educational"><#local subtype="Vzdělávací">
    <#elseif subtype=="company"><#local subtype="Firemní">
    </#if>

    <#assign tmp=TOOL.groupByType(item.children, "Item")>
    <#if tmp.discussion??><#assign diz=TOOL.analyzeDiscussion(tmp.discussion[0])><#else><#assign diz=null></#if>

    <table class="events">
    <tr>
        <td class="event_hdr">
            <b>${DATE.show(item.created,"CZ_DMY",false)}</b><br />
            <em>Druh:</em>&nbsp;${subtype}<br />
            <em>Kraj:</em>&nbsp;<@lib.showRegion region/><br />
            <em>Začátek:</em>&nbsp;${DATE.show(item.created,"TIME")}
        </td>
        <td>
            <#if showLogo>
                <#assign logo=TOOL.xpath(item, "/data/icon")!"NOLOGO">
                <#if logo!="NOLOGO">
                    <div class="cl_thumbnail"><img src="${logo}" alt="Logo akce ${TOOL.childName(item)}"></div>
                </#if>
            </#if>
            <h2 class="st_nadpis"><a href="${relation.url!("/akce/show/"+relation.id)}">${TOOL.childName(item)}</a></h2>
            <p>${TOOL.xpath(item, "/data/descriptionShort")}</p>

            <p class="meta-vypis">Aktualizováno: ${DATE.show(item.updated,"SMART")}
                | správce:&nbsp;<@showUserFromId item.owner />
                <#if diz??>| <@lib.showCommentsInListing diz, "CZ_SHORT", "/akce" /></#if>
                | Přečteno:&nbsp;<@showCounter item, "read"/>&times; |
                <a href="${relation.url!("/akce/"+relation.id)}?action=participants">Účastníků:&nbsp;${regs?eval}</a>
            </p>
            <#if showManagement>
                <div>
                    <a href="${URL.noPrefix("/akce/edit/"+relation.id+"?action=approve"+TOOL.ticket(USER,false))}">Schválit</a>
                    |
                    <a href="${URL.noPrefix("/EditRelation/"+relation.id+"?action=remove&amp;prefix=/akce")}">Smazat</a>
                </div>
            </#if>
        </td>
    </tr>
    </table>
</#macro>

<#macro showSubportal relation showDesc>
    <#local item=relation.child, icon=TOOL.xpath(item,"/data/icon")!"UNDEF",
        counter=VARS.getSubportalCounter(relation), members=item.getProperty("member"),
        score=item.getIntProperty("score")?default(-1)>

    <div class="s_nadpis"><a href="${relation.url}">${item.title}</a></div>
    <div class="s_sekce" style="text-align:center">
        <#if icon!="UNDEF">
            <div class="s_logo">
                <a href="${relation.url}"><img src="${icon}" alt="${item.title}"></a>
            </div>
        </#if>
        <#if showDesc>
            <h2 class="st_nadpis"><a href="${relation.url}">${item.title}</a></h2>
            ${TOOL.render(TOOL.xpath(item,"/data/descriptionShort"), USER!)}
        </#if>
    </div>

    <div class="s_nadpis">Informace o skupině</div>
    <div class="s_sekce">
      <table cellspacing="0" class="s_table skupiny">
        <tr><td>Založena:</td>     <td>${DATE.show(item.created,"CZ_DMY")}</td></tr>
        <tr><td>Členů:</td>        <td><a href="${relation.url}?action=members">${members?size}</a></td></tr>
        <tr><td>Článků:</td>       <td><a href="${relation.url}/clanky">${counter.ARTICLES?default("?")}</a></td></tr>
        <tr><td>Wiki stránek:</td> <td><a href="${relation.url}/wiki">${counter.WIKIS?default("?")}</a></td></tr>
        <tr><td>Dotazů:</td>       <td><a href="${relation.url}/poradna">${counter.QUESTIONS?default("?")}</a></td></tr>
        <tr><td>Akcí:</td>         <td><a href="${relation.url}/akce">${counter.EVENTS?default("?")}</a></td></tr>
        <tr><td>Čtenost:</td>      <td>${counter.READPCT?default("?")}&nbsp;%</td></tr>
        <#if score != -1><tr><td>Skóre:</td> <td>${score}</td></tr></#if>
      </table>
        <form action="/skupiny/edit/${relation.id}" method="post">
            <#if USER?? && members.contains(""+USER.id)>
             <input type="submit" value="Odregistrovat se">
            <#else>
             <input type="submit" value="Registrovat se">
            </#if>
            <input type="hidden" name="action" value="toggleMember">
            <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER!)}">
        </form>
    </div>
</#macro>

<#macro showVideoPlayer relation width height showLink>
    <#local item=relation.child, code=TOOL.xpath(item,"//code"), desc=TOOL.xpath(item,"//description")!"">
    <#if item.subType=="youtube">
        <#local player="http://www.youtube.com/v/"+code+"&amp;hl=en&amp;fs=1">
    <#elseif item.subType=="stream">
        <#local player="http://www.stream.cz/object/" + code>
    <#elseif item.subType=="googlevideo">
        <#local player="http://video.google.com/googleplayer.swf?docid="+code+"&amp;hl=cs&amp;fs=true">
    </#if>

    <#if showLink>(<a href="${relation.url!("/videa/show/"+relation.id)}">správa videa</a>)</#if><br>
    <object width="${width}" height="${height}">
        <param name="movie" value="${player}"></param><param name="allowFullScreen" value="true"></param>
        <embed src="${player}" type="application/x-shockwave-flash" allowfullscreen="true" width="${width}" height="${height}"></embed>
    </object>
    <#if desc!=""><p>${desc}</p></#if>
</#macro>

<#macro showVideo relation>
    <#local item = relation.child, tmp = TOOL.groupByType(item.children, "Item"),
    icon = TOOL.xpath(item,"/data/thumbnail")!"UNDEF", title = "${TOOL.childName(relation)}">
    <#if tmp.discussion??><#local diz = TOOL.analyzeDiscussion(tmp.discussion[0])><#else><#local diz = null></#if>
    <div class="video">
        <p class="st_nadpis">
            <a href="${relation.url}" title="${title}">${TOOL.limit(title, 45, "..")}</a>
        </p>
        <a href="${relation.url}" class="thumb">
            <img src="${icon}" alt="${title}" border="0">
        </a>
        <p class="meta-vypis" style="text-align: left">
            ${DATE.show(item.created, "SMART")} | <@showUserFromId item.owner/><br>
            Zhlédnuto: <@showCounter item, "read"/>&times;
            <#if diz??>| <@lib.showCommentsInListing diz, "CZ_SHORT", "/videa" /></#if>
        </p>
    </div>
</#macro>

<#macro showDesktop desktop>
    <#local item = desktop.item, tmp = TOOL.groupByType(item.children, "Item")>
    <#if tmp.discussion??><#assign diz = TOOL.analyzeDiscussion(tmp.discussion[0])><#else><#local diz = null></#if>
    <div class="desktop">
        <p title="${desktop.title}">${TOOL.limit(desktop.title, 28, "..")}</p>
        <a href="${desktop.url}" class="thumb">
            <img width="200" src="${desktop.thumbnailListingUrl}" alt="${desktop.title}">
        </a>
        <p class="meta-vypis" style="text-align: left">
            ${DATE.show(item.created, "SMART")} | <@showUserFromId item.owner/><br>
            Zhlédnuto: <@showCounter item, "read"/>&times;
            <#if diz??>| <@lib.showCommentsInListing diz, "CZ_SHORT", "/videa" /></#if>
        </p>
    </div>
</#macro>

<#macro showTopDesktop relation>
    <#assign topDesktop = TOOL.createScreenshot(relation)>
    <p>${topDesktop.title}<br />
        <a href="${topDesktop.url}" title="${topDesktop.title}" class="thumb">
            <img src="${topDesktop.thumbnailListingUrl}" alt="${topDesktop.title}">
        </a></p>
</#macro>

<#macro showGallery item title="Obrázky">
    <#assign images = TOOL.screenshotsFor(item)>
    <#if (images?size > 0)>
        <h3>${title}</h3>

        <p class="galerie">
            <#list images as image>
                <#assign alt = item.title + ", obrázek " + (image_index + 1)>
                <#if image.thumbnailPath??>
                    <a href="${image.path}"><img src="${image.thumbnailPath}" alt="${alt}" border="0"></a>
                <#else>
                    <img src="${image.path}" alt="${alt}">
                </#if>
            </#list>
        </p>
    </#if>
</#macro>

<#macro showNewsFromFeed feedUrl feedLinks>
    <h3>
        Aktuality
        <a href="${feedUrl}"><img src="/images/site2/feed16.png" width="16" height="16" border="0" alt="URL feedu"></a>
    </h3>
    <#if feedLinks?? && !(feedLinks?is_string && feedLinks == "UNDEFINED")>
        <ul>
        <#list feedLinks as link>
            <li>
                <#if link.child.url??>
                    <a href="${"/presmeruj?class=P&amp;id="+ITEM.id+"&amp;url="+link.child.url?url}">${link.child.text}</a>
                <#else>
                    ${link.child.text}
                </#if>
                (${DATE.show(link.child.updated,"CZ_FULL")})
            </li>
        </#list>
        </ul>
    <#else>
        <p>Zdroj zatím nebyl načten, je prázdný nebo obsahuje chybu.</p>
    </#if>
</#macro>

<#macro showPageTools relation>
    <p class="page_tools">
        <a href="${URL.url(relation)}?varianta=print" rel="nofollow" class="bez-slovniku">Tiskni</a>
        <#if TOOL.displaySocialBookmarks(USER!)>
            <span id="bookmarks">
                Sdílej:
                <a href="/sdilej?rid=${relation.id}&amp;s=link"><img src="/images/link/linkuj.gif" width="16" height="16" alt="Linkuj" title="Linkuj"/></a>
                <a href="/sdilej?rid=${relation.id}&amp;s=jag"><img src="/images/link/jagg.png" width="16" height="16" alt="Jaggni to" title="Jaggni to"/></a>
                <a href="/sdilej?rid=${relation.id}&amp;s=sme"><img src="/images/link/vybrali_sme.gif" width="15" height="15" alt="Vybrali.sme.sk" title="Vybrali.sme.sk"/></a>
                <a href="/sdilej?rid=${relation.id}&amp;s=google"><img src="/images/link/google.gif" width="16" height="16" alt="Google" title="Google"/></a>
                <a href="/sdilej?rid=${relation.id}&amp;s=del"><img src="/images/link/delicio.gif" width="16" height="16" alt="Del.icio.us" title="Del.icio.us"/></a>
                <a href="/sdilej?rid=${relation.id}&amp;s=fb"><img src="/images/link/facebook.gif" width="16" height="16" alt="Facebook" title="Facebook"/></a>
            </span>
        </#if>
    </p>
</#macro>

<#macro showAdminTools relation frozen>
    <#if USER?? && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
        <br />
        <b>Admin:</b>
        <a href="/SelectRelation?prefix=/forum&amp;url=/EditRelation&amp;action=move&amp;rid=${relation.id}">Přesunout</a>
        <#if USER.hasRole("discussion admin")>
            <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+relation.id+"&amp;prefix="+URL.prefix)}">Smazat</a>
            <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+relation.id+"&amp;dizId="+relation.child.id+TOOL.ticket(USER, false))}">
                <#if frozen>Rozmrazit<#else>Zmrazit</#if>
            </a>
        </#if>
    </#if>
</#macro>

<#macro showCommentVoters (threadId, voters, xauthor, shorten = true)>
    <span>Řešení ${voters?size}&times;</span> (<#--
 --><#list voters as voter>
        <#if shorten && (voter_index >= 3) && voters?size gt 4>
            <#if voters?size lt 8>
                <#assign dalsi_plural="další">
            <#else>
                <#assign dalsi_plural="dalších">
            </#if>
            a <a id="showMore-${threadId}" href="javascript:showCommentVoters(${threadId})">${voters?size - 3} ${dalsi_plural}</a><#--
         --><#break>
        <#else>
            <@lib.showUserFromId voter /><#--
         --><#if xauthor==voter> (tazatel)</#if><#--
         --><#if ! shorten && voter_has_next>, 
            <#elseif voter_has_next && ((voter_index = 2) && (voters?size > 4))> 
            <#elseif voter_has_next>, </#if><#--
     --></#if><#--
 --></#list><#--
 -->)
</#macro>


<#macro addFormField required = false, description = "&nbsp;", tooltip = "">
    <tr>
        <td <#if required>class="required"</#if>>${description}
            <#if tooltip != "">
                <@showHelp>${tooltip?html}</@showHelp>
            </#if>
        </td>
        <td>
            <#nested>
        </td>
    </tr>
</#macro>

<#macro addInput required, name, description, size = 24, extraAttributes = "", defaultValue = "">
    <@addFormField required, description>
        <@addInputBare name, size, extraAttributes, defaultValue>
            <#nested>
        </@addInputBare>
    </@addFormField>
</#macro>

<#macro addInputBare name, size = 24, extraAttributes = "", defaultValue = "">
    <input type="text" id="${name?html}" size="${size}" name="${name?html}" value="${(PARAMS.get(name)!defaultValue)?html}" ${extraAttributes}>
    <#nested>
    <@showError name />
</#macro>

<#macro addPassword required, name, description, size = 24, extraAttributes = "">
    <@addFormField required, description>
        <@addPasswordBare name, description, size, extraAttributes>
            <#nested>
        </@addPasswordBare>
    </@addFormField>
</#macro>

<#macro addPasswordBare name, description, size = 24, extraAttributes = "">
    <input type="password" id="${name?html}" size="${size}" name="${name?html}" ${extraAttributes}>
    <#nested>
    <@showError name />
</#macro>

<#macro addTextArea required, name, description, rows = 15, extraAttributes = "", value=PARAMS[name]!>
    <@addFormField required, description>
        <#nested>
        <@addTextAreaBare name, rows, extraAttributes, value />
    </@addFormField>
</#macro>

<#macro addTextAreaBare name, rows = 15, extraAttributes = "", value=PARAMS[name]!>
    <@showError name />
    <#nested>
    <textarea name="${name?html}" id="${name?html}" rows="${rows}" class="siroka" ${extraAttributes}>${value?html}</textarea>
</#macro>

<#-- todo prechod na  <@showRTEControls name/> -->
<#macro addTextAreaEditor name>
    <div class="form-edit">
            <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
                    <a href="javascript:insertAtCursor(document.getElementById('${name?html}'), '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
                    <#nested>
                </div>
        </div>
</#macro>

<#macro addSubmit description, name = "">
    <@addFormField>
        <@addSubmitBare description, name />
    </@addFormField>
</#macro>

<#macro addSubmitBare description, name = "">
    <input type="submit" <#if name!="">name="${name?html}"</#if> value="${description?html}" />
</#macro>

<#macro addHidden name, value>
    <input type="hidden" name="${name?html}" value="${value?html}" />
</#macro>

<#macro addCheckbox name, description, value="yes">
    <@addFormField false, "&nbsp;">
        <@addCheckboxBare name, description, value><#nested></@addCheckboxBare>
    </@addFormField>
</#macro>

<#macro addCheckboxBare name, description, value="yes">
    <#nested>
    <#local val=PARAMS.get(name)!"no">
    <label><input type="checkbox" name="${name?html}" value="${value?html}" <#if (val=="yes" || val == "true" || val == "1")>checked="checked"</#if> />
    ${description?html}</label>
</#macro>

<#macro addForm action, extraAttributes = "", multipart = false, method = "post">
    <form action="${action?html}" method="${method}" <#if multipart>enctype="multipart/form-data"</#if> ${extraAttributes}>
        <@addFakeForm>
            <#nested>
        </@addFakeForm>
    </form>
</#macro>

<#macro addFakeForm>
    <table cellpadding="5" border="0" class="siroka">
        <#nested>
    </table>
</#macro>

<#macro addSelect required, name, description, multipleChoice = false, size = 6>
    <@addFormField required, description>
        <select name="${name}" <#if multipleChoice>multiple="multiple"</#if> size="${size}">
            <#nested>
        </select>
    </@addFormField>
</#macro>

<#macro addSelectBare name, multipleChoice = false>
    <select name="${name}" <#if multipleChoice>multiple="multiple"</#if>>
        <#nested>
    </select>
    <@showError name />
</#macro>


<#macro addOption selectName, optionName, value=optionName, isDefault = false>
    <option value="${value}" <#if ((PARAMS.get(selectName)!"") == value) || (isDefault && !PARAMS.containsKey(selectName))>selected="selected"</#if> >${optionName}</option>
</#macro>

<#macro addFile required, name, description>
    <@addFormField required, description>
        <@addFileBare name>
            <#nested>
        </@addFileBare>
    </@addFormField>
</#macro>

<#macro addFileBare name>
    <input type="file" name="${name?html}" size="20">
    <#nested>
    <@showError name />
</#macro>

<#macro addDescriptionLine>
    <tr><td colspan="2"><#nested></td></tr>
</#macro>

<#macro addGroup description>
    <tr><td colspan="2">
        <h2 style="margin-bottom: 1em">${description?html}</h2>
    </td></tr>
    <#nested>
</#macro>

<#macro addRadioChoice name, value, description, isDefault = false>
    <label><input type="radio" name="${name?html}" value="${value}" <#if (PARAMS.get(selectName)! == value) || (isDefault && !PARAMS.containsKey(selectName))>checked="checked"</#if> />${description}</label>
</#macro>
