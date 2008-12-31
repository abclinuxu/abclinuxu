<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="text" formId="form" inputMode="wiki" />
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

<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table class="siroka" cellpadding="5">
        <tr>
            <td class="required">Otázka</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title!?html}">
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Odpověď</td>
            <td>
                <@lib.showError key="text"/>
                <@rte.showFallback "text"/>
                <textarea tabindex="2" name="text" class="siroka" rows="20">${PARAMS.text!?html}</textarea><br>
            </td>
        </tr>
        <tr>
            <td>
                Popis změny
                <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
            </td>
            <td>
                <input tabindex="3" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr!?html}">
                <div class="error">${ERRORS.rev_descr!}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="4" type="submit" name="preview" value="Náhled">
                <input tabindex="5" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
