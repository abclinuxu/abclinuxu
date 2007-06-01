<#include "../header.ftl">

<#assign autors=TOOL.createAuthorsForArticle(RELATION.getChild()),
         forbidRating=TOOL.xpath(ITEM, "//forbid_rating")?default("UNDEF"),
         forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")?default("UNDEF"),
         inPool=RELATION.upper==8082>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<p class="cl_inforadek">
    ${DATE.show(ITEM.created,"SMART_DMY")} |
    <#list autors as autor>
        <a href="${autor.url}">${TOOL.childName(autor)}</a><#if autor_has_next>, </#if>
    </#list>
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

<#if USER?exists && USER.hasRole("article admin")>
    <p>
        <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
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
        <#if !CHILDREN.poll?exists>
            <a href="${URL.noPrefix("/EditPoll?action=add&amp;rid="+RELATION.id)}">Vytvoř anketu</a>
        </#if>
        <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&amp;url=/EditRelation&action=move&amp;rid="+RELATION.id)}">Přesunout</a>
        <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/clanky&amp;rid="+RELATION.id)}">Smazat</a>
        <a href="${URL.make("/"+RELATION.id+".docb")}">Docbook</a>
        <a href="${URL.make("/edit/"+RELATION.id+"?action=showTalk")}">Rozhovor</a>
    </p>
</#if>

<#if ( PAGE?default(0) == 0) >
    <div class="cl_perex">${TOOL.xpath(ITEM,"/data/perex")}</div>
</#if>

<@lib.advertisement id="arbo-sq" />

${TOOL.render(TEXT,USER?if_exists)}

<#if forbidRating!="yes">
    <@lib.showRating RELATION/>
</#if>

<#if CHILDREN.poll?exists>
    <h3>Anketa</h3>
    <div class="anketa">
        <@lib.showPoll CHILDREN.poll[0], RELATION.url?default("/clanky/show/"+RELATION.id) />
    </div>
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

<#if SERIES?exists>
 <table class="cl-serial">
  <tr class="hlav">
    <th colspan="2">Seriál <a href="${SERIES.series.url}">${TOOL.childName(SERIES.series)}</a> (dílů: ${SERIES.total})</th>
  </tr>
  <tr class="dil">
    <td>první díl</td>
    <td>poslední díl</td>
  </tr>
  <tr>
    <td><a href="${SERIES.first.url}">${TOOL.childName(SERIES.first)}</a></td>
    <td><#if (SERIES.total > 1)><a href="${SERIES.last.url}">${TOOL.childName(SERIES.last)}</a><#else>&bull;</#if></td>
  </tr>
  <tr class="dil">
    <td>&laquo; předchozí díl</td>
    <td>následující díl &raquo;</td>
  </tr>
  <tr>
    <td><#if SERIES.previous?exists><a href="${SERIES.previous.url}">${TOOL.childName(SERIES.previous)}</a><#else>&bull;</#if></td>
    <td><#if SERIES.next?exists><a href="${SERIES.next.url}">${TOOL.childName(SERIES.next)}</a><#else>&bull;</#if></td>
  </tr>
 </table>
</#if>

<div class="cl_perex souvisejici">
    <#if RELATED?exists>
        <h3>Související články</h3>
        <#list RELATED as link>
            <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
        </#list>
    </#if>
    <#if RESOURCES?exists>
        <h3>Odkazy a zdroje</h3>
        <#list RESOURCES as link>
            <a href="${link.url}" rel="nofollow">${link.title}</a> ${link.description?if_exists}<br>
        </#list>
    </#if>
    <#if SAME_SECTION_ARTICLES?exists>
        <h3>Další články z této rubriky</h3>
        <#list SAME_SECTION_ARTICLES as relation>
            <a href="${relation.url?default("/clanky/show/"+relation.id)}">${TOOL.childName(relation)}</a><br>
        </#list>
    </#if>
</div>

<p><b>Nástroje</b>: <a rel="nofollow" href="/clanky/show/${RELATION.id}?varianta=print">Tisk</a>,
<a rel="nofollow" href="/clanky/show/${RELATION.id}?varianta=print&amp;noDiz">Tisk bez diskuse</a>
</p>

<#flush>

<@lib.advertisement id="sun-box" />

<#if CHILDREN.discussion?exists>
    <h3>Komentáře</h3>
    <@lib.showDiscussion CHILDREN.discussion[0]/>
<#elseif forbidDiscussion!="yes">
    <h3>Diskuse k tomuto článku</h3>
    <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<#include "../footer.ftl">
