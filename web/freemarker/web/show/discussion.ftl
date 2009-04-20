<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER!,RELATION.id,true)>
<#assign is_question=TOOL.isQuestion(RELATION)>
<#if SUBPORTAL??>
    <#import "../macros.ftl" as lib>
    <#assign plovouci_sloupec>
        <@lib.showSubportal SUBPORTAL, true/>
    </#assign>
</#if>

<#include "../header.ftl">

<#if !is_question>
 <@lib.advertisement id="gg-ds-obsah" />
 <@lib.advertisement id="obsah-box" />
</#if>

<@lib.advertisement id="arbo-sq" />

<@lib.showMessages/>

<div class="ds_toolbox">
 <b>Nástroje:</b>
   <#if DIZ.hasUnreadComments && DIZ.firstUnread??>
     <a href="#${DIZ.firstUnread}" title="Skočit na první nepřečtený komentář" rel="nofollow">První nepřečtený komentář</a>,
   </#if>
    <@lib.showMonitor RELATION "Zašle upozornění na váš email při vložení nového komentáře."/>,
   <#if is_question>
     Otázka <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true"+TOOL.ticket(USER!, false))}" rel="nofollow">byla</a>
        (${TOOL.xpath(ITEM,"//solved/@yes")?default("0")}) /
     <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false"+TOOL.ticket(USER!, false))}" rel="nofollow">nebyla</a>
        (${TOOL.xpath(ITEM,"//solved/@no")?default("0")}) vyřešena
        <a class="info" href="#">?<span class="tooltip">Kliknutím na příslušný odkaz zvolte, jestli otázka <i>byla</i> nebo <i>nebyla</i> vyřešena.</span></a>,
   </#if>
   <#if USER?? && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
     <br />
     <b>Admin:</b>
     <a href="/SelectRelation?prefix=/forum&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">Přesunout</a>,
     <#if USER.hasRole("discussion admin")>
         <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Smazat</a>,
         <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id+TOOL.ticket(USER, false))}">
            <#if DIZ.frozen>Rozmrazit<#else>Zmrazit</#if>
         </a>
     </#if>
   </#if>
</div>

<#if is_question>
 <h1>Otázka</h1>
 <@lib.showThread TOOL.createComment(ITEM), 0, DIZ, !DIZ.frozen>
     <br>
     Přečteno: ${TOOL.getCounterValue(ITEM, "read")}&times;
 </@lib.showThread>

    <p class="questionToFaq">
        Už jste tuto otázku viděli? Ptají se na ni čtenáři často? Pak by asi bylo vhodné
        uložit vzorovou odpověď do <a href="/faq">Často kladených otázek (FAQ)</a>.
    </p>

 <@lib.advertisement id="obsah-box" />
 <@lib.advertisement id="bsupport-box" />
 <@lib.advertisement id="miton-box" />
 <@lib.advertisement id="gg-ds-otazka" />

 <#if DIZ.size==0>
    <p>Na otázku zatím nikdo bohužel neodpověděl.</p>
 <#else>
     <h2>Odpovědi</h2>
 </#if>
<#elseif !DIZ.frozen>
 <br />
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}" rel="nofollow">
 Vložit další komentář</a>
</#if>

<#if DIZ.frozen><p class="error">Diskuse byla administrátory uzamčena.<br />
FAQ: <a href="/faq/abclinuxu.cz/proc-byl-uzamcen-smazan-muj-dotaz-v-poradne">Proč byl uzamčen/smazán můj dotaz v Poradně?</a></p></#if>

<#list DIZ.threads as thread>
   <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
</#list>

<#if (!DIZ.frozen)>
    <p>
        <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}" rel="nofollow">
        Založit nové vlákno</a> &#8226;
        <a href="#www-abclinuxu-cz">Nahoru</a>
    </p>
</#if>

<@lib.advertisement id="arbo-full" />

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
