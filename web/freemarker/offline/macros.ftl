<#macro showArticle(relation)>
 <#local clanek=relation.child,
        autor=TOOL.createUser(TOOL.xpath(clanek,"/data/author")),
        tmp=TOOL.groupByType(clanek.children)>
 <#if tmp.discussion?exists>
  <#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
 </#if>
 <h2 class="st_nadpis"><a href="../../${DUMP.getFile(relation.id)}">${TOOL.xpath(clanek,"data/name")}</a></h2>
 <p>
  <span class="barva">
   ${DATE.show(clanek.created, "CZ_DMY")} |
   <a href="http://www.abclinuxu.cz/Profile/${autor.id}">${autor.name}</a>
  </span>
 </p>
 <p class="perex_out">
  ${TOOL.xpath(clanek,"/data/perex")}
 </p>
 <p class="barva">
  Pøeèteno: ${TOOL.getCounterValue(clanek)}x
  <#if diz?exists>
   | <a href="../../${DUMP.getFile(diz.relationId)}">Komentáøù: ${diz.responseCount}</a>
  </#if>
 </p>
</#macro>

<#macro separator double=false>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="--------------------"><br>
 <#if double><img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="" vspace="1"><br></#if>
</#macro>

<#macro doubleSeparator>
 <@separator double=true/>
</#macro>

<#macro showParents>
 <table border="0" width="100%">
  <tr>
   <td>
    <#if PARENTS?exists>
     <#list PARENTS as relation>
      <a href="../${DUMP.getFile(relation.id)}">${TOOL.childName(relation)}</a><#if relation_has_next> - </#if>
     </#list>
    </#if>
   </td>
   <td align="right">
    <a href="${ONLINE}"><img src="../../images/tl-online.gif" width="59" height="23" border="0" alt="online"></a>
   </td>
  </tr>
 </table>
</#macro>

<#macro showThread(comment level dizId relId showControls extra...)>
 <div class="ds_hlavicka">
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"CZ_FULL")}
  <#if comment.author?exists>
   <#local who=TOOL.sync(comment.author)>
   <a href="http://www.abclinuxu.cz/Profile//Profile/${who.id}">${who.nick?default(who.name)}</a>
   <#local city=TOOL.xpath(who,"//personal/city")?default("UNDEF")><#if city!="UNDEF"> | ${city}</#if>
  <#else>
   ${TOOL.xpath(comment.data,"author")?if_exists}
  </#if><br>
  ${TOOL.xpath(comment.data,"title")?if_exists}<br>
  <#if (comment.parent>0)><a href="#${comment.parent}" title="Skoèit na komentáø o jednu úroveò vý¹e">Vý¹e</a> |</#if>
  <a onClick="schovej_vlakno(${comment.id})" id="a${comment.id}" title="Schová nebo rozbalí celé vlákno">Sbalit</a>
 </div>
 <div id="div${comment.id}">
  <#if TOOL.xpath(comment.data,"censored")?exists>
     <@showCensored comment, dizId, relId/>
  <#else>
   <div class="ds_text">
     ${TOOL.render(TOOL.element(comment.data,"text"),USER?if_exists)}
   </div>
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
        Ná¹ administrátor shledal tento pøíspìvek závadným nebo nevyhovujícím zamìøení portálu.
        <#assign message = TOOL.xpath(comment.data,"censored")?if_exists>
        <#if message?has_content><br>${message}</#if>
    </p>
</#macro>

<#macro month (month)>
    <#if month=="1">leden
    <#elseif month=="2">únor
    <#elseif month=="3">bøezen
    <#elseif month=="4">duben
    <#elseif month=="5">kvìten
    <#elseif month=="6">èerven
    <#elseif month=="7">èervenec
    <#elseif month=="8">srpen
    <#elseif month=="9">záøí
    <#elseif month=="10">øíjen
    <#elseif month=="11">listopad
    <#elseif month=="12">prosinec
    </#if>
</#macro>

<#macro listPages (result, rid)>
    <p>
        Stránky:
        <#list 0..(result.pageCount-1) as page>
            <#if page!=result.pageIndex>
                <a href="../../${DUMP.getFile(rid, page)}">${page}</a>
            <#else>
                ${page+1}
            </#if>
        </#list>
    </p>
</#macro>