<#import "../macros.ftl" as lib>
<#if SUBPORTAL?exists>
    <#assign plovouci_sloupec>
        <@lib.showSubportal SUBPORTAL, true/>
    </#assign>
</#if>

<#include "../header.ftl">

<#assign autors=TOOL.createAuthorsForArticle(RELATION.getChild()),
         forbidRating=TOOL.xpath(ITEM, "//forbid_rating")?default("UNDEF"),
         forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")?default("UNDEF"),
         inPool=RELATION.upper==8082>

<#if PAGES?exists && PAGE?exists>
    <h1>${PAGES[PAGE]}</h1>
<#else>
    <h1>${ITEM.title}</h1>
</#if>

<p class="meta-vypis">
    ${DATE.show(ITEM.created,"SMART_DMY")} |
    <#if autors?size gt 0>
      <#list autors as autor>
          <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
      </#list>
      <#assign subportal_article=false>
    <#else>
        <@lib.showUser TOOL.createUser(ITEM.owner)/>
        <#assign subportal_article=true>
    </#if>
</p>

<#if inPool>
    <h2>Rubrika:
        <#assign section=TOOL.xpath(ITEM, "/data/section_rid")?default("UNDEFINED")>
        <#if section=="UNDEFINED">
            nezadána
        <#else>
            ${TOOL.childName(section)}
        </#if>
        </b>
    </h2>
</#if>

<#if USER?exists && TOOL.permissionsFor(USER, RELATION).canModify()>
    <p>
        <a href="${URL.noPrefix("/clanky/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>

        <#if autors?size gt 0>
            <#if SERIES?exists>
                <#if ! inPool>
                    <a href="${URL.noPrefix("/serialy/edit/"+SERIES.series.id+"?action=rmArticle&amp;articleRid="+RELATION.id+TOOL.ticket(USER, false))}">Vyřadit ze seriálu</a>
                </#if>
            <#else>
                <#if inPool>
                    <a href="${URL.noPrefix("/clanky/edit/"+RELATION.id+"?action=addSeries")}">Přiřadit k seriálu</a>
                <#else>
                    <a href="${URL.noPrefix("/serialy/edit?action=addArticle&amp;articleRid="+RELATION.id)}">Přiřadit k seriálu</a>
                </#if>
            </#if>
            <#if !CHILDREN.royalties?exists>
                <b><a class="error" href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vložit honorář</a></b>
            <#else>
                <a href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vložit honorář</a>
                <#list CHILDREN.royalties as honorar>
                    <a href="${URL.make("/honorare/"+honorar.id+"?action=edit")}">Upravit honorář</a>
                </#list>
            </#if>
            <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&amp;url=/EditRelation&action=move&amp;rid="+RELATION.id)}">Přesunout</a>
            <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
                <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/clanky&amp;rid="+RELATION.id)}">Smazat</a>
            </#if>
        <#else>
            <#if TOOL.permissionsFor(USER, RELATION).canDelete()>
                <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=&amp;rid="+RELATION.id)}">Smazat</a>
            </#if>
            <#if USER.hasRole("root") && subportal_article>
                <a href="${URL.noPrefix("/clanky/edit/"+RELATION.id+"?action=toggleHP")}"><#if ITEM.subType?exists && ITEM.subType=="SUBPORTAL">Zobrazovat<#else>Nezobrazovat</#if> na HP</a>
            </#if>
        </#if>

        <a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+RELATION.id)}">Vytvoř anketu</a>
        
        <a href="${URL.make("/"+RELATION.id+".docb")}">Docbook</a>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=showTalk")}">Rozhovor</a>
        <a href="${URL.make("/inset/"+RELATION.id+"?action=addFile")}">Přidat přílohy</a>
        <a href="${URL.make("/inset/"+RELATION.id+"?action=manage")}">Správa příloh</a>
    </p>
</#if>

<#if ( PAGE?default(0) == 0) >
    <div class="cl_perex">${TOOL.xpath(ITEM,"/data/perex")}</div>
</#if>

<@lib.advertisement id="arbo-sq" />

<#assign items = TOOL.processArticle( TOOL.render(TEXT,USER?if_exists) )>
<#list items as item>
    <#if item.type == "text">${item.value}
    <#elseif item.type == "poll">
        <#assign index = item.value?eval>
        <a id="inlinepoll-${index}"></a>
        <#assign url = RELATION.url?default("/clanky/show/"+RELATION.id)>
        <#if PAGE?exists><#assign url=url+"?page="+PAGE></#if>
        <@lib.showPoll CHILDREN.poll[index], url+"#inlinepoll-"+index />
        <#assign dummy=CHILDREN.poll.set(index, "UNDEF")>
    </#if>
</#list>

<#if forbidRating!="yes">
    <@lib.showRating RELATION/>
</#if>


<#if USER?exists && TOOL.permissionsFor(USER, RELATION).canModify() && CHILDREN.poll?exists>
    <#assign wrote_pollhdr=false>
    <#list CHILDREN.poll as poll>
        <#if poll!="UNDEF">
            <#if !wrote_pollhdr>
                <h3>Nepoužité ankety</h3>
                <#assign write_pollhdr=true>
            </#if>
            <@lib.showPoll poll, RELATION.url?default("/clanky/show/"+RELATION.id) />
            <div>Kód: <code>&lt;inline type="poll" id="${poll_index}"&gt;</code></div>
        </#if>
    </#list>
</#if>

<#if PAGES?exists>
    <div class="cl_perex">
        <h3>Jednotlivé podstránky článku</h3>
        <div class="s_sekce">
            <ol>
                <#list PAGES as page>
                    <li>
                        <#if page_index==PAGE>
                            ${page}
                        <#else>
                            <a href="${RELATION.url?default("/clanky/show/"+RELATION.id)}?page=${page_index}">${page}</a>
                        </#if>
                    </li>
                </#list>
            </ol>
        </div>
    </div>
</#if>

<div class="cl_perex souvisejici">
    <#if SERIES?exists>
        <h3>Seriál <a href="${SERIES.series.url}" title="${TOOL.childName(SERIES.series)}">${TOOL.childName(SERIES.series)}</a> (dílů: ${SERIES.total})</h3>
        <#if SERIES.first?exists><a href="${SERIES.first.url}">${TOOL.childName(SERIES.first)}</a> (první díl)<br /></#if>
        <#if SERIES.previous?exists>
            &lt;&mdash;&laquo; <a href="${SERIES.previous.url}">${TOOL.childName(SERIES.previous)}</a><br />
        </#if>
        <#if SERIES.next?exists>
            &raquo;&mdash;&gt; <a href="${SERIES.next.url}">${TOOL.childName(SERIES.next)}</a><br />
        </#if>
        <#if (SERIES.total > 1)><a href="${SERIES.last.url}">${TOOL.childName(SERIES.last)}</a> (poslední díl)<br /></#if>
    </#if>
    <#if RELATED?exists>
        <h3>Související články</h3>
        <#list RELATED as link>
            <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br />
        </#list>
    </#if>
    <#if RESOURCES?exists>
        <h3>Odkazy a zdroje</h3>
        <#list RESOURCES as link>
            <a href="${link.url}" rel="nofollow">${link.title}</a> ${link.description?if_exists}<br />
        </#list>
    </#if>
    <#if SAME_SECTION_ARTICLES?exists>
        <h3>Další články z této rubriky</h3>
        <#list SAME_SECTION_ARTICLES as relation>
            <a href="${relation.url?default("/clanky/show/"+relation.id)}">${TOOL.childName(relation)}</a><br />
        </#list>
    </#if>
</div>

<#assign attachments=TOOL.attachmentsFor(ITEM)>
<#if (attachments?size > 0)>
    <#assign wrote_div=false>

        <#list attachments as attachment>
            <#assign hidden=TOOL.xpath(attachment.child, "/data/object/@hidden")?default("false")>
            <#if hidden=="false" || TOOL.permissionsFor(USER, RELATION).canModify()>
                <#if !wrote_div>
                    <div class="ds_attachments"><span>Přílohy:</span><ul>
                    <#assign wrote_div=true>
                </#if>

                <li>
                <a href="${TOOL.xpath(attachment.child, "/data/object/@path")}">${TOOL.xpath(attachment.child, "/data/object/originalFilename")}</a>
                (${TOOL.xpath(attachment.child, "/data/object/size")} bytů) <#if hidden=="true"><i>skrytá</i></#if></li>
            </#if>
        </#list>

        <#if wrote_div></ul></div></#if>
</#if>

<p><b>Nástroje</b>: <a rel="nofollow" href="/clanky/show/${RELATION.id}?varianta=print">Tisk</a>,
<a rel="nofollow" href="/clanky/show/${RELATION.id}?varianta=print&amp;noDiz">Tisk bez diskuse</a>
</p>

<#flush>

<@lib.advertisement id="obsah-box" />

<#if CHILDREN.discussion?exists>
    <h3>Komentáře</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#elseif forbidDiscussion!="yes">
    <h3>Diskuse k tomuto článku</h3>
    <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<#include "../footer.ftl">
