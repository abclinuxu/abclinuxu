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
    <h1 class="st_nadpis"><a href="${url}">${TOOL.xpath(clanek,"data/name")}</a></h1>
    <p>${TOOL.xpath(clanek,"/data/perex")}</p>
    <p class="cl_inforadek">
        ${DATE.show(clanek.created, dateFormat[0])} |
        <#list autors as autor>
            <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
        </#list> |
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
    <#local visits = TOOL.getRelationCountersValue(items,"visit"), reads = TOOL.getRelationCountersValue(items,"read")>

    <table class="sw-polozky">
      <thead>
        <tr>
            <td class="td01">Jméno</td>
            <td class="td02">Hodnocení</td>
            <!--<td class="td03">Přečteno</td>-->
            <td class="td04">Navštíveno</td>
            <td class="td05">Poslední úprava</td>
        </tr>
      </thead>
      <tbody>
        <#list SORT.byName(ITEMS) as software>
            <tr>
                <td class="td01"><a href="${software.url}" title="${TOOL.childName(software)}">${TOOL.childName(software)}</a></td>
                <td class="td02"><@showShortRating software, "", false /></td>
                <!--<td class="td03"><@showCounter software.child, reads, "read" />&times;</td>-->
                <td class="td04"><@showCounter software.child, visits, "visit" />&times;</td>
                <td class="td05">${DATE.show(software.child.updated, "SMART")}</td>
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
   title=TOOL.xpath(ITEM, "/data/title")?default("Zprávička")
 >
    <h3 class="st_nadpis"><a href="${url}" title="${title}">${title}</a></h3>
    <p>
        ${TOOL.xpath(ITEM,"data/content")}<br>
        <span style="font-size: smaller">
        <a href="/Profile/${autor.id}">${autor.name}</a> | ${DATE.show(ITEM.created,"CZ_FULL")} |
        <a href="${url}" title="${title}">Komentáře: ${diz.responseCount}</a><#rt>
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
    <p>${TOOL.xpath(item,"data/content")}</p>
    <span><a href="/Profile/${autor.id}">${TOOL.nonBreakingSpaces(autor.name)}</a>
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
	<#local blacklisted = diz.isBlacklisted(comment)>
	<div class="ds_hlavicka<#if diz.isUnread(comment)>_novy</#if><#if blacklisted> ds_hlavicka_blacklisted</#if><#if who?exists && USER?exists && who.id == USER.id> ds_hlavicka_me</#if>" id="${comment.id}">
    <#if comment.author?exists && showControls>
        <#assign avatar = TOOL.getUserAvatar(who?if_exists, USER?if_exists)?default("UNDEFINED")>
        <#if avatar != "UNDEFINED">
            <img src="${avatar}" id="comment${comment.id}_avatar" alt="avatar" class="ds_avatar <#if blacklisted>ds_controls_blacklisted</#if>" >
        </#if>
    </#if>
  ${DATE.show(comment.created,"SMART")}
  <#if comment.author?exists>
   <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
   <#local blog=TOOL.getUserBlogAnchor(who, "blog")?default("UNDEF")>
   <#if blog!="UNDEF">&nbsp;| blog: ${blog}</#if>
   <#local city=TOOL.xpath(who,"//personal/city")?default("UNDEF")><#if city!="UNDEF"> | ${city}</#if>
  <#else>
   ${comment.anonymName?if_exists}
  </#if><br>
  <#if blacklisted>
     <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle2" class="ds_control_sbalit" title="Schová nebo rozbalí celé vlákno">Rozbalit</a>
	 <#else>
     <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle2" class="ds_control_sbalit2" title="Schová nebo rozbalí celé vlákno">Rozbalit</a>
  </#if>
  ${comment.title?if_exists}
  <#if showControls>
	 <div id="comment${comment.id}_controls"<#if blacklisted> class="ds_controls_blacklisted"</#if>>
         <#assign nextUnread = diz.getNextUnread(comment)?default("UNDEF")>
         <#if ! nextUnread?is_string><a href="#${nextUnread}" title="Skočit na další nepřečtený komentář">Další</a> |</#if>
         <a href="${URL.make("/EditDiscussion/"+diz.relationId+"?action=add&amp;dizId="+diz.id+"&amp;threadId="+comment.id+extra[0]?default(""))}">Odpovědět</a> |
         <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}" title="Žádost o přesun diskuse, stížnost na komentář">Admin</a> |
         <a href="#${comment.id}" title="Přímá adresa na tento komentář">Link</a> |
         <#if (comment.parent?exists)><a href="#${comment.parent}" title="Odkaz na komentář o jednu úroveň výše">Výše</a> |</#if>
         <#if comment.author?exists>
             <#if blacklisted><#local action="fromBlacklist", title="Neblokovat", hint="Odstraní autora ze seznamu blokovaných uživatelů">
             <#else><#local action="toBlacklist", title="Blokovat", hint="Přidá autora na seznam blokovaných uživatelů"></#if>
             <#if USER?exists><#local myId=USER.id></#if>
             <a href="${URL.noPrefix("/EditUser/"+myId?if_exists+"?action="+action+"&amp;bUid="+who.id+"&amp;url="+URL.prefix+"/show/"+diz.relationId+"#"+comment.id)}" title="${hint}">${title}</a> |
         </#if>
         <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle1" title="Schová nebo rozbalí celé vlákno" class="ds_control_sbalit3"><#if ! blacklisted>Sbalit<#else>Rozbalit</#if></a>
     </div>
  <#elseif USER?exists && USER.hasRole("discussion admin")>
      <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}">Admin</a>
  </#if>
  <div style="clear: right"></div><!-- aby avatar nepresahoval -->
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
    <p class="cenzura">
        <#assign admin = TOOL.xpath(comment.data,"//censored/@admin")?default("5473")>
        Náš <a href="/Profile/${admin}">administrátor</a>
        shledal tento příspěvek závadným nebo nevyhovujícím zaměření portálu.
        <#assign message = TOOL.xpath(comment.data,"//censored")?default("")>
        <#if message?has_content><br>${message}</#if>
        <br><a href="${URL.make("/show?action=censored&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Zobrazit</a> příspěvek
    </p>
    <#if USER?exists && USER.hasRole("discussion admin")>
        <a href="${URL.make("/EditDiscussion?action=censore&amp;rid="+relId+"&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Odvolat cenzuru</a>
    </#if>
</#macro>

<#macro showDiscussion(relation)>
    <#local DIZ = TOOL.createDiscussionTree(relation.child,USER?if_exists,relation.id,true)>
    <#if DIZ.monitored><#local monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj"></#if>

    <div class="ds_toolbox">
     <b>Nástroje:</b>
       <#if DIZ.hasUnreadComments>
         <a href="#${DIZ.firstUnread}" title="Skočit na první nepřečtený komentář" rel="nofollow">První nepřečtený komentář</a>,
       </#if>
         <a href="${URL.make("/EditMonitor/"+DIZ.relationId+"?action=toggle")}" rel="nofollow">${monitorState}</a>
           <span title="Počet lidí, kteří sledují tuto diskusi">(${DIZ.monitorSize})</span>
           <a class="info" href="#">?<span class="tooltip">Zašle každý nový komentář emailem na vaši adresu</span></a>,
         <a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print" rel="nofollow">Tisk</a>
       <#if USER?exists && USER.hasRole("discussion admin")>
         <br />
         <b>Admin:</b>
         <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+DIZ.relationId+"&amp;dizId="+DIZ.id)}">
            <#if DIZ.frozen>Rozmrazit<#else>Zmrazit</#if></a>
       </#if>
    </div>

    <p>
        <#if DIZ.frozen>
            Diskuse byla administrátory uzamčena
        <#else>
            <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DIZ.id+"&amp;threadId=0&amp;rid="+DIZ.relationId)}" rel="nofollow">
            Vložit další komentář</a>
        </#if>
    </p>

    <#list DIZ.threads as thread>
       <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
    </#list>

    <#if (!DIZ.frozen && DIZ.size>3)>
     <p><a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+DIZ.id+"&amp;rid="+DIZ.relationId)}" rel="nofollow">
     Založit nové vlákno</a></p>
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
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0")}" target="rating" rel="nofollow">špatné</a> &bull; <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3")}" target="rating" rel="nofollow">dobré</a>
        <#else>
           <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0&amp;return=true")}" rel="nofollow">špatné</a> &bull; <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3&amp;return=true")}" rel="nofollow">dobré</a>
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

<#macro month (month)>
    <#if month=="1">leden
    <#elseif month=="2">únor
    <#elseif month=="3">březen
    <#elseif month=="4">duben
    <#elseif month=="5">květen
    <#elseif month=="6">červen
    <#elseif month=="7">červenec
    <#elseif month=="8">srpen
    <#elseif month=="9">září
    <#elseif month=="10">říjen
    <#elseif month=="11">listopad
    <#elseif month=="12">prosinec
    </#if>
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
        <input name="submit" type="submit" class="button" value="Hlasuj" src="/images/site2/vote_btn.gif" alt="Hlasuj">
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