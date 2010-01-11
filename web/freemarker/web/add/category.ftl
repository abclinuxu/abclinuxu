<@lib.addRTE textAreaId="note" formId="form" menu="wiki" /
<#include "../header.ftl">

<@lib.showMessages/>

<h2>Nová sekce</h2>

<@lib.addForm URL.make("/EditCategory"), "name='form'">
    <@lib.addInput true, "name", "Jméno sekce", 40 />
    <@lib.addSelect true, "type", "Typ">
        <@lib.addOption "type", "Sekce software", "software" />
        <@lib.addOption "type", "Sekce hardware", "hardware" />
        <@lib.addOption "type", "Diskuzní fórum", "forum" />
        <@lib.addOption "type", "Sekce FAQ", "faq" />
        <@lib.addOption "type", "Rubrika", "section" />
        <@lib.addOption "type", "Blog", "blog" />
        <@lib.addOption "type", "Subportál", "subportal" />
        <@lib.addOption "type", "Sekce", "generic", true />
    </@lib.addSelect>

    <@lib.addInput false, "subtype", "Podtyp" />
    <@lib.addInput false, "group", "Skupina", 5 />
    <@lib.addFormField false, "Oprávnění skupiny">
        <#list GROUP_PERMISSIONS as perm>
            <@lib.showOption3 "groupPermissions",perm.permission,perm.permission,"checkbox",perm.set/>
        </#list>
    </@lib.addFormField>
    <@lib.addFormField false, "Oprávnění ostatních">
        <#list OTHERS_PERMISSIONS as perm>
            <@lib.showOption3 "othersPermissions",perm.permission,perm.permission,"checkbox",perm.set/>
        </#list>
    </@lib.addFormField>

    <@lib.addTextArea false, "note", "Poznámka", 15>
        <@lib.showRTEControls "note"/>
    </@lib.addTextArea>

    <@lib.addSubmit "Pokračuj" />
    <@lib.addHidden "action", "add2" />
    <@lib.addHidden "rid", PARAMS.rid />
</@lib.addForm>

<#include "../footer.ftl">
