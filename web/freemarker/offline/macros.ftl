<#macro showArticle(relation)>
 <#local clanek=relation.child>
 <#local autor=TOOL.createUser(TOOL.xpath(clanek,"/data/author"))>
 <#local tmp=TOOL.groupByType(clanek.content)>
 <#if tmp.discussion?exists>
  <#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
 </#if>
 <p>
  <a class="nadpis_out" href="../${DUMP.getFile(relation.id)}">
   ${TOOL.xpath(clanek,"data/name")}
  </a><br>
  <span class="barva">
   ${DATE.show(clanek.created, "CZ_FULL")} |
   <a href="http://www.abclinuxu.cz/Profile?userId=${autor.id}">${autor.name}</a>
  </span>
 </p>
 <p class="perex_out">
  ${TOOL.xpath(clanek,"/data/perex")}
 </p>
 <p class="barva">
  Pøeèteno: ${TOOL.getCounterValue(clanek)}x
  <#if diz?exists>
   | <a href="../${DUMP.getFile(diz.relationId)}">Komentáøù: ${diz.responseCount}</a>
   <#if diz.responseCount gt 0>, poslední ${DATE.show(diz.lastUpdate, "CZ_FULL")}</#if>
  </#if>
 </p>
</#macro>

<#macro separator>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="---------------------------">
</#macro>

<#macro doubleSeparator>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt=""><br>
 <img src="/images/site/sedybod.gif" width="100%" height="1" border="0" alt="" vspace="1"><br>
</#macro>

<#macro showDiscussions(dizs)>
 <table width="100%" cellspacing="0" cellpadding="1" border="0">
  <tr>
   <td colspan="3" class="cerna2">
    <strong>Diskuse</strong>
   </td>
  </tr>
  <tr>
   <td class="seda1"><span class="piditucna">Dotaz</span></td>
   <td class="seda1" align="center"><span class="piditucna">Odpovìdí</span></td>
   <td class="seda1" align="center"><span class="piditucna">Poslední</span></td>
  </tr>
  <#list SORT.byDate(TOOL.analyzeDiscussions(dizs),"DESCENDING") as diz>
   <tr>
    <td>
     <a href="../${DUMP.getFile(diz.relationId)}">${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a>
    </td>
    <td align="center"><span class="pidi">${diz.responseCount}</span></td>
    <td align="center"><span class="pidi">${DATE.show(diz.lastUpdate,"CZ_FULL")}</span></td>
   </tr>
   <tr><td colspan="3"><#if diz_has_next><#call separator><#else><#call doubleSeparator></#if></td></tr>
  </#list>

 </table>
</#macro>

<#macro showComment(reaction dizId relId showControls) >
 <p class="diz_header">
  <span class="diz_header_prefix">Datum:</span> ${DATE.show(reaction.updated,"CZ_FULL")}<br>
  <span class="diz_header_prefix">Od:</span>
  <#if reaction.owner!=0>
   <#local who=TOOL.createUser(reaction.owner)>
   <a href="http://www.abclinuxu.cz/Profile?userId=${who.id}">${who.name}</a><br>
  <#else>
   ${TOOL.xpath(reaction,"data/author")}<br>
  </#if>
  <span class="diz_header_prefix">Titulek:</span> ${TOOL.xpath(reaction,"data/title")}<br>
 </p>
 <p>${TOOL.render(TOOL.xpath(reaction,"data/text"))}</p>
</#macro>

<#macro showThread(diz level dizId relId)>
 <#local space=level*15>
 <div style="padding-left: ${space}pt">
  <#call showComment(diz.record dizId relId true)>
 </div>
 <#if diz.list?exists>
  <#list diz.list as child>
   <#local level2=level+1>
   <#call showThread(child level2 dizId relId)>
  </#list>
 </#if>
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

<#macro showPoll(poll url)>
</#macro>
