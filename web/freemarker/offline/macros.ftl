<#macro showArticle(relation)>
 <#local clanek=relation.child,
        autor=TOOL.createUser(TOOL.xpath(clanek,"/data/author")),
        tmp=TOOL.groupByType(clanek.children)>
 <#if tmp.discussion??>
  <#local diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
 </#if>
 <h1 class="st_nadpis"><a href="../../${DUMP.getFile(relation.id)}">${TOOL.xpath(clanek,"data/name")}</a></h1>
 <p>${TOOL.xpath(clanek,"/data/perex")}</p>
 <p class="cl_inforadek">
   ${DATE.show(clanek.created, "CZ_DMY")} |
   <a href="http://www.abclinuxu.cz/Profile/${autor.id}">${autor.name}</a> |
   Přečteno: ${TOOL.getCounterValue(clanek,"read")}x
  <#if diz??>
   | <a href="../../${DUMP.getFile(diz.relationId)}">Komentářů:&nbsp;${diz.responseCount}</a>
  </#if>
 </p>
</#macro>

<#macro separator double=false>
 <p>------------------</p>
 <#if double>
   <p>==================</p>
 </#if>
</#macro>

<#macro doubleSeparator>
 <@separator double=true/>
</#macro>

<#macro showParents>
  <div class="hl_vpravo">
    <a href="${ONLINE?default("http://www.abclinuxu.cz")}">online</a>
  </div>
  <div class="hl_vlevo">
    <#if PARENTS??>
     <#list PARENTS as relation>
      <a href="../../${DUMP.getFile(relation.id)}">${TOOL.childName(relation)}</a><#if relation_has_next> - </#if>
     </#list>
    </#if>
  &nbsp;</div>
</#macro>

<#macro showThread(comment level dizId relId showControls extra...)>
 <div class="ds_hlavicka">
  <a name="${comment.id}"></a>
  ${DATE.show(comment.created,"CZ_FULL")}
  <#if comment.author??>
   <#local who=TOOL.sync(comment.author)>
   <a href="http://www.abclinuxu.cz/Profile//Profile/${who.id}">${who.nick?default(who.name)}</a>
   <#local city=TOOL.xpath(who,"//personal/city")!"UNDEF"><#if city!="UNDEF"> | ${city}</#if>
  <#else>
   ${TOOL.xpath(comment.data,"author")!}
  </#if><br>
  ${TOOL.xpath(comment.data,"title")!}<br>
  <#if (comment.parent>0)><a href="#${comment.parent}" title="Skočit na komentář o jednu úroveň výše">Výše</a> |</#if>
  <a onClick="schovej_vlakno(${comment.id})" id="a${comment.id}" title="Schová nebo rozbalí celé vlákno">Sbalit</a>
 </div>
 <div id="div${comment.id}">
  <#if TOOL.xpath(comment.data,"censored")??>
     <@showCensored comment, dizId, relId/>
  <#else>
   <div class="ds_text">
     ${TOOL.render(TOOL.element(comment.data,"text"),USER!)}
   </div>
  </#if>
  <#local level2=level+1>
  <div style="padding-left: 15pt">
   <#list comment.children! as child>
    <@showThread child, level2, dizId, relId, showControls, extra[0]! />
   </#list>
  </div>
 </div>
</#macro>

<#macro showCensored(comment dizId relId)>
    <p>
        Náš administrátor shledal tento příspěvek závadným nebo nevyhovujícím zaměření portálu.
        <#assign message = TOOL.xpath(comment.data,"censored")!>
        <#if message?has_content><br>${message}</#if>
    </p>
</#macro>

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

<#macro listPages (result, rid)>
    <p>
        Stránky:
        <#list 0..(result.pageCount-1) as page>
            <#if page!=result.pageIndex>
                <a href="../../${DUMP.getFile(rid, page)}">${page+1}</a>
            <#else>
                ${page+1}
            </#if>
        </#list>
    </p>
</#macro>