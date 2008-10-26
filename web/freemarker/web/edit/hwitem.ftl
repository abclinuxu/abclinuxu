<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="note" formId="form" inputMode="wiki" />
<@rte.addRTE textAreaId="setup" formId="form" inputMode="wiki" />
<@rte.addRTE textAreaId="params" formId="form" inputMode="wiki" />
<@rte.addRTE textAreaId="identification" formId="form" inputMode="wiki" />
<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<#if PREVIEW?exists>
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
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td class="required">Podpora pod Linuxem</td>
            <td>
                <select name="support" tabindex="2">
                    <#assign support=PARAMS.support?if_exists>
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
                    <#assign driver=PARAMS.driver?if_exists>
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
                <input type="text" name="driverUrl" value="${PARAMS.driverUrl?if_exists}" size="60" tabindex="4" class="wide">
                <div class="error">${ERRORS.driverUrl?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Zastaralý</td>
            <td>
                <input type="radio" name="outdated" value="yes"<#if PARAMS.outdated?if_exists=="yes"> checked</#if> tabindex="6"> ano
                <input type="radio" name="outdated" value=""<#if (PARAMS.outdated?if_exists!="yes")> checked</#if> tabindex="7"> ne
            </td>
        </tr>

        <tr>
            <td>Identifikace pod Linuxem</td>
            <td>
                <div>
                    Identifikaci zařízení pod Linuxem se věnuje <a href="/faq/hardware/jak-zjistim-co-mam-za-hardware">FAQ</a>.
                    Zadejte jen skutečně relevantní údaje, buďte struční.
                </div>
                <@rte.showFallback "identification"/>
                <textarea name="identification" class="siroka" rows="12" tabindex="8" class="wide">${PARAMS.identification?if_exists?html}</textarea>
                <@lib.showError key="identification"/>
            </td>
        </tr>

        <tr>
            <td>Technické parametry</td>
            <td>
                <@rte.showFallback "params"/>
                <textarea name="params" class="siroka" rows="14" tabindex="9" class="wide">${PARAMS.params?if_exists?html}</textarea>
                <@lib.showError key="params"/>
            </td>
        </tr>

        <tr>
            <td>Postup zprovoznění</td>
            <td>
                <@rte.showFallback "setup"/>
                <textarea name="setup" class="siroka" rows="16" tabindex="10" class="wide">${PARAMS.setup?if_exists?html}</textarea>
                <@lib.showError key="setup"/>
            </td>
        </tr>

        <tr>
            <td>Poznámka</td>
            <td>
                <@rte.showFallback "note"/>
                <textarea name="note" class="siroka" rows="16" tabindex="11" class="wide">${PARAMS.note?if_exists?html}</textarea>
                <@lib.showError key="note"/>
            </td>
        </tr>

        <tr>
            <td>
                Popis změny
                <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
            </td>
            <td>
                <input tabindex="12" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists?html}">
                <div class="error">${ERRORS.rev_descr?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <#if PREVIEW?exists>
                    <input tabindex="13" type="submit" name="preview" value="Zopakuj náhled">
                <#else>
                    <input tabindex="13" type="submit" name="preview" value="Náhled">
                </#if>
                <input tabindex="14" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>

    <input type="hidden" name="rid" value="${RELATION.id}">
    <input type="hidden" name="action" value="edit2">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
