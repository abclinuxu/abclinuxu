<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Přiřadit smlouvy autorům</h2>

<form action="${URL.noPrefix("/sprava/redakce/smlouvy")}" method="POST">
	Vyberte autory, jimž budou přiřazeny následující smlouvy:
	<ul>
	<#list CONTRACTS as contract>
		<li>${contract.title}</li>
	</#list>
	<ul>
	
    <table class="siroka">
    	<#list AUTHORS as author>
    		<#if author_index==0><tr></#if>
			<td><@lib.filterCheckBox filter=FILTER name="authorId" value="${author.id}">${author.title}</@lib.filterCheckBox></td>
			<#if ((author_index) % 3 )==0 && author_index!=0 && author_has_next ></tr><tr></#if>
			<#if author_has_next></tr></#if>
		</#list>
    </table>
    
    <input type="submit" name="assign2" value="Přiřadit" />
	<input type="submit" name="back" value="Zpět" />
	<@lib.filterHidden filter=FILTER name="filterContractsByTitle" />
	<@lib.filterHidden filter=FILTER name="filterContractsByVersion" />
	<@lib.filterHidden filter=FILTER name="filterContractsByDescription" />
	<#list FILTER.value("contractId") as contractId >
	<input type="hidden" name="contractId" value="${contractId}" />
	</#list>	
</form>

<#include "../../footer.ftl">
