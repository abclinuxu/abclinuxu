<@lib.addRTE textAreaId="note" formId="form" menu="wiki" />
<@lib.addRTE textAreaId="setup" formId="form" menu="wiki" />
<@lib.addRTE textAreaId="params" formId="form" menu="wiki" />
<@lib.addRTE textAreaId="identification" formId="form" menu="wiki" />
<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<#if PREVIEW??>
 <h2>Náhled</h2>
 <p>
    Prohlédněte si vzhled vašeho záznamu. Opravte chyby a zvolte tlačítko <code>Náhled</code>.
    Pokud jste s výsledkem spokojeni, stiskněte tlačítko <code>Dokonči</code>.
 </p>

 <div style="padding-left: 30pt">
    <@hwlib.showHardware PREVIEW />
 </div>
</#if>

<h2>Nápověda</h2>

<p>
   Zadejte prosím podrobné informace o tomto druhu hardwaru, zda je vůbec podporován
   a na jaké úrovni, kde je možné najít ovladač, jak jej detekuje Linux, technické
   parametry, váš názor na cenu a postup zprovoznění. U něj je vhodné psát postup,
   který je nezávislý na distribuci, aby byl váš záznam užitečný i lidem, kteří
   si zvolili jinou distribuci.
</p>

<h2>Formátování</h2>

<p>
    Smíte používat základní HTML značky. Pokud je nepoužijete,
    prázdné řádky budou nahrazeny novým odstavcem. Více informací
    <a href="#formatovani">najdete</a> pod formulářem.
</p>

<form action="${URL.make("/edit")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td class="required">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name!}" size="40" tabindex="1">
                <div class="error">${ERRORS.name!}</div>
            </td>
        </tr>

        <tr>
            <td class="required">Podpora pod Linuxem</td>
            <td>
                <select name="support" tabindex="2">
                    <#assign support=PARAMS.support!>
                    <option value="complete"<#if support=="complete"> SELECTED</#if>>kompletní</option>
                    <option value="partial"<#if support=="partial"> SELECTED</#if>>částečná</option>
                    <option value="none"<#if support=="none"> SELECTED</#if>>žádná</option>
                </select>
            </td>
        </tr>

        <tr>
            <td class="required">Ovladač je dodáván</td>
            <td>
                <select name="driver" tabindex="3">
                    <#assign driver=PARAMS.driver!>
                    <option value="kernel"<#if driver=="kernel"> SELECTED</#if>>v jádře</option>
                    <option value="xfree"<#if driver=="xfree"> SELECTED</#if>>v XFree86</option>
                    <option value="maker"<#if driver=="maker"> SELECTED</#if>>výrobcem</option>
                    <option value="other"<#if driver=="other"> SELECTED</#if>>někým jiným</option>
                    <option value="none"<#if driver=="none"> SELECTED</#if>>neexistuje</option>
                    <option>netuším</option>
                </select>
            </td>
        </tr>

        <tr>
            <td>Adresa ovladače</td>
            <td>
                <input type="text" name="driverUrl" value="${PARAMS.driverUrl!}" size="60" tabindex="4" class="wide">
                <div class="error">${ERRORS.driverUrl!}</div>
            </td>
        </tr>

        <tr>
            <td>Zastaralý</td>
            <td>
                <input type="radio" name="outdated" value="yes"<#if PARAMS.outdated! == "yes"> checked</#if> tabindex="6"> ano
                <input type="radio" name="outdated" value=""<#if (PARAMS.outdated!!="yes")> checked</#if> tabindex="7"> ne
            </td>
        </tr>

        <tr>
            <td>Identifikace pod Linuxem</td>
            <td>
                <div>
                    Identifikaci zařízení pod Linuxem se věnuje <a href="/faq/hardware/jak-zjistim-co-mam-za-hardware">FAQ</a>.
                    Zadejte jen skutečně relevantní údaje, buďte struční.
                </div>
                <@lib.showError key="identification"/>
                <@lib.showRTEControls "identification"/>
                <textarea name="identification" id="identification" class="siroka" rows="15" tabindex="8">${PARAMS.identification!?html}</textarea>
            </td>
        </tr>

        <tr>
            <td>Technické parametry</td>
            <td>
                <@lib.showError key="params"/>
                <@lib.showRTEControls "params"/>
                <textarea name="params" id="params" class="siroka" rows="15" tabindex="9">${PARAMS.params!?html}</textarea>
            </td>
        </tr>

        <tr>
            <td>Postup zprovoznění</td>
            <td>
                <@lib.showError key="setup"/>
                <@lib.showRTEControls "setup"/>
                <textarea name="setup" id="setup" class="siroka" rows="20" tabindex="10">${PARAMS.setup!?html}</textarea>
            </td>
        </tr>

        <tr>
            <td>Poznámka</td>
            <td>
                <@lib.showError key="note"/>
                <@lib.showRTEControls "note"/>
                <textarea name="note" id="note" class="siroka" rows="20" tabindex="11">${PARAMS.note!?html}</textarea>
            </td>
        </tr>

        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <#if PREVIEW??>
                    <input tabindex="12" type="submit" name="preview" value="Zopakuj náhled">
                    <input tabindex="13" type="submit" name="finish" value="Dokonči">
                <#else>
                    <input tabindex="12" type="submit" name="preview" value="Náhled">
                </#if>
            </td>
        </tr>
    </table>

    <input type="hidden" name="rid" value="${RELATION.id}">
    <input type="hidden" name="action" value="add2">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
