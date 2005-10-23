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
        autor=TOOL.createUser(TOOL.xpath(clanek,"/data/author")?default("5473")),
        thumbnail=TOOL.xpath(clanek,"/data/thumbnail")?default("UNDEF"),
        tmp=TOOL.groupByType(clanek.children, "Item"),
        rating=TOOL.ratingFor(clanek.data,"article")?default("UNDEF"),
	    url=relation.url?default("/clanky/show/"+relation.id),
	    ctennost=.globals["CITACE"]?if_exists(clanek)?default("UNDEF")
    >
    <#if tmp.discussion?exists><#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>
    <#if thumbnail!="UNDEF"><div style="float:right;margin:0.8em 0.4em 0 0.4em">${thumbnail}</div></#if>
    <h1 class="st_nadpis"><a href="${url}">${TOOL.xpath(clanek,"data/name")}</a></h1>
    <p>${TOOL.xpath(clanek,"/data/perex")}</p>
    <p class="cl_inforadek">${DATE.show(clanek.created, dateFormat[0])} |
        <a href="/Profile/${autor.id}">${autor.name}</a> |
        P�e�teno: <#if ctennost?string!="UNDEF">${ctennost}<#else>${TOOL.getCounterValue(clanek)}</#if>x
        <#if diz?exists>
            | <a href="${diz.url?default("/clanky/show/"+diz.relationId)}">Koment���:&nbsp;${diz.responseCount}</a
            ><#if diz.responseCount gt 0>, posledn�&nbsp;${DATE.show(diz.updated, dateFormat[1]?default(dateFormat[0]))}</#if>
        </#if>
        <#if rating!="UNDEF">| Hodnocen�:&nbsp;<span title="Hlas�: ${rating.count}">${rating.result?string["#0.00"]}</span></#if>
    </p>

</#macro>

<#macro showNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   autor=TOOL.createUser(ITEM.owner),
   diz=TOOL.findComments(ITEM),
   url=relation.url?default("/zpravicky/show/"+relation.id)
 >
 <p>${DATE.show(ITEM.created,"CZ_FULL")}
 <a href="/Profile/${autor.id}">${autor.name}</a><br>
 ${TOOL.xpath(ITEM,"data/content")}<br>
 <span style="font-size: smaller">
  <a href="${url}">Link</a>
  Koment��e: ${diz.responseCount}<#if diz.responseCount gt 0>, posledn� ${DATE.show(diz.updated, "CZ_FULL")}</#if>
 </span>
 </p>
</#macro>

<#macro separator double=false>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="--------------------"><br>
 <#if double><img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="" vspace="1"><br></#if>
</#macro>

<#macro showComment(comment dizId relId showControls extra...) >
prosim poslete mi URL a popis, jak jste na tuto stranku narazili
</#macro>

<#macro showThread(comment level dizId relId showControls extra...)>
 <div class="ds_hlavicka<#if comment.unread>_novy</#if>">
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"CZ_FULL")}
  <#if comment.author?exists>
   <#local who=TOOL.sync(comment.author)><a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
   <#local blog=TOOL.getUserBlogAnchor(who, "blog")?default("UNDEF")>
   <#if blog!="UNDEF">&nbsp;| blog: ${blog}</#if>
   <#local city=TOOL.xpath(who,"//personal/city")?default("UNDEF")><#if city!="UNDEF"> | ${city}</#if>
  <#else>
   ${TOOL.xpath(comment.data,"author")?if_exists}
  </#if><br>
  ${TOOL.xpath(comment.data,"title")?if_exists}<br>
  <#if showControls>
   <#if comment.nextUnread?exists><a href="#${comment.nextUnread}" title="Sko�it na dal�� nep�e�ten� koment��">Dal��</a> |</#if>
   <a href="${URL.make("/EditDiscussion/"+relId+"?action=add&amp;dizId="+dizId+"&amp;threadId="+comment.id+extra[0]?default(""))}">Odpov�d�t</a> |
   <a href="${URL.make("/EditRequest/"+relId+"?action=comment&amp;threadId="+comment.id)}" title="��dost o p�esun diskuse, st�nost na koment��">Admin</a> |
   <a href="#${comment.id}" title="P��m� adresa na tento koment��">Link</a> |
   <#if (comment.parent>0)><a href="#${comment.parent}" title="Odkaz na koment�� o jednu �rove� v��e">V��e</a> |</#if>
   <a onClick="schovej_vlakno(${comment.id})" id="a${comment.id}" title="Schov� nebo rozbal� cel� vl�kno">Sbalit</a>
  </#if>
 </div>
 <div id="div${comment.id}">
  <#if TOOL.xpath(comment.data,"censored")?exists>
     <@showCensored comment, dizId, relId/>
  <#else>
   <div class="ds_text">
     ${TOOL.render(TOOL.element(comment.data,"text"),USER?if_exists)}
   </div>
   <#assign signature = TOOL.getUserSignature(who?if_exists, USER?if_exists)?default("UNDEFINED")>
   <#if signature!="UNDEFINED"><div class="signature">${signature}</div></#if>
  </#if>
  <#local level2=level+1>
  <div style="padding-left: 15pt">
   <#list comment.children?if_exists as child>
    <@showThread child, level2, dizId, relId, showControls, extra[0]?if_exists />
   </#list>
  </div>
 </div>
</#macro>

<#macro showCensored(comment dizId relId)>
    <p class="cenzura">
        <#assign admin = TOOL.xpath(comment.data,"censored/@admin")?default("UNDEFINED")>
        N� <#if admin!="UNDEFINED"><a href="/Profile/${admin}"></#if>administr�tor<#if admin!="UNDEFINED"></a></#if>
        shledal tento p��sp�vek z�vadn�m nebo nevyhovuj�c�m zam��en� port�lu.
        <#assign message = TOOL.xpath(comment.data,"censored")?if_exists>
        <#if message?has_content><br>${message}</#if>
        <br><a href="${URL.make("/show?action=censored&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Zobrazit</a> p��sp�vek
    </p>
    <#if USER?exists && USER.hasRole("discussion admin")>
        <a href="${URL.make("/EditDiscussion?action=censore&amp;rid="+relId+"&amp;dizId="+dizId+"&amp;threadId="+comment.id)}">Odvolat cenzuru</a>
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
