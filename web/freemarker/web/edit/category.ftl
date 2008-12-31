<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="note" formId="form" inputMode="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úprava sekce</h2>

<form action="${URL.make("/EditCategory")}" method="POST" name="form">

    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="120" class="required">Jméno sekce</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name!}" size="40" tabindex="1">
                <div class="error">${ERRORS.name!}</div>
            </td>
        </tr>
        <tr>
            <td width="120" class="required">Typ</td>
            <td>
                <select name="type" tabindex="2">
                    <option value="software"<#if PARAMS.type! == "software"> SELECTED</#if>>Sekce software</option>
                    <option value="hardware"<#if PARAMS.type! == "hardware"> SELECTED</#if>>Sekce hardware</option>
                    <option value="forum"<#if PARAMS.type! == "forum"> SELECTED</#if>>Diskusní fórum</option>
                    <option value="faq"<#if PARAMS.type! == "faq"> SELECTED</#if>>Sekce FAQ</option>
                    <option value="section"<#if PARAMS.type! == "section"> SELECTED</#if>>Rubrika</option>
                    <option value="blog"<#if PARAMS.type! == "blog"> SELECTED</#if>>Blog</option>
                    <option value="subportal"<#if PARAMS.type! == "subportal"> SELECTED</#if>>Subportál</option>
                    <option value="generic"<#if PARAMS.type?default("generic")=="generic"> SELECTED</#if>>Sekce</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="120">Podtyp</td>
            <td>
                <input type="text" name="subtype" value="${PARAMS.subtype!}" tabindex="3">
            </td>
        </tr>
        <tr>
            <td width="120">Skupina</td>
            <td>
                <input type="text" name="group" value="${PARAMS.group}" tabindex="4">
                <div class="error">${ERRORS.group!}</div>
            </td>
        </tr>
        <tr>
            <td width="120">Oprávnění skupiny</td>
            <td>
                <#list GROUP_PERMISSIONS as perm>
                    <@lib.showOption3 "groupPermissions",perm.permission,perm.permission,"checkbox",perm.set/>
                </#list>
            </td>
        </tr>
        <tr>
            <td width="120">Oprávnění ostatních</td>
            <td>
                <#list OTHERS_PERMISSIONS as perm>
                    <@lib.showOption3 "othersPermissions",perm.permission,perm.permission,"checkbox",perm.set/>
                </#list>
            </td>
        </tr>
        <tr>
            <td></td>
            <td><label><input type="checkbox" name="recurse" value="true" />Provést změnu práv rekurzivně</label>
        </tr>
        <tr>
            <td width="120">Relation.upper</td>
            <td>
                <input type="text" name="upper" value="${PARAMS.upper!}" tabindex="7">
                <div class="error">${ERRORS.upper!}</div>
            </td>
        </tr>
        <tr>
            <td width="120">Poznámka</td>
            <td>
                <@lib.showError key="note"/>
                <@rte.showFallback "note"/>
                <textarea name="note" class="siroka" rows="20" tabindex="8">${PARAMS.note!?html}</textarea>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td><input type="submit" VALUE="Dokonči" TABINDEX="9"></td>
        </tr>
    </table>

    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
    <input type="hidden" name="url" value="${URL.make("/EditCategory")}">
</form>


<#include "../footer.ftl">
