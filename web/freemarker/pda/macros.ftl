<#macro showMessages>
 <#list MESSAGES as msg>
  <p class="message">${msg}</p>
 </#list>
 <#if ERRORS.generic?exists>
  <p class="error">${ERRORS.generic}</p>
 </#if>
</#macro>

<#macro showArticle(relation)>
  <#local clanek=relation.child, tmp=TOOL.groupByType(clanek.children),
          url=relation.url?default("/clanky/show/"+relation.id)>
  <#if tmp.discussion?exists><#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>
  <h1 class="uvod"><a href="${url}">${TOOL.xpath(clanek,"data/name")}</a></h1>
  <span class="uvod">
   ${DATE.show(clanek.created, "CZ_DM")}
   | <a href="/Profile/${autor.id}">${autor.name}</a>
   | Pøeèteno: ${TOOL.getCounterValue(clanek,"read")}x
   <#if diz?exists>| <@showCommentsInListing diz, "CZ_DM", "/clanky" /></#if>
   <#if rating!="UNDEF">| Hodnocení:&nbsp;${rating.result?string["#0.00"]}</#if>
  </span>
 <p class="vytah">
  ${TOOL.xpath(clanek,"/data/perex")}
 </p>
</#macro>

<#macro showCommentsInListing(diz dateFormat urlPrefix)>
    <a href="${diz.url?default(urlPrefix+"/show/"+diz.relationId)}">Komentáøù:&nbsp;
    ${diz.responseCount}<@markNewComments diz/></a><#rt>
    <#lt><#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, dateFormat)}</#if>
</#macro>

<#macro showNews(relation)>
 <#local ITEM=TOOL.sync(relation.child), diz=TOOL.findComments(ITEM)>
 <p>${DATE.show(ITEM.created,"CZ_SHORT")}
 ${TOOL.xpath(ITEM,"data/content")}<br>
 <span style="font-size: 7pt">
  <a href="${relation.url?default("/zpravicky/show/"+relation.id)}" target="_content" style="font-size: 7pt">Zobrazit</a>
  <#if diz.responseCount gt 0>
   Komentáøe: ${diz.responseCount}, poslední ${DATE.show(diz.updated, "CZ_FULL")}
  </#if>
 </span>
 </p>
</#macro>

<#macro separator double=false>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="--------------------"><br>
 <#if double><img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="" vspace="1"><br></#if>
</#macro>

<#macro markNewComments(discussion)><#t>
<#if TOOL.hasNewComments(USER?if_exists, discussion)><#t>
    <span title="V diskusi jsou nové komentáøe" class="new_comment_mark">*</span><#t>
</#if><#t>
</#macro>

<#macro showThread(comment level diz showControls extra...)>
 <div>
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"CZ_FULL")} <#if diz.isUnread(comment)>(nový)</#if>
  <#if comment.author?exists>
   <#local who=TOOL.createUser(comment.author)><a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
   <#local city=TOOL.xpath(who,"//personal/city")?default("UNDEF")><#if city!="UNDEF"> | ${city}</#if>
  <#else>
   ${comment.anonymName?if_exists}
  </#if><br>
  ${comment.title?if_exists}<br>
  <#if showControls>
   <#assign nextUnread = diz.getNextUnread(comment)?default("UNDEF")>
   <#if ! nextUnread?is_string><a href="#${nextUnread}" title="Skoèit na dal¹í nepøeètený komentáø">Dal¹í</a> |</#if>
   <a href="#${comment.id}" title="Pøímá adresa na tento komentáø">Link</a> |
   <#if (comment.parent?exists)><a href="#${comment.parent}" title="Odkaz na komentáø o jednu úroveò vý¹e">Vý¹e</a> |</#if>
  </#if>
 </div>
 <div>
  <#if TOOL.xpath(comment.data,"censored")?exists>
     <@showCensored comment, dizId, relId/>
  <#else>
   <div class="ds_text">
     ${TOOL.render(TOOL.element(comment.data,"//text"),USER?if_exists)}
   </div>
   <#assign signature = TOOL.getUserSignature(who?if_exists, USER?if_exists)?default("UNDEFINED")>
   <#if signature!="UNDEFINED"><div class="signature">${signature}</div></#if>
  </#if>
  <#local level2=level+1>
  <div style="padding-left: 15pt">
   <#list comment.children?if_exists as child>
    <@showThread child, level2, diz, showControls, extra[0]?if_exists />
   </#list>
  </div>
 </div>
</#macro>

<#macro showCensored(comment dizId relId)>
    <p class="cenzura">
        <#assign admin = TOOL.xpath(comment.data,"//censored/@admin")?default("5473")>
        Ná¹ <a href="/Profile/${admin}">administrátor</a>
        shledal tento pøíspìvek závadným nebo nevyhovujícím zamìøení portálu.
        <#assign message = TOOL.xpath(comment.data,"//censored")?default("")>
        <#if message?has_content><br>${message}</#if>
    </p>
</#macro>
