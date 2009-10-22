<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
    <li><a href="${URL.make("/redakce/smlouvy/edit/?action=add")}" title="Přidat šablonu smlouvy">Přidat smlouvu</a></li>    
</ul>
</@lib.showSignPost>

<@lib.showMessages/>

<h3>Správa smluv</h3>

<p>Na této stránce můžete prohlížet, přidávat a přiřazovat smlouvy autorům. 
Smlouvy lze filtrovat dle následujících kritérií:</p>
<ul style="list-style: none">
	<li>Verze - označení verze smlouvy</li>
	<li>Název - název smlouvy</li>
	<li>Popis - popis smlouvy</li>
</ul>

<form action="${URL.noPrefix("/sprava/redakce/smlouvy")}" method="POST">
    <table class="siroka list">
	    <thead>
            <tr>
                <th style="text-align: left">&nbsp;</th>
                <th style="text-align: left">Verze</th>
                <th style="text-align: left">Název</th>
                <th style="text-align: left">Popis</th>
                <th style="text-align: left">Akce</th>
            </tr>
        </thead>
	    <tbody>
            <tr>
            	<td>&nbsp;</td>
                <td style="text-align: left"><@lib.filterInput filter=FILTER name="filterContractsByVersion" size="10" /></td>
                <td style="text-align: left"><@lib.filterInput filter=FILTER name="filterContractsByTitle" size="20" /></td>
                <td style="text-align: left"><@lib.filterInput filter=FILTER name="filterContractsByDescription" style="width: 400px;" /></td>
                <td style="text-algin: left"><input type="submit" name="list" value="Filtruj"/></td>
            </tr>            
            <#list FOUND.data as contract>
                <tr>
                	<td><@lib.filterCheckBox filter=FILTER name="contractId" value="${contract.id}"></@lib.filterCheckBox></td>
                	<td>${contract.version?html}</td>	
                	<td>${contract.title?html}</td>
                	<td>
                		<textarea rows="3" cols="40" style="width: 400px; font-family: inherit; border: none; background: inherit;">${(contract.description)!?html}</textarea>     
                	</td>
                	<td><a href="${URL.make("/redakce/smlouvy/edit/${contract.id}?action=edit")}" title="Upravit smlouvu">U</a></td>
                </tr>
            </#list>
	    </tbody>
    </table>
<input type="submit" name="assign" value="Přiřaď vybrané smlouvy" />
</form>

<div id="contracts_navigation">
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