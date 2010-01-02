<#include "../../header.ftl">

<@lib.showMessages/>

<h3>Volné náměty</h3>

<p>
    Na této stránce najdete všechny náměty, které si ještě nikdo nerezervoval.
</p>

<table class="siroka list">
	<thead>
        <tr>
            <th style="text-align: left">Název</th>
            <th style="text-align: right;">Termín odevzdání</th>
            <th>Honorář</th>
        </tr>
    </thead>
	<tbody>
	<form action="${URL.make("/sprava/redakce/namety")}" method="POST">
        <tr>     
            <td style="text-align: left">
                <@lib.filterInput filter=FILTER name="filterTopicsByTitle" size="25"/>
            </td>
            <td style="text-align: right;">
                <select name="filterTopicsByDeadline">
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value=""></@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="-1">žádný</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="0">v prodlení</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="0-1">tento měsíc</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="1-2">příští měsíc</@lib.filterOption>
                </select>
            </td>
            <td>
                <select name="filterTopicsByRoyalty">
                    <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value=""></@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value="0">běžný</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value="1">jiný</@lib.filterOption>
                </select>
            </td>
            <td style="text-align: left">
                <input type="submit" name="list" value="Filtruj"/>
            </td>
        </tr>         
        <#list FOUND.data as topic>
            <tr style="vertical-align: top;">
                <td style="text-align: left;">
                	<a href="${URL.make("/sprava/redakce/namety/" + topic.relationId)}">${(topic.title)!?html}</a>
                </td>
                <td style="text-align: right;">
                    <#if (topic.deadline)??>
                        <#assign deadlineStyle=""><#if topic.delayed><#assign deadlineStyle="style=\"color: red\""></#if>
                        <span ${deadlineStyle!}>${DATE.show(topic.deadline, "CZ_DMY")}</span>
                    </#if>
                </td>
                <td>${(topic.royalty)!"běžný"}</td>
            </tr>
        </#list>        
    </tbody>
</table>
</form>

<div id="listing-navigation">
<#if FOUND.prevPage??>
    <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${FOUND.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>
    0 &lt;&lt;
</#if>
${FOUND.thisPage.row} - ${FOUND.thisPage.row+FOUND.thisPage.size}&nbsp;
<#if FOUND.nextPage??>
    <a href="${URL_BEFORE_FROM}${FOUND.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${(FOUND.total - FOUND.pageSize)?string["#"]}${URL_AFTER_FROM}">${FOUND.total}</a>
<#else>
    &gt;&gt; ${FOUND.total}
</#if>
</div>
<#include "../../footer.ftl">