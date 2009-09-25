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
	<form action="${URL.noPrefix("/sprava/redakce/namety")}" method="POST">	    
        <tr>
        	<td>&nbsp;</td>
            <td><@lib.filterInput filter=FILTER id="filterTopicsByTitle" size="8" tabindex="1"/></td>
            <td><select name="filterTopicsByOpened" tabindex="2">
            		<@lib.filterOption filter=FILTER id="filterTopicsByOpened" value=""></@lib.filterOption>
            		<@lib.filterOption filter=FILTER id="filterTopicsByOpened" value="1">ano</@lib.filterOption>
            		<@lib.filterOption filter=FILTER id="filterTopicsByOpened" value="0">ne</@lib.filterOption>
              	</select>
            </td>
            <td><select name="filterTopicsByAuthor" tabindex="3">
              	<@lib.filterOption filter=FILTER id="filterTopicsByAuthor" value=""></@lib.filterOption>
                <#list AUTHORS as author>
					  <@lib.filterOption filter=FILTER id="filterTopicsByAuthor" value="${author.id}">${author.title}</@lib.filterOption>
				</#list>
                </select>
            </td>
            <td>
                 <select name="filterTopicsByTerm" tabindex="4">
                	 <@lib.filterOption filter=FILTER id="filterTopicsByTerm" value=""></@lib.filterOption>
                	 <@lib.filterOption filter=FILTER id="filterTopicsByTerm" value="0">v prodlení</@lib.filterOption>
                	 <@lib.filterOption filter=FILTER id="filterTopicsByTerm" value="0-1">tento měsíc</@lib.filterOption>
                	 <@lib.filterOption filter=FILTER id="filterTopicsByTerm" value="1-2">příští měsíc</@lib.filterOption>
                 </select>
            </td>
            <td>
                 <select name="filterTopicsByAccepted" tabindex="5">
                 	 <@lib.filterOption filter=FILTER id="filterTopicsByAccepted" value=""></@lib.filterOption>
                 	 <@lib.filterOption filter=FILTER id="filterTopicsByAccepted" value="1">ano</@lib.filterOption>
                 	 <@lib.filterOption filter=FILTER id="filterTopicsByAccepted" value="0">ne</@lib.filterOption>
                 </select>
            </td>
            <td>
                 <select name="filterTopicsByRoyalty" tabindex="6">
                 	 <@lib.filterOption filter=FILTER id="filterTopicsByRoyalty" value=""></@lib.filterOption>
                 	 <@lib.filterOption filter=FILTER id="filterTopicsByRoyalty" value="0">běžný</@lib.filterOption>
                 	 <@lib.filterOption filter=FILTER id="filterTopicsByRoyalty" value="1">jiný</@lib.filterOption>
                 </select>
            </td>
            <td><input type="submit" name="list" value="Filtruj" tabindex="7" /></td>
        </tr>         
        <#list FOUND.data as topic>
            <tr>
              	<td>
              		<@lib.filterCheckBox filter=FILTER id="topicId" value="${topic.id}"></@lib.filterCheckBox>
              	</td>
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
        <#if FOUND.data??>
           	<tr>
           		<td colspan="8" style="text-align: right">
           			<input type="submit" name="notify" value="Notifikace"/>
           		</td>
           	</tr>
	    </#if>
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