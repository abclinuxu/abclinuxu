<@lib.addRTE textAreaId="text" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se upravit často kladenou otázku. Povolené jsou jen změny,
které vylepšují kvalitu odpovědi, formátování, pravopis, stylistiku
a podobně. Rozhodně jsou zakázány dotazy, od toho je zde <a href="/diskuse.jsp">diskusní fórum</a>.
Vaše změny budou uloženy jako nová revize, tudíž je možné je kdykoliv
vrátit zpět.</p>
<br />

<#if PARAMS.preview??>
    <fieldset>
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${PREVIEW.title!}</h1>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text"), USER!)}
        </div>
    </fieldset>
</#if>
<br />

<@lib.addForm URL.make("/faq/edit"), "name='form'">
    <@lib.addInput true, "title", "Otázka", 80 />
    <@lib.addTextArea true, "text", "Odpověď", 20>
        <@lib.showRTEControls "text"/>
    </@lib.addTextArea>
    <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
        <@lib.addInputBare "rev_descr" />
    </@lib.addFormField>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
