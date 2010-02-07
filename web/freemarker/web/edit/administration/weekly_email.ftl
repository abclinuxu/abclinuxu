<#include "../../header.ftl">
<@lib.showMessages/>

<h1>Víkendový souhrnný email</h1>

<@lib.addForm URL.make("/sprava/mailing/vikend"), "name='form'">
    <@lib.addTextArea false, "html", "Html verze", 20 />
    <@lib.addTextArea false, "text", "Text verze", 20 />

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "edit2" />
</@lib.addForm>

<#include "../../footer.ftl">