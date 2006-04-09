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
    <#if thumbnail!="UNDEF"><div class="cl_thumbnail">${thumbnail}</div></#if>
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

<#macro showThread(comment level diz showControls extra...)>
 <div class="ds_hlavicka<#if diz.isUnread(comment)>_novy</#if>">
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"CZ_FULL")}
  <#if comment.author?exists>
   <#local who=TOOL.createUser(comment.author)><a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
   <#local blog=TOOL.getUserBlogAnchor(who, "blog")?default("UNDEF")>
   <#if blog!="UNDEF">&nbsp;| blog: ${blog}</#if>
   <#local city=TOOL.xpath(who,"//personal/city")?default("UNDEF")><#if city!="UNDEF"> | ${city}</#if>
  <#else>
   ${comment.anonymName?if_exists}
  </#if><br>
  ${comment.title?if_exists}<br>
  <#if showControls>
   <#assign nextUnread = diz.getNextUnread(comment)?default("UNDEF"), blacklisted = diz.isBlacklisted(comment)>
   <#if ! nextUnread?is_string><a href="#${nextUnread}" title="Sko�it na dal�� nep�e�ten� koment��">Dal��</a> |</#if>
   <a href="${URL.make("/EditDiscussion/"+diz.relationId+"?action=add&amp;dizId="+diz.id+"&amp;threadId="+comment.id+extra[0]?default(""))}">Odpov�d�t</a> |
   <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}" title="��dost o p�esun diskuse, st�nost na koment��">Admin</a> |
   <a href="#${comment.id}" title="P��m� adresa na tento koment��">Link</a> |
   <#if (comment.parent?exists)><a href="#${comment.parent}" title="Odkaz na koment�� o jednu �rove� v��e">V��e</a> |</#if>
   <#if comment.author?exists>
       <#if blacklisted><#local action="fromBlacklist", title="Neblokovat", hint="Odstran� autora ze seznamu blokovan�ch u�ivatel�">
       <#else><#local action="toBlacklist", title="Blokovat", hint="P�id� autora na seznam blokovan�ch u�ivatel�"></#if>
       <#if USER?exists><#local myId=USER.id></#if>
       <a href="${URL.noPrefix("/EditUser/"+myId?if_exists+"?action="+action+"&amp;bUid="+who.id+"&amp;url="+URL.prefix+"/show/"+diz.relationId+"#"+comment.id)}" title="${hint}">${title}</a> |
   </#if>
   <a onClick="schovej_vlakno(${comment.id})" id="a${comment.id}" title="Schov� nebo rozbal� cel� vl�kno"><#if ! blacklisted>Sbalit<#else>Rozbalit</#if></a>
  </#if>
 </div>
 <div id="div${comment.id}" <#if blacklisted?if_exists>style="display: none;"</#if>>
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
        N� <a href="/Profile/${admin}">administr�tor</a>
        shledal tento p��sp�vek z�vadn�m nebo nevyhovuj�c�m zam��en� port�lu.
        <#assign message = TOOL.xpath(comment.data,"//censored")?default("")>
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

<#macro showPoll (relation url=relation.url?default("/ankety/show/"+relation.id))>
    <#assign anketa = relation.child, total = anketa.totalVoters>
    <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>
    <div>
        <form action="${URL.noPrefix("/EditPoll/"+relation.id)}" method="POST">
        <div class="ank-otazka">${anketa.text}</div>
        <#list anketa.choices as choice>
            <div class="ank-odpov">
              <#assign procento = TOOL.percent(choice.count,total)>
              <label><input type="${type}" name="voteId" value="${choice.id}">${choice.text}</label>&nbsp;(<span title="${choice.count} hlas�">${procento}%</span>)<br>
              <div class="ank-sloup-okraj" style="width: ${procento}px">
                <div class="ank-sloup"></div>
              </div>
            </div>
        </#list>
        <input name="submit" type="submit" class="button" value="Hlasuj" src="/images/site2/vote_btn.gif" alt="Hlasuj">
        Celkem ${total} hlas�<br>
        <input type="hidden" name="url" value="${url}">
        <input type="hidden" name="action" value="vote">
        </form>
    </div>
</#macro>