<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
   <li><a href="${URL.make("/redakce/namety/edit?action=add")}">Přidat námět</a></li>
</ul>
</@lib.showSignPost>

<@lib.showMessages/>

<h3>Náměty</h3>

<p>Seznam všech námětů. Námět může být buď přiřazený konkrétnímu redaktorovi nebo je veřejný. 
Téma je přijato, pokud je přiřazeno k článku, které editor přidal a ten již není ve stavu rozepsaný</p>

<ul style="list-style: none">
	<li>Název - část názvu námětu</li>
	<li>Veřejný - indikace, zda je námět dostupný pro autory</li>
	<li>Autor - přiřazený autor</li>
	<li>Termín - do kdy má být námět zpracován</li>
	<li>Přijatý - indikace, zda autor námět přijal</li>
	<li>Honorář - výše honoráře za námět<li>
</ul>

<form action="${URL.noPrefix("/sprava/redakce/namety")}" method="POST">
    <table class="siroka list">
	    <thead>
            <tr>
                <th>&nbsp;</th>
                <th>Název</th>
                <th>Veřejný</th>
                <th>Autor</th>
                <th>Termín</th>
                <th>Přijatý</th>
                <th>Honorář</th>
                <th>&nbsp;</th>
            </tr>
        </thead>
	    <tbody>
            <tr>
            	<td>&nbsp;</td>
                <td><@lib.showFilterInput FILTER, "filterTopicsByTitle", "8" /></td>
                <td><select name="filterTopicsByOpened">
                		<@lib.showOption5 "", "", FILTER.checked("filterTopicsByOpened", "")/>
                        <@lib.showOption5 "1", "ano", FILTER.checked("filterTopicsByOpened", "1")/>
                        <@lib.showOption5 "0", "ne", FILTER.checked("filterTopicsByOpened", "0")/> 
                	</select>
                </td>
                <td><select name="filterTopicsByAuthor">
                		<@lib.showOption5 "", "", FILTER.checked("filterTopicsByAuthor", "") />
                    	<#list AUTHORS as author>
							<@lib.showOption5 "${author.id}", "${author.title}", FILTER.checked("filterTopicsByAuthor", "${author.id}") />
						</#list>
                    </select>
                </td>
                <td>
                    <select name="filterTopicsByTerm">
                        <@lib.showOption5 "", "", FILTER.checked("filterTopicsByTerm", "") />
                        <@lib.showOption5 "0", "v prodlení", FILTER.checked("filterTopicsByTerm", "0") />
                        <@lib.showOption5 "0-1", "tento měsíc", FILTER.checked("filterTopicsByTerm", "0-1") />
                        <@lib.showOption5 "1-2", "příští měsíc", FILTER.checked("filterTopicsByTerm", "1-2") />
                    </select>
                </td>
                <td>
                    <select name="filterTopicsByAccepted">
                        <@lib.showOption5 "", "", FILTER.checked("filterTopicsByAccepted", "") />
                        <@lib.showOption5 "1", "ano", FILTER.checked("filterTopicsByAccepted", "1") />
                        <@lib.showOption5 "0", "ne", FILTER.checked("filterTopicsByAccepted", "0") />
                    </select>
                </td>
                <td>
                    <select name="filterTopicsByRoyalty">
                        <@lib.showOption5 "", "", FILTER.checked("filterTopicsByRoyalty", "") />
                        <@lib.showOption5 "0", "běžný", FILTER.checked("filterTopicsByRoyalty", "0") />
                        <@lib.showOption5 "1", "jiný", FILTER.checked("filterTopicsByRoyalty", "1") />
                    </select>
                </td>
                <td><input type="submit" value="Filtruj" /></td>
            </tr>
            <#list FOUND.data as topic>
                <tr>
                	<td><input type="checkbox" value="${topic.id}" name="filterTopics" /></td>
                    <td style="text-align: left">${(topic.title)!?html}</td>
                    <td>${(topic.isPublic())!?string("ano","ne")}</td>
                    <td><#if (topic.author)?? >
                    	<a href="${URL.make("/redakce/autori/show/?aId=${(topic.author.id)!}&amp;action=show")}">${(topic.author.title)!?html}</a>
                    	</#if>
                    </td>
                    <td><#if (topic.deadline)??>
                    		<#if topic.isInDelay() ><span style="color: red"></#if>
                            ${DATE.show(topic.deadline, "CZ_DMY")}
                            <#if topic.isInDelay()></span></#if>
                        </#if>
                    </td>
                    <td>${(topic.accepted)!?string("ano","ne")}</td>
                    <td>${(topic.royalty)!"běžný"}</td>
                    <td style="white-space: nowrap">
                        <a href="${URL.make("/sprava/redakce/namety/edit/${topic.id}?action=edit")}" title="Upravit námět">U</a>&nbsp;
                        <a href="${URL.make("/sprava/redakce/namety/edit/${topic.id}?action=rm")}" title="Smazat námět">X</a>
                    </td>
                </tr>
            </#list>
	    </tbody>
    </table>
</form>

<div>
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