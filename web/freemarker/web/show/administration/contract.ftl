<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Zobrazení smlouvy</h2>

	<table class="siroka">
        <tr>
            <td>Název: ${(CONTRACT.title)!?html}</td>
        </tr>
        <tr>
            <td>Verze: ${(CONTRACT.version)!?html}</td>
        </tr>
        <tr>
            <td>Očekáváné datum platnosti: 
            	<#if (CONTRACT.proposedDate)??>
            		<#assign proposed_date=DATE.show(CONTRACT.proposedDate, "ISO_DMY") />
            	<#else>
            		<#assign proposed_date="">
            	</#if>		
                ${proposed_date}
            </td>
        </tr>
        <tr>
            <td>Stručný popis: ${(CONTRACT.description)!?html}</td>
        </tr>
        <tr>
            <td>Obsah:<br/>
            ${(CONTRACT.content)!html}
            </td>
        </tr>
        <#if EDITOR??>
        <tr>
            <td>Jednatelovo id: ${(CONTRACT.employer.id)!}</td>
        </tr>
        </#if>
        <#--
        <tr>
        	<td>Jednatelův podpis:</td>
            <td></td>
        </tr>
        -->    
    </table>

<#include "../../footer.ftl">