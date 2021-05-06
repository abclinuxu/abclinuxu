<@lib.addRTE textAreaId="text" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pokládání dotazu</h1>

<#assign rules=TOOL.xpath(RELATION.child, "data/rules")!"UNDEF">
<#if rules!="UNDEF">
    ${TOOL.render(rules,USER!)}
</#if>

<#if ! USER??>
 <h2>Proč se přihlásit</h2>

 <p>Registrovaní čtenáři si mohou nechat sledovat diskusi, takže jim budou emailem posílány
 reakce ostatních čtenářů. Zároveň si budete moci ve svém profilu snadno vyhledat
 tento dotaz. Proto je výhodné se přihlásit. Nemáte-li u nás ještě účet,
 <a href="${URL.noPrefix("/EditUser?action=add")}">zaregistrujte&nbsp;se</a>. </p>
</#if>

<@lib.addForm URL.make("/EditDiscussion"), "name='form'", true>
    <#if ! USER??>
        <@lib.addInput true, "author", "Zadejte vaše jméno", 30>
            <br/> nebo <a href="/Profile?action=login">se přihlašte</a>.
        </@lib.addInput>
    </#if>

    <@lib.addInput true, "title", "Titulek", 60 />
    <@lib.addTextArea true, "text", "Váš komentář", 20>
        <@lib.showRTEControls "text"/>
    </@lib.addTextArea>

    <@lib.addFormField true, "Vložení přílohy", "Například výpis logu, konfigurační soubor, snímek obrazovky a podobně.">
        <@lib.addFileBare "attachment" />
    </@lib.addFormField>
    
    <@lib.addSubmit "Náhled dotazu", "preview" />
    <@lib.addHidden "action", "addQuez2" />
    <@lib.addHidden "rid", PARAMS.rid />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">


<#include "../footer.ftl">
