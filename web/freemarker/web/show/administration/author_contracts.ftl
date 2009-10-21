<#include "../../header.ftl">

<h3>Autorské smlouvy</h3>
<p>Seznam smluv, které jste odsouhlasili:</p>

<#-- list of accepted contracts -->
<#if CONTRACTS?? >
<table>
	<thead>
		<tr>
			<th>Verze</th>
			<th>Datum přijetí</th>
		</tr>	
	</thead>
	<tbody>
<#list CONTRACTS as contract>
		<tr>
			<td><a href="/redakce/smlouvy/${contract.id}?action=show">${(contract.version)!?html}</a></td>
			<td>${DATE.show(contract.effectiveDate, "ISO_DMY")}</td>
		</tr>
</#list>
	</tbody>
</table>
<#else>
<p>Žádné smlouvy nebyly nalezeny.</p>
</#if>

<#-- processing of new contract -->
<#if NEW_CONTRACT??>
<div id="new-contract">
<h3>Nová smlouva</h3>
<p>Verze: ${NEW_CONTRACT.version}</p>
<p>Popis: ${NEW_CONTRACT.description?html}</p>
<br/>
${DRAFT}
</div>

<form action="${URL.noPrefix("/redakce/smlouvy?action=accept")}" method="POST">
<div class="two-columns">
	<div class="left-column">
	V <input type="text" name="location" class="required" size="20" /> dne ${DATE.show(TODAY,"CZ_DMY",false)} <br />
	${NEW_CONTRACT.employee.name!?html} <br/> 
	<input type="submit" name="accept" value="Přijmout" />
	</div>

	<div class="right-column">
	V Praze dne ${DATE.show(TODAY,"CZ_DMY",false)} <br />
	<img src="${(NEW_CONTRACT.employerSignature)!}" /> <br />
	${(NEW_CONTRACT.employer.name)!} <br />
	Objednatel 
	</div>
</div>
<input type="hidden" name="contractId" value="${NEW_CONTRACT.id}" />
</form>

</#if>
<#include "../../footer.ftl">
