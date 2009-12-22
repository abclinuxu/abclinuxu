<#if NEW_CONTRACT.signedDate??>
	<#assign THEDAY=NEW_CONTRACT.signedDate />
<#else>
	<#assign THEDAY=TODAY />
</#if>

<#if !(LOCATION??) >
<form action="${URL.noPrefix("/redakce/smlouvy/accept")}" method="POST">
</#if>
<div class="two-columns">
	<div class="left-column">
	V <#if !(LOCATION??)><input type="text" name="location" class="required" size="20" /><#else>${LOCATION}</#if> 
	dne ${DATE.show(THEDAY, "CZ_DMY", false)} <br />
	${NEW_CONTRACT.employee.title!?html} <br/>
	Autor<br />
	<#if !(LOCATION??)>
	<input type="submit" name="accept" value="Přijmout" />
	</#if>
	</div>

	<div class="right-column">
	V Praze 
	dne ${DATE.show(THEDAY,"CZ_DMY",false)} <br />
	<img src="${(NEW_CONTRACT.employerSignature)!}" /> <br />
	${(NEW_CONTRACT.employerName)!} <br />
	${(NEW_CONTRACT.employerPosition)!} <br /> 
	</div>
</div>
<#if !(LOCATION??)>
<input type="hidden" name="contractId" value="${NEW_CONTRACT.id}" />
</form>
</#if>