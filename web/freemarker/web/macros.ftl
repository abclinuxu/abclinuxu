<#macro showMessages>
 <#list MESSAGES as msg>
  <p class="message">${msg}</p>
 </#list>
 <#if ERRORS.generic?exists>
  <p class="error">${ERRORS.generic}</p>
 </#if>
</#macro>

<#macro showArticle(relation dateFormat...)>
    <#local clanek=relation.child,
        autors=TOOL.createAuthorsForArticle(clanek),
        thumbnail=TOOL.xpath(clanek,"/data/thumbnail")?default("UNDEF"),
        tmp=TOOL.groupByType(clanek.children, "Item"),
        url=relation.url?default("/clanky/show/"+relation.id)
    >
    <#if tmp.discussion?exists><#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>
    <#if thumbnail!="UNDEF"><div class="cl_thumbnail">${thumbnail}</div></#if>
    <h1 class="st_nadpis"><a href="${url}">${clanek.title}</a></h1>
    <p>${TOOL.xpath(clanek,"/data/perex")}</p>
    <p class="meta-vypis">
        ${DATE.show(clanek.created, dateFormat[0])} |

        <#if autors?size gt 0>
            <#list autors as autor>
                <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
            </#list>
        <#else>
            <@lib.showUser TOOL.createUser(clanek.owner)/>
        </#if>
        |
        Přečteno: <@showCounter clanek, .globals["CITACE"]?if_exists, "read" />&times;
        <#if diz?exists>| <@showCommentsInListing diz, dateFormat[1]?default(dateFormat[0]), "/clanky" /></#if>
        <@showShortRating relation, "| " />
    </p>
</#macro>

<#macro showCounter(item, map, type)><#rt>
    <#if map.get?exists><#local value = map.get(item)?default("UNDEF")><#else><#local value = "UNDEF"></#if><#rt>
    <#lt><#if value?string!="UNDEF">${value}<#else>${TOOL.getCounterValue(item, type)}</#if><#rt>
</#macro>

<#macro showCommentsInListing(diz dateFormat urlPrefix)>
    <a href="${diz.url?default(urlPrefix+"/show/"+diz.relationId)}">Komentářů:&nbsp;${diz.responseCount}<@markNewComments diz/></a><#rt>
    <#lt><#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, dateFormat)}</#if>
</#macro>

<#macro showSoftwareList(items)>
<#local visits = TOOL.getRelationCountersValue(items,"visit")>
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
      <td class="td-meta"><@showCounter software.child, visits, "visit" />&times;</td>
      <td class="td-datum">${DATE.show(software.child.updated, "SMART")}</td>
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
   url=relation.url?default("/zpravicky/show/"+relation.id)
 >
    <h3 class="st_nadpis"><a href="${url}" title="${ITEM.title}">${ITEM.title}</a></h3>
    <p>
        ${TOOL.xpath(ITEM,"data/content")}<br>
        <span style="font-size: smaller">
        <@showUser autor/> | ${DATE.show(ITEM.created,"CZ_FULL")} |
        <a href="${url}" title="${ITEM.title}">Komentáře: ${diz.responseCount}</a><#rt>
        <#lt><#if diz.responseCount gt 0><@markNewComments diz/>, poslední ${DATE.show(diz.updated, "SMART")}</#if>
        </span>
    </p>
</#macro>

<#macro showTemplateNews(relation)>
  <#local item=TOOL.sync(relation.child),
    autor=TOOL.createUser(item.owner),
    diz=TOOL.findComments(item),
    url=relation.url?default("/zpravicky/show/"+relation.id)>
    <span>${DATE.show(item.created,"CZ_SHORT")} | ${NEWS_CATEGORIES[item.subType].name}</span>
    <#local text=TOOL.xpath(item,"data/content"), shortened=TOOL.limitNewsLength(text)?default("UNDEFINED")>
    <#if shortened=="UNDEFINED">
        <p>${text}</p>
    <#else>
        <p>${shortened}... <i><a href="${url}">Více&raquo;</a></i></p>
    </#if>
    <span><@showUser autor/>
    | <a href="${url}" title="<#if diz.responseCount gt 0>poslední&nbsp;${DATE.show(diz.updated, "SMART")}</#if>"
    >Komentářů: ${diz.responseCount}<@lib.markNewComments diz/></a></span>
</#macro>

<#macro markNewComments(discussion)><#t>
<#if TOOL.hasNewComments(USER?if_exists, discussion)><#t>
    <span title="V diskusi jsou nové komentáře" class="new_comment_mark">*</span><#t>
</#if><#t>
</#macro>

<#macro markNewCommentsQuestion(discussion)><#t>
<#if TOOL.hasNewComments(USER?if_exists, discussion)><#t>
    <span title="V diskusi jsou nové komentáře" class="new_comment_state">*</span><#t>
</#if><#t>
</#macro>

<#macro separator double=false>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="--------------------"><br>
 <#if double><img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="" vspace="1"><br></#if>
</#macro>

<#macro showThread(comment level diz showControls extra...)>
    <#if comment.author?exists>
        <#local who=TOOL.createUser(comment.author)>
    </#if>
      <#local blacklisted = diz.isBlacklisted(comment), attachments = comment.attachments>
      <div class="ds_hlavicka<#if diz.isUnread(comment)>_novy</#if><#if blacklisted> ds_hlavicka_blacklisted</#if><#if who?exists && USER?exists && who.id == USER.id> ds_hlavicka_me</#if>" id="${comment.id}">
        <#if comment.author?exists && showControls>
            <#assign avatar = TOOL.getUserAvatar(who?if_exists, USER?if_exists)?default("UNDEFINED")>
            <#if avatar != "UNDEFINED">
                <img src="${avatar}" id="comment${comment.id}_avatar" alt="avatar" class="ds_avatar <#if blacklisted>ds_controls_blacklisted</#if>">
            </#if>
        </#if>
        ${DATE.show(comment.created,"SMART")}
        <#if comment.author?exists>
            <@showUser who/>
            <#local score=who.getIntProperty("score")?default(-1)><#if score != -1> | skóre: ${score}</#if>
            <#local blog=TOOL.getUserBlogAnchor(who, "blog")?default("UNDEF")><#if blog!="UNDEF"> | blog: ${blog}</#if>
            <#local city=TOOL.xpath(who,"//personal/city")?default("UNDEF")><#if city!="UNDEF"> | ${city}</#if>
        <#else>
            ${comment.anonymName?if_exists}
        </#if><br>
        <span class="<#if blacklisted>ds_control_sbalit<#else>ds_control_sbalit2</#if>" id="comment${comment.id}_toggle2">
            <a onClick="schovej_vlakno(${comment.id})" title="Schová nebo rozbalí celé vlákno">Rozbalit</a>
            <a onClick="rozbal_vse(${comment.id})" title="Schová nebo rozbalí vše pod tímto komentářem">Rozbalit vše</a>
        </span>
        ${comment.title?if_exists}
        <#nested>
        <#if showControls>
            <div id="comment${comment.id}_controls"<#if blacklisted> class="ds_controls_blacklisted"</#if>>
                <#local nextUnread = diz.getNextUnread(comment)?default("UNDEF")>
                <#if ! nextUnread?is_string><a href="#${nextUnread}" title="Skočit na další nepřečtený komentář">Další</a> |</#if>
                <a href="${URL.make("/EditDiscussion/"+diz.relationId+"?action=add&amp;dizId="+diz.id+"&amp;threadId="+comment.id+extra[0]?default(""))}">Odpovědět</a> |
                <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}" title="Žádost o přesun diskuse, stížnost na komentář">Admin</a> |
                <a href="#${comment.id}" title="Přímá adresa na tento komentář">Link</a> |
                <#if (comment.parent?exists)><a href="#${comment.parent}" title="Odkaz na komentář o jednu úroveň výše">Výše</a> |</#if>
                <#if comment.author?exists><#local blockTarget="bUid=" + who.id><#else><#local blockTarget="bName=" + comment.anonymName?url></#if>
                <#if blacklisted>
                    <#local blockUrl="/EditUser?action=fromBlacklist&amp;"+blockTarget, title="Neblokovat", hint="Odstraní autora ze seznamu blokovaných uživatelů">
                <#else>
                    <#local blockUrl="/EditUser?action=toBlacklist&amp;"+blockTarget, title="Blokovat", hint="Přidá autora na seznam blokovaných uživatelů">
                </#if>
                <a href="${URL.noPrefix(blockUrl+TOOL.ticket(USER?if_exists, false)+"&amp;url="+URL.prefix+"/show/"+diz.relationId+"#"+comment.id)}" title="${hint}">${title}</a> |
                <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle1" title="Schová nebo rozbalí celé vlákno" class="ds_control_sbalit3"><#if ! blacklisted>Sbalit<#else>Rozbalit</#if></a>
            </div>
        <#elseif USER?exists && USER.hasRole("discussion admin")>
            <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}">Admin</a>
        </#if>
        <#if (attachments?size > 0)>
            <div class="ds_attachments"><span><#if (attachments?size == 1)>Příloha:<#else>Přílohy:</#if></span>
                <ul>
                    <#list attachments as id>
                        <li><#local attachment = diz.attachments(id)>
                        <a href="${TOOL.xpath(attachment, "/data/object/@path")}">${TOOL.xpath(attachment, "/data/object/originalFilename")}</a>
                        (${TOOL.xpath(attachment, "/data/object/size")} bytů)</li>
                    </#list>
                </ul>
           </div>
        </#if>
    </div>
    <div id="comment${comment.id}" <#if who?exists>class="ds_text_user${who.id}"</#if><#if blacklisted?if_exists> style="display: none;"</#if>>
        <#if TOOL.xpath(comment.data,"//censored")?exists>
            <@showCensored comment, diz.id, diz.relationId/>
        <#else>
            <div class="ds_text">
                ${TOOL.render(TOOL.element(comment.data,"//text"),USER?if_exists)}
            </div>
            <#assign signature = TOOL.getUserSignature(who?if_exists, USER?if_exists)?default("UNDEFINED")>
            <#if signature!="UNDEFINED"><div class="signature">${signature}</div></#if>
        </#if>
        <#local level2=level+1>
        <div class="ds_odsazeni">
            <#list comment.children?if_exists as child>
                <@showThread child, level2, diz, showControls, extra[0]?if_exists />
            </#list>
        </div>
    </div>
</#macro>

<#macro showCensored(comment dizId relId)>
    <div class="cenzura">
        <img src="/images/site2/skryty-komentar-krizek.png" width="40" height="40" alt="skrytý komentář" style="float:right;">
        <#assign admin = TOOL.xpath(comment.data,"//censored/@admin")?default("5473")>
        Náš <a href="/Profile/${admin}">administrátor</a> shledal tento komentář
        <a href="/faq/abclinuxu.cz/proc-je-komentar-oznacen-jako-zavadny">závadným</a>.
        <#assign message = TOOL.xpath(comment.data,"//censored")?default("")>
        <#if message?has_content>
            <p class="cenzura_duvod">${message}</p>
        </#if>
        <a href="${URL.make("/show?action=censored&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Zobrazit komentář</a>
        <#if USER?exists && USER.hasRole("discussion admin")>
            <a href="${URL.make("/EditDiscussion?action=censore&amp;rid="+relId+"&amp;dizId="+dizId+"&amp;threadId="+comment.id+TOOL.ticket(USER?if_exists, false))}">Odvolat cenzuru</a>
        </#if>
    </div>
</#macro>

<#macro showDiscussion(relation)>
    <#local DIZ = TOOL.createDiscussionTree(relation.child,USER?if_exists,relation.id,true)>
    <#if DIZ.monitored><#local monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj"></#if>
    <div class="ds_toolbox">
     <b>Nástroje:</b>
       <#if DIZ.hasUnreadComments>
         <a href="#${DIZ.firstUnread}" title="Skočit na první nepřečtený komentář" rel="nofollow">První nepřečtený komentář</a>,
       </#if>
         <a href="${URL.make("/EditMonitor/"+DIZ.relationId+"?action=toggle"+TOOL.ticket(USER?if_exists, false))}" rel="nofollow">${monitorState}</a>
           <span title="Počet lidí, kteří sledují tuto diskusi">(${DIZ.monitorSize})</span>
           <a class="info" href="#">?<span class="tooltip">Zašle každý nový komentář emailem na vaši adresu</span></a>,
         <a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print" rel="nofollow">Tisk</a>
       <#if USER?exists && USER.hasRole("discussion admin")>
         <br />
         <b>Admin:</b>
         <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+DIZ.relationId+"&amp;dizId="+DIZ.id+TOOL.ticket(USER?if_exists, false))}">
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
    <#local rating=TOOL.ratingFor(relation.child.data)?default("UNDEF")>
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
        <#if USER?exists>
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0"+TOOL.ticket(USER?if_exists, false))}" target="rating" rel="nofollow">špatné</a>
           &bull;
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3"+TOOL.ticket(USER?if_exists, false))}" target="rating" rel="nofollow">dobré</a>
        <#else>
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0&amp;return=true"+TOOL.ticket(USER?if_exists, false))}" rel="nofollow">špatné</a>
           &bull;
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3&amp;return=true"+TOOL.ticket(USER?if_exists, false))}" rel="nofollow">dobré</a>
        </#if>
        </div>
        <iframe name="rating" frameborder="0" height="20" scrolling="no" class="rating_iframe"></iframe>
    </div>
</#macro>

<#macro showShortRating (relation, separator, heading=true)>
    <#local rating=TOOL.ratingFor(relation.child.data)?default("UNDEF")>
    <#if rating!="UNDEF"><#if heading>${separator}Hodnocení:&nbsp;</#if>${rating.percent}&nbsp;%&nbsp;(${rating.count}&nbsp;hlasů)</#if>
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

<#macro showPoll (relation url=relation.url?default("/ankety/show/"+relation.id))>
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
                <#if link.description?exists>
                    <dd>${link.description}</dd>
                </#if>
            </#list>
            </dl>
        </div>
    </#if>
</#macro>

<#macro showOption (param value caption type extra...)>
    <label>
        <input type="${type}" name="${param}" value="${value}"<#if TOOL.isWithin(PARAMS[param], value)> checked</#if>${" "+extra[0]?if_exists}>
        ${caption}
    </label>
</#macro>

<#macro showOption2 (param value caption type values)>
    <label>
        <input type="${type}" name="${param}" value="${value}"<#if TOOL.isWithin(values, value)> checked</#if>> ${caption}
    </label>
</#macro>

<#macro showOption3 (param value caption type condition)>
    <label>
        <input type="${type}" name="${param}" value="${value}"<#if condition> checked</#if>> ${caption}
    </label>
</#macro>

<#macro showOption4 (value caption values)>
    <option value="${value}"<#if TOOL.isWithin(values, value)> selected</#if>>${caption}</option>
</#macro>

<#macro advertisement (id)>${TOOL.getAdvertisement(id, .vars)}</#macro>

<#macro showDiscussionState diz>
    <@markNewCommentsQuestion diz/>
    <#if TOOL.xpath(diz.discussion,"/data/frozen")?exists>
        <img src="/images/site2/zamceno.gif" alt="Z" title="Diskuse byla administrátory uzamčena">
    </#if>
    <#if TOOL.isQuestionSolved(diz.discussion.data)>
        <img src="/images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle čtenářů vyřešena">
    </#if>
    <#if USER?exists && TOOL.xpath(diz.discussion,"//monitor/id[text()='"+USER.id+"']")?exists>
        <img src="/images/site2/sledovano.gif" alt="S" title="Tuto diskusi sledujete monitorem">
    </#if>
</#macro>

<#macro showUser user><a href="/lide/${user.login}">${user.nick?default(user.name)}</a></#macro>

<#macro showRevisions relation>
    <p class="documentHistory">
        <#local info = TOOL.getRevisionInfo(relation.child)>
        Dokument vytvořil: <@showUser info.creator/>, ${DATE.show(relation.child.created,"SMART")}.
        <#if (info.lastRevision > 1)>
            Poslední úprava: <@showUser info.lastCommiter/>, ${DATE.show(relation.child.updated,"SMART")}.
            <#if (info.committers?size > 0)>
                Další přispěvatelé:
                <#list info.committers as committer><#rt>
                    <@showUser committer/><#rt>
                    <#lt><#if committer_has_next>, <#else>.</#if>
                </#list>
            </#if>
            <a href="/revize?rid=${relation.id}&amp;prefix=${URL.prefix}" rel="nofollow">Historie změn</a>
        </#if>
    </p>
</#macro>

<#macro showHelp>
    <a class="info" href="#">?<span class="tooltip"><#nested></span></a>
</#macro>

<#macro showError key>
    <#if ERRORS[key]?exists><div class="error">${ERRORS[key]}</div></#if>
</#macro>

<#macro initRTE>
    <#if (RTE.wysiwygMode && RTE.instances?size > 0)>
        <script type="text/javascript" src="/data/fckeditor/fckeditor.js"></script>
        <script type="text/javascript">
        window.onload = function() {
            <#list RTE.instances as editor>
                var aFCKeditor = new FCKeditor('${editor.id}');
                aFCKeditor.BasePath = '/data/fckeditor/';
                <#if editor.inputMode == "news">
                    aFCKeditor.Config['CustomConfigurationsPath'] = aFCKeditor.BasePath + 'NewsGuard_config.js';
                    aFCKeditor.ToolbarSet = 'NewsGuard';
                <#else>
                    aFCKeditor.Config['CustomConfigurationsPath'] = aFCKeditor.BasePath + 'SafeHTMLGuard_config.js';
                    <#if editor.inputMode == "wiki">
                        aFCKeditor.ToolbarSet = 'WikiContentGuard';
                    <#elseif editor.inputMode == "blog">
                        aFCKeditor.ToolbarSet = 'BlogGuard';
                    <#else>
                        aFCKeditor.ToolbarSet = 'SafeHTMLGuard';
                    </#if>
                </#if>
                <#if editor.commentedContent?exists>
                    aFCKeditor.Config['AbcCitationContent'] = '${editor.commentedContent?js_string}';
                </#if>
                aFCKeditor.Config['ProcessHTMLEntities'] = false ;
                aFCKeditor.ReplaceTextarea();
            </#list>
        }
        </script>
    </#if>
</#macro>

<#macro showTagCloud list title cssStyle>
    <div id="tagcloud_container"<#if (cssStyle?length gte 1)> style="${cssStyle}"</#if>>
    <#if title?exists ><div id="title">${title}</div></#if>
	<ul id="tagcloud">
		<#list list as tag>
			<li class="${tag.cssClass}">
			    <a href="/stitky/${tag.id}" title="Štítek ${tag.title}: ${tag.usage} použití">${tag.title}</a>
			</li>
		</#list>
	</ul>
    </div>
</#macro>

<#macro repeat times>
    <#if times lt 1><#return></#if>
    <#list 1..times as temp>
        <#nested/>
    </#list>
</#macro>

<#macro showForum rid numQuestions onHP showAdvertisement>
    <#local forum = VARS.getFreshQuestions(numQuestions, rid),
            feed = FEEDS.getForumFeedUrl(rid)?default("UNDEF"),
            FORUM=TOOL.analyzeDiscussions(forum)>

      <table class="ds" id="forum_table_${rid}">
       <#if USER?exists><form method="post" action="/EditUser/${USER.id}"></#if>
        <thead>
          <tr>
            <td class="td-nazev">
              <span class="meta-odkazy">
                 <a href="/forum/EditDiscussion?action=addQuez&amp;rid=${rid}">Položit dotaz</a>,
                 <a href="/forum/dir/${rid}?from=${FORUM?size}&amp;count=20">Starší dotazy</a>
              </span>
              <#local relation=TOOL.createRelation(rid)>
              <span class="st_nadpis"><a href="${relation.url}" title="${TOOL.childName(relation)}">${TOOL.childName(relation)}</a></span>
            </td>
            <td class="td-meta">Stav</td>
            <td class="td-meta">Reakcí</td>
            <td class="td-datum">
                Poslední
                <#if feed!="UNDEF">
                   &nbsp;<a href="${feed}"><img src="/images/site2/feed12.png" width="12" height="12" border="0" alt="${TOOL.childName(relation)}, RSS feed"></a>
                </#if>
                <#if USER?exists>
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
       <#if USER?exists></form></#if>
        <tbody id="forum_tbody_${rid}">
         <#list FORUM as diz>
          <tr>
            <td><a href="${diz.url}">${TOOL.limit(diz.title,60,"...")}</a></td>
            <td class="td-meta"><@lib.showDiscussionState diz /></td>
            <td class="td-meta">${diz.responseCount}</td>
            <td class="td-datum">${DATE.show(diz.updated,"SMART")}</td>
          </tr>
         </#list>
        </tbody>
        <tfoot id="forum_tfoot_${rid}">
            <script type="text/javascript"><!--
            new Forum(${rid}, ${FORUM?size}, ${VARS.maxSizes.question});
            //--></script>
        </tfoot>
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
            region=item.string1?default("UNDEF"),
            regs=TOOL.xpathValue(item.data, "count(//registrations/registration)")>

    <#if subtype=="community"><#local subtype="Komunitní">
    <#elseif subtype=="educational"><#local subtype="Vzdělávací">
    <#elseif subtype=="company"><#local subtype="Firemní">
    </#if>

    <#assign tmp=TOOL.groupByType(item.children, "Item")>
    <#if tmp.discussion?exists><#assign diz=TOOL.analyzeDiscussion(tmp.discussion[0])><#else><#assign diz=null></#if>

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
                <#assign logo=TOOL.xpath(item, "/data/icon")?default("NOLOGO")>
                <#if logo!="NOLOGO">
                    <div class="cl_thumbnail"><img src="${logo}" alt="Logo akce ${TOOL.childName(item)}"></div>
                </#if>
            </#if>
            <h2 class="st_nadpis"><a href="${relation.url?default("/akce/show/"+relation.id)}">${TOOL.childName(item)}</a></h2>
            <p>${TOOL.xpath(item, "/data/descriptionShort")}</p>

            <p class="meta-vypis">Aktualizováno: ${DATE.show(item.updated,"SMART")}
                | správce:&nbsp;<@lib.showUser TOOL.createUser(item.owner) />
                <#if diz?exists>| <@lib.showCommentsInListing diz, "CZ_SHORT", "/akce" /></#if>
                | Přečteno:&nbsp;${TOOL.getCounterValue(item,"read")}&times; |
                <a href="${relation.url?default("/akce/"+relation.id)}?action=participants">Účastníků:&nbsp;${regs?eval}</a>
            </p>
            <#if showManagement>
                <div>
                    <a href="${URL.noPrefix("/akce/edit/"+relation.id+"?action=approve"+TOOL.ticket(USER,false))}">Schválit</a>
                    |
                    <a href="${URL.noPrefix("/EditRelation/"+RELATION.id+"?action=remove&amp;prefix=/akce")}">Smazat</a>
                </div>
            </#if>
        </td>
    </tr>
    </table>
</#macro>

<#macro showSubportal relation showDesc>
    <#local item=relation.child, icon=TOOL.xpath(item,"/data/icon")?default("UNDEF"),
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
            ${TOOL.render(TOOL.xpath(item,"/data/descriptionShort"), USER?if_exists)}
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
        <#if score != -1><tr><td>Skóre:</td> <td>${score}</td></tr></#if>
      </table>
        <form action="/skupiny/edit/${relation.id}" method="post">
            <#if USER?exists && members.contains(""+USER.id)>
             <input type="submit" value="Odregistrovat se">
            <#else>
             <input type="submit" value="Registrovat se">
            </#if>
            <input type="hidden" name="action" value="toggleMember">
            <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER?if_exists)}">
        </form>
    </div>
</#macro>

<#macro showVideo relation width height showLink>
    <#local item=relation.child, code=TOOL.xpath(item,"//code"), desc=TOOL.xpath(item,"//description")?default("")>
    <#if item.subType=="youtube"><#local player="http://www.youtube.com/v/"+code+"&amp;hl=en&amp;fs=1">
    <#elseif item.subType=="googlevideo"><#local player="http://video.google.com/googleplayer.swf?docid="+code+"&amp;hl=cs&amp;fs=true">
    </#if>

    <#if showLink>(<a href="${relation.url?default("/videa/show/"+relation.id)}">správa videa</a>)</#if><br>
    <object width="${width}" height="${height}"><param name="movie" value="${player}"></param><param name="allowFullScreen" value="true"></param><embed src="${player}" type="application/x-shockwave-flash" allowfullscreen="true" width="${width}" height="${height}"></embed></object>
    <#if desc!=""><p>${desc}</p></#if>
</#macro>
