<#include "../header.ftl">

<@lib.showMessages/>

<@lib.advertisement id="arbo-sq" />

<h1>Archiv anket</h1>

<ul>
<#list POLLS.data as relation>
    <li>
        <#assign poll=relation.child, diz=TOOL.findComments(poll)?default("UNDEF")>
        ${relation.child.text}<br />
        <div class="ank-vypis">
	    <a href="${relation.url?default("/ankety/show/"+relation.id)}">${DATE.show(poll.created, "CZ_DMY")}</a>
            ${poll.totalVoters} hlasù, komentáøù: ${diz.responseCount}<#if (diz.responseCount > 0)>, poslední
	    ${DATE.show(diz.updated, "CZ_FULL")}</#if>
	</div>
    </li>
</#list>
</ul>

<p>
    <#if POLLS.prevPage?exists>
        <a href="/ankety">0</a>
        <a href="/ankety?from=${POLLS.prevPage.row}&amp;count=${POLLS.pageSize}">&lt;&lt;</a>
    <#else>0 &lt;&lt;
    </#if>
    ${POLLS.thisPage.row}-${POLLS.thisPage.row+POLLS.thisPage.size}
    <#if POLLS.nextPage?exists>
        <a href="/ankety?from=${POLLS.nextPage.row?string["#"]}&amp;count=${POLLS.pageSize}}">&gt;&gt;</a>
        <a href="/ankety?from=${(POLLS.total - POLLS.pageSize)?string["#"]}&amp;count=${POLLS.pageSize}">${POLLS.total}</a>
    <#else>&gt;&gt; ${POLLS.total}
    </#if>
</p>

<#include "../footer.ftl">


