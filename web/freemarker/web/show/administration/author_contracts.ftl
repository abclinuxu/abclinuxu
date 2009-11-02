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
			<td><a href="/redakce/smlouvy/?contractId=${contract.id}&amp;action=show">${(contract.version)!?html}</a></td>
			<td>${DATE.show(contract.signedDate, "ISO_DMY")}</td>
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
<#include "../../misc/contract_tail.ftl" />

</#if>
<#include "../../footer.ftl">
