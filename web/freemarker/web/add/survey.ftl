<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vložení velké ankety</h1>

<p>Nápovědu najdete ve <a href="https://wiki.stickfish.com/bin/view/Projects/SurveyFormat">wiki</a></p>

<h2>Anketa</h2>

<@lib.addForm URL.make("/EditSurvey")>
    <@lib.addInput true, "title", "Jméno ankety", 40 />
    <@lib.addTextArea false, "choices", "Volby", 4, "cols='40'" />
    <@lib.addTextArea true, "definition", "XML definice", 20, "class='siroka'" />
    <@lib.addSubmit "Pokračuj" />

    <#if PARAMS.surveyId??>
        <@lib.addHidden "action", "edit2" />
        <@lib.addHidden "surveyId", PARAMS.surveyId />
    <#else>
        <@lib.addHidden "action", "add2" />
    </#if>
</@lib.addForm>

<#include "../footer.ftl">
