<#macro showMessages>
 <#list MESSAGES as msg>
  <p class="message">${msg}</p>
 </#list>
 <#if ERRORS.generic?exists>
  <p class="error">${ERRORS.generic}</p>
 </#if>
</#macro>

<#macro showParents parents>
 <p>
 <#list TOOL.getParents(parents,USER?if_exists,URL) as link>
  <a href="${link.url}">${link.text}</a>
  <#if link_has_next> <img src="/images/site2/zarovnani.gif" align="absmiddle" alt="-"> </#if>
 </#list>
 </p>
</#macro>

<#macro showArticle(relation dateFormat...)>
 <#local clanek=relation.child,
         autor=TOOL.createUser(TOOL.xpath(clanek,"/data/author")),
         tmp=TOOL.groupByType(clanek.children) >
 <#if tmp.discussion?exists><#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>
  <h1 class="uvod"><a href="/clanky/show/${relation.id}">
  ${TOOL.xpath(clanek,"data/name")}</a></h1>
  <span class="uvod">
   ${DATE.show(clanek.created, dateFormat[0])}
   | <a href="/Profile/${autor.id}">${autor.name}</a>
   | Pøeèteno: ${TOOL.getCounterValue(clanek)}x

  <#if diz?exists>
   | <a href="/clanky/show/${diz.relationId}">
   Komentáøù: ${diz.responseCount}</a
     ><#if diz.responseCount gt 0>, poslední ${DATE.show(diz.updated, dateFormat[1]?default(dateFormat[0]))}</#if>
  </#if>

  | Hodnocení:

  <#assign rating=TOOL.ratingFor(clanek.data,"article")?default(0)>
  <#if rating!=0>${rating?string["#0.00"]}</#if>

  </span>
 <p class="vytah">
  ${TOOL.xpath(clanek,"/data/perex")}
 </p>
</#macro>

<#macro showNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   autor=TOOL.createUser(ITEM.owner),
   diz=TOOL.findComments(ITEM)
 >
 <p>${DATE.show(ITEM.created,"CZ_FULL")}
 <a href="/Profile/${autor.id}">${autor.name}</a><br>
 ${TOOL.xpath(ITEM,"data/content")}<br>
 <span style="font-size: smaller">
  <a href="/news/show/${relation.id}">Zobraz</a>
  Komentáøe: ${diz.responseCount}<#if diz.responseCount gt 0>, poslední ${DATE.show(diz.updated, "CZ_FULL")}</#if>
 </span>
 </p>
</#macro>

<#macro showTemplateNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   autor=TOOL.createUser(ITEM.owner),
   diz=TOOL.findComments(ITEM)
 >
 <p>${DATE.show(ITEM.created,"CZ_SHORT")}<br>
 <a href="/Profile/${autor.id}">${autor.name}</a><br>
 ${TOOL.xpath(ITEM,"data/content")}<br>
 <span>
  <a href="/news/show/${relation.id}">Zobrazit</a>
  Komentáøe: ${diz.responseCount}<#if diz.responseCount gt 0>, poslední ${DATE.show(diz.updated, "CZ_SHORT")}</#if>
 </span>
 </p>
</#macro>

<#macro separator double=false>
 <#if !double><div class="delimiter"></div></#if>
 <#if double><div class="delimiterEnd"></div></#if>
</#macro>

<#macro showComment(comment dizId relId showControls) >
 <p class="diz_header">
  <a name="${comment.id}"></a>
  <#if (MAX_COMMENT?default(99999) < comment.id) ><span class="diz_header_new">${DATE.show(comment.created,"CZ_FULL")}</span><#else>${DATE.show(comment.created,"CZ_FULL")}</#if>
  <#if comment.author?exists>
   <#local who=TOOL.sync(comment.author)>
   <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a><br>
  <#else>
   ${TOOL.xpath(comment.data,"author")?if_exists}<br>
  </#if>
  ${TOOL.xpath(comment.data,"title")?if_exists}<br>
  <#if showControls>
   <a href="${URL.make("/EditDiscussion/"+relId+"?action=add&dizId="+dizId+"&threadId="+comment.id)}">Odpovìdìt</a>
   <a href="#${comment.parent}">Nahoru</a>
   <#if USER?exists && USER.hasRole("discussion admin")>
    | <a href="${URL.make("/EditDiscussion/"+relId+"?action=edit&dizId="+dizId+"&threadId="+comment.id)}">Upravit</a>
    <#if (comment.id>0)>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=rm&dizId="+dizId+"&threadId="+comment.id)}">Smazat</a>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=censore&dizId="+dizId+"&threadId="+comment.id)}">Cenzura</a>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=move&dizId="+dizId+"&threadId="+comment.id)}">Pøesunout</a>
     <#if (comment.parent>0)>
      <a href="${URL.make("/EditDiscussion/"+relId+"?action=moveUp&dizId="+dizId+"&threadId="+comment.id)}">Pøesunout vý¹e</a>
     </#if>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=toQuestion&dizId="+dizId+"&threadId="+comment.id)}">Osamostatnit</a>
    </#if>
   </#if>
  </#if>
 </p>
 <#if TOOL.xpath(comment.data,"censored")?exists>
  <#if TOOL.xpath(comment.data,"censored/@admin")?exists><#local message="Cenzura: "+TOOL.xpath(comment.data,"censored")></#if>
  <p class="cenzura">
   ${message?default("Na¹i administrátoøi shledali tento pøíspìvek závadným.")}
   <br>Pøíspìvek si mù¾ete prohlédnout <a href="${URL.make("/show?action=censored&dizId="+dizId+"&threadId="+comment.id)}">zde</a>.
  </p>
  <#if USER?exists && USER.hasRole("discussion admin")>
   <a href="${URL.make("/EditDiscussion?action=censore&rid="+relId+"&dizId="+dizId+"&threadId="+comment.id)}">Odvolat cenzuru</a>
  </#if>
 <#else>
  <div class="diz_text">${TOOL.render(TOOL.element(comment.data,"text"),USER?if_exists)}</div>
  <#if who?exists && TOOL.xpath(who,"/data/personal/signature")?exists
    && (! USER?exists || TOOL.xpath(USER,"//settings/signatures")?default("yes")=="yes")
  ><div class="signature">${TOOL.xpath(who,"/data/personal/signature")}</div></#if>
 </#if>
</#macro>

<#macro showThread(diz level dizId relId showControls)>
 <#local space=level*15>
 <div style="padding-left: ${space}pt">
  <@showComment diz, dizId, relId, showControls />
 </div>
 <#if diz.children?exists>
  <#local level2=level+1>
  <#list diz.children as child>
   <@showThread child, level2, dizId, relId, showControls />
  </#list>
 </#if>
</#macro>

<#macro star value><#if (value>0.60)><img src="/images/site2/rating/star1.gif" alt="*" class="star" align="absmiddle"><#elseif (value<0.2)><img src="/images/site2/rating/star0.gif" alt="-" class="star" align="absmiddle"><#else><img src="/images/site2/rating/star5.gif" alt="+" class="star" align="absmiddle"></#if></#macro>