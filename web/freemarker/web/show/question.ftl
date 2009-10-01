<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER!,RELATION.id,true)>
<#if SUBPORTAL??>
    <#import "../macros.ftl" as lib>
    <#assign plovouci_sloupec>
        <@lib.showSubportal SUBPORTAL, true/>
    </#assign>
</#if>

<#assign html_header>
    <script type="text/javascript" src="/data/site/solutions.js"></script>
</#assign>

<#include "../header.ftl">

<@lib.advertisement id="arbo-sq" />

<@lib.showMessages/>

<h1>Dotaz: ${ITEM.title}</h1>

<@lib.showThread TOOL.createComment(ITEM), 0, DIZ, !DIZ.frozen, "question">
    <div>
        Přečteno: ${TOOL.getCounterValue(ITEM, "read")}&times;
    </div>
</@lib.showThread>

<#--
<div>
    Problém <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true"+TOOL.ticket(USER!, false))}" rel="nofollow">byl</a>
    (${TOOL.xpath(ITEM,"//solved/@yes")!"0"}) /
    <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false"+TOOL.ticket(USER!, false))}" rel="nofollow">nebyl</a>
    (${TOOL.xpath(ITEM,"//solved/@no")!"0"}) vyřešen
    <@lib.showHelp>Kliknutím na příslušný odkaz zvolte, jestli otázka <i>byla</i> nebo <i>nebyla</i> vyřešena.</@lib.showHelp>
</div>
-->

<#if TOOL.isQuestionSolved(ITEM)>
  <hr />
    <div class="ds_solutions">
        <p><b>Řešení dotazu:</b></p>
        <ul>
        <#list DIZ.solutions.entrySet() as sol>
            <#assign comment=sol.value>
            <#if comment.author??><#assign author=TOOL.createUser(comment.author)></#if>
            <li><#--
             --><a href="#${sol.key.id}">Komentář #${sol.key.id}</a> (<#--
                --><#if comment.author??><@lib.showUser author /><#else>${comment.anonymName!}</#if>, <#--
                -->${sol.key.getVotes()} hlasů)<#--
         --></li>
        </#list>
        </ul>
    </div>
  <hr />
</#if>

<@lib.advertisement id="gg-ds-otazka" />

<div class="ds_toolbox">
    <b>Nástroje:</b>
    <#if DIZ.hasUnreadComments && DIZ.firstUnread??>
        <a href="#${DIZ.firstUnread}" title="Skočit na první nepřečtený komentář" rel="nofollow">První nepřečtený komentář</a>
    </#if>

    <@lib.showMonitor RELATION "Zašle upozornění na váš email při vložení nového komentáře."/>
    <@lib.showAdminTools RELATION DIZ.frozen />
</div>

<#if DIZ.size==0>
    <p>Na otázku zatím nikdo bohužel neodpověděl.</p>
<#else>
    <h2>Odpovědi</h2>
</#if>

<#if DIZ.frozen>
    <p class="error">
        Diskuse byla administrátory uzamčena.<br />
        FAQ: <a href="/faq/abclinuxu.cz/proc-byl-uzamcen-smazan-muj-dotaz-v-poradne">Proč byl uzamčen/smazán můj dotaz v Poradně?</a>
    </p>
</#if>

<#list DIZ.threads as thread>
   <@lib.showThread thread, 0, DIZ, !DIZ.frozen, "reply" />
</#list>

<#if (!DIZ.frozen)>
    <p>
        <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}" rel="nofollow">
        Založit nové vlákno</a> &#8226;
        <a href="#www-abclinuxu-cz">Nahoru</a>
    </p>
</#if>

<@lib.advertisement id="obsah-box" />
<@lib.advertisement id="arbo-full" />

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
