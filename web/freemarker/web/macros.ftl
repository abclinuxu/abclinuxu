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
        autor=TOOL.createUser(TOOL.xpath(clanek,"/data/author")),
        tmp=TOOL.groupByType(clanek.children),
        rating=TOOL.ratingFor(clanek.data,"article")?default(0) >
    <#if tmp.discussion?exists><#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>

        <h1 class="st_nadpis"><a href="/clanky/show/${relation.id}">${TOOL.xpath(clanek,"data/name")}</a></h1>
        <p>${TOOL.xpath(clanek,"/data/perex")}</p>
        <p class="cl_inforadek">${DATE.show(clanek.created, dateFormat[0])} |
            <a href="/Profile/${autor.id}">${autor.name}</a> |
            P�e�teno: ${TOOL.getCounterValue(clanek)}x
            <#if diz?exists>
                | <a href="/clanky/show/${diz.relationId}">
                Koment���: ${diz.responseCount}</a
                ><#if diz.responseCount gt 0>, posledn� ${DATE.show(diz.updated, dateFormat[1]?default(dateFormat[0]))}</#if>
            </#if>
            <#if rating!=0>| Hodnocen�: ${rating?string["#0.00"]}</#if>
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
  Koment��e: ${diz.responseCount}<#if diz.responseCount gt 0>, posledn� ${DATE.show(diz.updated, "CZ_FULL")}</#if>
 </span>
 </p>
</#macro>

<#macro separator double=false>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="--------------------"><br>
 <#if double><img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="" vspace="1"><br></#if>
</#macro>

<#macro showComment(comment dizId relId showControls extra...) >
 <div class="ds_hlavicka<#if (MAX_COMMENT?default(99999) < comment.id) >_novy</#if>">
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"CZ_FULL")}
  <#if comment.author?exists>
   <#local who=TOOL.sync(comment.author)>
   <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
  <#else>
   ${TOOL.xpath(comment.data,"author")?if_exists}
  </#if><br>
  ${TOOL.xpath(comment.data,"title")?if_exists}<br>
  <#if showControls>
   <a href="${URL.make("/EditDiscussion/"+relId+"?action=add&amp;dizId="+dizId+"&amp;threadId="+comment.id+extra[0]?default(""))}">Odpov�d�t</a> |
   <a href="#${comment.id}">Link</a>
   <#if (comment.parent>0)>
    | <a href="#${comment.parent}">Nahoru</a>
   </#if>
   <#if USER?exists && USER.hasRole("discussion admin")>
    || <a href="${URL.make("/EditDiscussion/"+relId+"?action=edit&dizId="+dizId+"&threadId="+comment.id)}">Upravit</a>
    <#if (comment.id>0)>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=rm&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Smazat</a>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=censore&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Cenzura</a>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=move&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">P�esunout</a>
     <#if (comment.parent>0)>
      <a href="${URL.make("/EditDiscussion/"+relId+"?action=moveUp&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">P�esunout v��e</a>
     </#if>
     <a href="${URL.make("/EditDiscussion/"+relId+"?action=toQuestion&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Osamostatnit</a>
    </#if>
   </#if>
  </#if>
 </div>
 <#if TOOL.xpath(comment.data,"censored")?exists>
  <#if TOOL.xpath(comment.data,"censored/@admin")?exists><#local message="Cenzura: "+TOOL.xpath(comment.data,"censored")></#if>
  <p class="cenzura">
   ${message?default("Na�i administr�to�i shledali tento p��sp�vek z�vadn�m.")}
   <br>P��sp�vek si m��ete prohl�dnout <a href="${URL.make("/show?action=censored&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">zde</a>.
  </p>
  <#if USER?exists && USER.hasRole("discussion admin")>
   <a href="${URL.make("/EditDiscussion?action=censore&amp;rid="+relId+"&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Odvolat cenzuru</a>
  </#if>
 <#else>
  <div class="ds_text">
    ${TOOL.render(TOOL.element(comment.data,"text"),USER?if_exists)}
  </div>
  <#if who?exists && TOOL.xpath(who,"/data/personal/signature")?exists
    && (! USER?exists || TOOL.xpath(USER,"//settings/signatures")?default("yes")=="yes")
  ><div class="signature">${TOOL.xpath(who,"/data/personal/signature")}</div></#if>
 </#if>
</#macro>

<#macro showThread(diz level dizId relId showControls extra...)>
 <#local space=level*15>
 <div style="padding-left: ${space}pt">
  <@showComment diz, dizId, relId, showControls, extra[0]?if_exists />
 </div>
 <#if diz.children?exists>
  <#local level2=level+1>
  <#list diz.children as child>
    <@showThread child, level2, dizId, relId, showControls, extra[0]?if_exists />
  </#list>
 </#if>
</#macro>

<#macro star value><#if (value>0.60)><img src="/images/site/star1.gif" alt="*"><#elseif (value<0.2)><img src="/images/site/star0.gif" alt="-"><#else><img src="/images/site/star5.gif" alt="+"></#if></#macro>

<#macro month (month)>
    <#if month=="1">leden
    <#elseif month=="2">�nor
    <#elseif month=="3">b�ezen
    <#elseif month=="4">duben
    <#elseif month=="5">kv�ten
    <#elseif month=="6">�erven
    <#elseif month=="7">�ervenec
    <#elseif month=="8">srpen
    <#elseif month=="9">z���
    <#elseif month=="10">��jen
    <#elseif month=="11">listopad
    <#elseif month=="12">prosinec
    </#if>
</#macro>
