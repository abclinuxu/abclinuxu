<#import "../macros.ftl" as lib>
<#if RELATION.upper==250>
    <#assign plovouci_sloupec>
    <div class="s_nadpis"><a href="/nej">Nej anket na AbcLinuxu</a></div>
    <div class="s_sekce">
        <#if VARS.mostVotedOnPolls??>
            <b>Ankety s nejvíce hlasy</b>
            <ul>
                <#list VARS.mostVotedOnPolls.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url?default("/ankety/show/"+rel.key.id)}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>

        <#if VARS.mostCommentedPolls??>
            <b>Nejkomentovanější ankety</b>
            <ul>
                <#list VARS.mostCommentedPolls.entrySet() as rel>
                    <#if rel_index gt 2><#break></#if>
                    <li><a href="${rel.key.url?default("/ankety/show/"+rel.key.id)}">${TOOL.childName(rel.key)}</a></li>
                </#list>
            </ul>
        </#if>
    </div>
    </#assign>
</#if>

<#include "../header.ftl">

<@lib.showMessages/>

<@lib.advertisement id="arbo-sq" />

<#if USER?? && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.noPrefix("/EditPoll/"+RELATION.id+"?action=edit&amp;pollId="+POLL.id)}">Upravit</a>
 </p>
</#if>


<p><b>${POLL.text}</b></p>

<table class="ank" border="0" cellpadding="3">
 <#list POLL.choices as choice>
  <#assign procento=TOOL.percent(choice.count,POLL.totalVoters)>
  <tr>
   <td>${choice.text}</td>
   <td>
     <div style="width: ${procento}px" class="ank-sloup-okraj">
       <div class="ank-sloup"></div>
     </div>
   </td>
   <td>${procento}% (${choice.count})</td>
  </tr>
 </#list>
</table>

<p>Celkem ${POLL.totalVoters} hlasů<br />
Vytvořeno: ${DATE.show(POLL.created, "CZ_FULL")}</p>

<@lib.showPageTools RELATION />

<#if CHILDREN.discussion??>
    <h3>Komentáře</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#elseif forbidDiscussion?default("yes") != "yes">
    <h3>Diskuse k tomuto článku</h3>
    <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<@lib.advertisement id="arbo-full" />

<#include "../footer.ftl">
