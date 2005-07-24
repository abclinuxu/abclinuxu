<#macro showArticle(relation)>
 <#local clanek=relation.child>
 <p>
  ${DATE.show(clanek.created, "CZ_SHORT")}
  <a href="${relation.url?default("/clanky/show/"+relation.id)}">${TOOL.xpath(clanek,"data/name")}</a><br>
  ${TOOL.xpath(clanek,"/data/perex")}
 </p>
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

<#macro showComment(comment) >
 <div class="ds_hlavicka">
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"CZ_FULL")}
  <#if comment.author?exists>
   <#local who=TOOL.sync(comment.author)><a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
  <#else>
   ${TOOL.xpath(comment.data,"author")?if_exists}
  </#if><br>
  ${TOOL.xpath(comment.data,"title")?if_exists}<br>
 </div>
 <#if TOOL.xpath(comment.data,"censored")?exists>
  <#if TOOL.xpath(comment.data,"censored/@admin")?exists><#local message="Cenzura: "+TOOL.xpath(comment.data,"censored")></#if>
  <p class="cenzura">
   ${message?default("Na¹i administrátoøi shledali tento pøíspìvek závadným.")}
  </p>
 <#else>
  <div class="ds_text">
    ${TOOL.render(TOOL.element(comment.data,"text"),USER?if_exists)}
  </div>
  <#if who?exists && TOOL.xpath(who,"/data/personal/signature")?exists
    && (! USER?exists || TOOL.xpath(USER,"//settings/signatures")?default("yes")=="yes")
  ><div class="signature">${TOOL.xpath(who,"/data/personal/signature")}</div></#if>
 </#if>
</#macro>

<#macro showThread(diz level)>
 <#local space=level*15>
 <div style="padding-left: ${space}pt">
  <@showComment diz />
 </div>
 <#if diz.children?exists>
  <#local level2=level+1>
  <#list diz.children as child>
    <@showThread child, level2 />
  </#list>
 </#if>
</#macro>
