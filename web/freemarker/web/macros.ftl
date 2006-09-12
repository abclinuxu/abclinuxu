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
	    url=relation.url?default("/clanky/show/"+relation.id)
    >
    <#if tmp.discussion?exists><#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])></#if>
    <#if thumbnail!="UNDEF"><div class="cl_thumbnail">${thumbnail}</div></#if>
    <h1 class="st_nadpis"><a href="${url}">${TOOL.xpath(clanek,"data/name")}</a></h1>
    <p>${TOOL.xpath(clanek,"/data/perex")}</p>
    <p class="cl_inforadek">
        ${DATE.show(clanek.created, dateFormat[0])} |
        <a href="/Profile/${autor.id}">${autor.name}</a> |
        P�e�teno: <@showCounter clanek, .globals["CITACE"]?if_exists, "read" />x
        <#if diz?exists>| <@showCommentsInListing diz, dateFormat[1]?default(dateFormat[0]), "/clanky" /></#if>
        <@showShortRating relation, "| " />
    </p>
</#macro>

<#macro showCounter(item, map, type)><#rt>
    <#local value = map.get(item)?default("UNDEF")><#rt>
    <#lt><#if value?string!="UNDEF">${value}<#else>${TOOL.getCounterValue(item, type)}</#if><#rt>
</#macro>

<#macro showCommentsInListing(diz dateFormat urlPrefix)>
    <a href="${diz.url?default(urlPrefix+"/show/"+diz.relationId)}">Koment���:&nbsp;${diz.responseCount}<@markNewComments diz/></a><#rt>
    <#lt><#if diz.responseCount gt 0>, posledn�&nbsp;${DATE.show(diz.updated, dateFormat)}</#if>
</#macro>

<#macro showSoftwareList(items)>
    <#local visits = TOOL.getRelationCountersValue(items,"visit"), reads = TOOL.getRelationCountersValue(items,"read")>
    <table class="swlisting">
    <tr>
        <th>Jm�no</th>
        <th>Hodnocen�</th>
        <th>Precteno</th>
        <th>Navst�veno</th>
        <th>Posledn� �prava</th>
    </tr>
    <#list SORT.byName(items) as software>
        <tr>
            <td class="jmeno"><a href="${software.url}">${TOOL.childName(software)}</a></td>
            <td><@showShortRating software, "", false /></td>
            <td><@showCounter software.child, reads, "read" />x</td>
            <td><@showCounter software.child, visits, "visit" />x</td>
            <td>${DATE.show(software.child.updated, "SMART")}</td>
        </tr>
    </#list>
    </table>
</#macro>

<#macro showNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   autor=TOOL.createUser(ITEM.owner),
   diz=TOOL.findComments(ITEM),
   url=relation.url?default("/zpravicky/show/"+relation.id),
   title=TOOL.xpath(ITEM, "/data/title")?default("Zpr�vi�ka")
 >
    <h3>${title}</h3>
    <p>
        ${TOOL.xpath(ITEM,"data/content")}<br>
        <span style="font-size: smaller">
        <a href="/Profile/${autor.id}">${autor.name}</a> |
	    ${DATE.show(ITEM.created,"SMART")} |
        <a href="${url}">Koment��e: ${diz.responseCount}</a><#rt>
        <#lt><#if diz.responseCount gt 0><@markNewComments diz/>, posledn� ${DATE.show(diz.updated, "SMART")}</#if>
        </span>
    </p>
</#macro>

<#macro showTemplateNews(relation)>
    <#local item=TOOL.sync(relation.child),
    autor=TOOL.createUser(item.owner),
    diz=TOOL.findComments(item),
    url=relation.url?default("/zpravicky/show/"+relation.id)>
    ${DATE.show(item.created,"CZ_SHORT")} | ${NEWS_CATEGORIES[item.subType].name}
    <p>${TOOL.xpath(item,"data/content")}</p>
    <a href="/Profile/${autor.id}">${TOOL.nonBreakingSpaces(autor.name)}</a>
    | <a href="${url}" title="<#if diz.responseCount gt 0>posledn� ${DATE.show(diz.updated, "SMART")}</#if>"
    >(Koment���: ${diz.responseCount}<@lib.markNewComments diz/>)</a>
</#macro>

<#macro markNewComments(discussion)><#t>
<#if TOOL.hasNewComments(USER?if_exists, discussion)><#t>
    <span title="V diskusi jsou nov� koment��e" class="new_comment_mark">*</span><#t>
</#if><#t>
</#macro>

<#macro markNewCommentsQuestion(discussion)><#t>
<#if TOOL.hasNewComments(USER?if_exists, discussion)><#t>
    <span title="V diskusi jsou nov� koment��e" class="new_comment_state">*</span><#t>
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
	<#assign blacklisted = diz.isBlacklisted(comment)>
	<div class="ds_hlavicka<#if diz.isUnread(comment)>_novy</#if><#if blacklisted> ds_hlavicka_blacklisted</#if><#if who?exists && USER?exists && who.id == USER.id> ds_hlavicka_me</#if>">
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"SMART")}
  <#if comment.author?exists>
   <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
   <#local blog=TOOL.getUserBlogAnchor(who, "blog")?default("UNDEF")>
   <#if blog!="UNDEF">&nbsp;| blog: ${blog}</#if>
   <#local city=TOOL.xpath(who,"//personal/city")?default("UNDEF")><#if city!="UNDEF"> | ${city}</#if>
  <#else>
   ${comment.anonymName?if_exists}
  </#if><br>
  <#assign blacklisted = diz.isBlacklisted(comment)>
  <#if blacklisted>
     <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle2" class="ds_control_sbalit" title="Schov� nebo rozbal� cel� vl�kno">Rozbalit</a>
	 <#else>
     <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle2" class="ds_control_sbalit2" title="Schov� nebo rozbal� cel� vl�kno">Rozbalit</a>
  </#if>
  ${comment.title?if_exists}
  <#if showControls>
	 <div id="comment${comment.id}_controls" <#if blacklisted>class="ds_controls_blacklisted"</#if>>
         <#assign nextUnread = diz.getNextUnread(comment)?default("UNDEF")>
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
         <a onClick="schovej_vlakno(${comment.id})" id="comment${comment.id}_toggle1" title="Schov� nebo rozbal� cel� vl�kno" class="ds_control_sbalit3"><#if ! blacklisted>Sbalit<#else>Rozbalit</#if></a>
     </div>
  <#elseif USER?exists && USER.hasRole("discussion admin")>
      <a href="${URL.make("/EditRequest/"+diz.relationId+"?action=comment&amp;threadId="+comment.id)}">Admin</a>
  </#if>
 </div>
 <div id="comment${comment.id}" <#if who?exists>class="ds_text_user${who.id}"</#if> <#if blacklisted?if_exists>style="display: none;"</#if>>
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

<#macro showDiscussion(relation)>
    <#local DIZ = TOOL.createDiscussionTree(relation.child,USER?if_exists,relation.id,true)>
    <#if DIZ.monitored><#local monitorState="P�esta� sledovat"><#else><#assign monitorState="Sleduj"></#if>

    <div class="ds_toolbox">
     <b>N�stroje:</b>
       <#if DIZ.hasUnreadComments>
         <a href="#${DIZ.firstUnread}" title="Sko�it na prvn� nep�e�ten� koment��">Prvn� nep�e�ten� koment��</a>,
       </#if>
         <a href="${URL.make("/monitor/"+DIZ.relationId+"?action=toggle")}">${monitorState}</a>
           <span title="Po�et lid�, kte�� sleduj� tuto diskusi">(${DIZ.monitorSize})</span>
           <a class="info" href="#">?<span class="tooltip">Za�le ka�d� nov� koment�� emailem na va�i adresu</span></a>,
         <a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print">Tisk</a>
       <#if USER?exists && USER.hasRole("discussion admin")>
         <br />
         <b>Admin:</b>
         <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+DIZ.relationId+"&amp;dizId="+DIZ.id)}">
            <#if DIZ.frozen>Rozmrazit<#else>Zmrazit</#if></a>
       </#if>
    </div>

    <p>
        <#if DIZ.frozen>
            Diskuse byla administr�tory uzam�ena
        <#else>
            <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DIZ.id+"&amp;threadId=0&amp;rid="+DIZ.relationId)}">
            Vlo�it dal�� koment��</a>
        </#if>
    </p>

    <#list DIZ.threads as thread>
       <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
    </#list>

    <#if (!DIZ.frozen && DIZ.size>3)>
     <p><a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+DIZ.id+"&amp;rid="+DIZ.relationId)}">
     Zalo�it nov� vl�kno</a></p>
    </#if>
</#macro>

<#macro showRating (relation heading=true boxCssClass="cl_rating")>
    <#local rating=TOOL.ratingFor(relation.child.data)?default("UNDEF"), width = 0>
    <#if rating!="UNDEF"><#local width = (rating.percent / 100) * 78 ></#if>
    <div class="${boxCssClass}">
        <#if heading>
            <span class="nadpis">
                Hodnocen�:
            </span>
        </#if>
        <#if rating!="UNDEF">
            <span class="stupnice">
                <img src="/images/site2/teplomerrtut.gif" alt="" height="5" width="${width?string["0"]}">
            </span>
            <span title="Po�et hlas�: ${rating.count}">${rating.percent}%</span>
        <#else>
            nehodnoceno
        </#if>
        <span class="hlasovani">
            Hlasujte:
            <#if USER?exists>
                <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3")}" target="rating">dobr�</a>
                <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0")}" target="rating">�patn�</a>
            <#else>
                <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=3&amp;return=true")}" rel="nofollow">dobr�</a>
                <a href="${URL.make("/rating/"+relation.id+"?action=rate&amp;rvalue=0&amp;return=true")}" rel="nofollow">�patn�</a>
            </#if>
        </span>
        <iframe name="rating" width="300" frameborder="0" height="20" scrolling="no" class="rating_iframe"></iframe>
    </div>
</#macro>

<#macro showShortRating (relation, separator, heading=true)>
    <#local rating=TOOL.ratingFor(relation.child.data)?default("UNDEF")>
    <#if rating!="UNDEF"><#if heading>${separator}Hodnocen�:&nbsp;</#if><span title="Hlas�: ${rating.count}">${rating.percent}%</span></#if>
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

<#macro showRelated (item)>
    <#assign related = TOOL.getRelatedDocuments(item)>
    <#if (related?size > 0)>
        <div class="cl_perex souvisejici">
            <h3>Souvisej�c� dokumenty</h3>
            <dl>
            <#list related as link>
                <dt>
                    <a href="${link.url}">${link.title}</a>
                    <#if link.type=='article'>(�l�nek)
                    <#elseif link.type=='content'>(dokument)
                    <#elseif link.type=='dictionary'>(pojem)
                    <#elseif link.type=='discussion'>(diskuse)
                    <#elseif link.type=='driver'>(ovlada�)
                    <#elseif link.type=='external'>(extern� dokument)
                    <#elseif link.type=='faq'>(FAQ)
                    <#elseif link.type=='hardware'>(hardware)
                    <#elseif link.type=='news'>(zpr�vi�ka)
                    <#elseif link.type=='other'>(ostatn�)
                    <#elseif link.type=='poll'>(anketa)
                    <#elseif link.type=='section'>(sekce)
                    <#--<#elseif link.type=='software'>(software)-->
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
        <input type="${type}" name="${param}" value="${value}"<#if TOOL.isWithin(PARAMS[param], value)> checked</#if>${extra[0]?if_exists}>
        ${caption}
    </label>
</#macro>

<#macro showOption2 (param value caption type values)>
    <label>
        <input type="${type}" name="${param}" value="${value}"<#if TOOL.isWithin(values, value)> checked</#if>> ${caption}
    </label>
</#macro>

