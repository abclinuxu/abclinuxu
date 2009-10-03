<#include "../../header.ftl">

<@lib.showMessages/>

<h3>Aktuální náměty</h3>
<ul style="list-style: none">
	<li>Název - část názvu námětu</li>
	<li>Veřejný - indikace, zda je námět dostupný pro autory</li>
	<li>Termín - do kdy má být námět zpracován</li>
	<li>Honorář - výše honoráře za námět<li>
</ul>
<table class="siroka list">
	<thead>
        <tr>
            <th>Název</th>
            <th>Veřejný</th>
            <th>Termín</th>
            <th>Honorář</th>
            <th>Popis</th>
        </tr>
    </thead>
	<tbody>
	<form action="${URL.make("/redakce/namety")}" method="POST">	    
        <tr>     
            <td><@lib.filterInput filter=FILTER name="filterTopicsByTitle" size="8" tabindex="1"/></td>
            <td><select name="filterTopicsByOpened" tabindex="2">
            		<@lib.filterOption filter=FILTER name="filterTopicsByOpened" value=""></@lib.filterOption>
            		<@lib.filterOption filter=FILTER name="filterTopicsByOpened" value="1">ano</@lib.filterOption>
            		<@lib.filterOption filter=FILTER name="filterTopicsByOpened" value="0">ne</@lib.filterOption>
              	</select>
            </td>
            <td>
                 <select name="filterTopicsByTerm" tabindex="3">
                	 <@lib.filterOption filter=FILTER name="filterTopicsByTerm" value=""></@lib.filterOption>
                	 <@lib.filterOption filter=FILTER name="filterTopicsByTerm" value="0">v prodlení</@lib.filterOption>
                	 <@lib.filterOption filter=FILTER name="filterTopicsByTerm" value="0-1">tento měsíc</@lib.filterOption>
                	 <@lib.filterOption filter=FILTER name="filterTopicsByTerm" value="1-2">příští měsíc</@lib.filterOption>
                 </select>
            </td>
            <td>
                 <select name="filterTopicsByRoyalty" tabindex="4">
                 	 <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value=""></@lib.filterOption>
                 	 <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value="0">běžný</@lib.filterOption>
                 	 <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value="1">jiný</@lib.filterOption>
                 </select>
            </td>
            <td style="text-align: left"><input type="submit" name="list" value="Filtruj" tabindex="5" /></td>
        </tr>         
        <#list FOUND.data as topic>
            <tr>
                <td style="text-align: left; vertical-align: top;">
                	${(topic.title)!?html}
                	<form method="POST">
                		<input type="hidden" name="topicId" value="${topic.id}" />
                		<input type="submit" name="accept" value="Přijmout námět" />
                	</form>
                </td>
                <td style="vertical-align: top;">${(topic.isPublic())!?string("ano","ne")}</td>
                <td style="vertical-align: top;"><#if (topic.deadline)??>
                  		<#if topic.isInDelay() ><span style="color: red"></#if>
                        ${DATE.show(topic.deadline, "CZ_DMY")}
                        <#if topic.isInDelay()></span></#if>
                    </#if>
                </td>
                <td style="vertical-align: top;">${(topic.royalty)!"běžný"}</td>
                <td>
                	<textarea rows="5" cols="40" style="font-family: inherit; border: none; background: inherit;">${(topic.description)!?html}</textarea>     
                </td>
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