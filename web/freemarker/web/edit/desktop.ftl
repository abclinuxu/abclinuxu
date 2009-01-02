<#import "/web/rte-macro.ftl" as rte>
<@rte.addRTE textAreaId="desc" formId="form" inputMode="wiki" />
<#include "../header.ftl">

<h1>Úprava desktopu</h1>

<@lib.showMessages/>

<p>
    Tato stránka slouží k úpravě popisu vašeho desktopu. Jméno distribuce do titulku obvykle nepatří,
    důležitější je <a href="/slovnik/wm">správce oken</a> a téma. Nahraný obrázek nejde změnit,
    můžete upravit jen titulek a popis. Desktop je možné smazat, jen dokud pod ním nejsou cizí komentáře.
</p>

<form action="${URL.make("/desktopy/edit")}" method="POST" name="form">
    <table cellpadding="0" border="0" class="siroka">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name!}" size="40" tabindex="1">
                <@lib.showError key="name"/>
            </td>
        </tr>
        <tr>
            <td>
                URL tématu
                <@lib.showHelp>Adresa tématu nebo pozadí použitého v desktopu.</@lib.showHelp>

            </td>
            <td>
                <input type="text" name="theme" value="${PARAMS.theme!}" size="40" tabindex="2">
                <@lib.showError key="theme"/>
            </td>
        </tr>
        <tr>
            <td>Popis</td>
            <td>
                <@lib.showError key="desc"/>
                <@rte.showFallback "desc"/>
                <textarea name="desc" class="siroka" rows="20" tabindex="3">${PARAMS.desc!?html}</textarea>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" name="submit" value="Dokonči" tabindex="4" class="button">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
