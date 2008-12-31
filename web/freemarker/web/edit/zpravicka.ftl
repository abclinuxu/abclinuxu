<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava zprávičky</h1>

<h2>Co je to zprávička?</h2>

<p>Zprávička je krátký text, který upozorňuje naše čtenáře
na zajímavou informaci, stránky či událost ve světě Linuxu,
Open Source, hnutí Free Software či obecně IT. Zprávičky
neslouží pro soukromou inzerci či oznámení, firemní oznámení
schvaluje i maže pouze <a href="/Profile/1">Leoš Literák</a>.
</p>

<h2>Jak ji mám napsat?</h2>

<p>Zprávička by měla obsahovat pouze text bez formátování, z HTML značek
je povolen jen odkaz a případně paragraf. Formátovací značky (font,
italické či tučné písmo) a obrázky jsou zapovězeny.
Pokud uživatel zvolil nevhodnou kategorii, vyberte jinou.
Titulek by měl krátce popsat hlavní téma zprávičky, bude použít v RSS
a vygeneruje se z něj URL.</p>

<h2>Náhled</h2>

    <@lib.showNews RELATION />

<form action="${URL.make("/edit")}" method="POST" name="newsForm">
    <table cellpadding="5" border="0" class="siroka">
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input type="text" name="title" size="40" maxlength="50" value="${PARAMS.title!?html}">
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Obsah</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.newsForm.content, '&lt;a href=&quot;&quot;&gt;', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                </div>
                <textarea name="content" class="siroka" rows="15" tabindex="1">${PARAMS.content!?html}</textarea>
                <div class="error">${ERRORS.content!}</div>
            </td>
        </tr>
        <#if USER?? && USER.hasRole("news admin")>
        <tr>
            <td>Datum zveřejnění:</td>
            <td>
                <input type="text" size="16" name="publish" id="datetime_input" value="${PARAMS.publish!}">
                <input type="button" id="datetime_btn" value="..."><script type="text/javascript">cal_setupDateTime()</script>
                Formát 2005-01-25 07:12
                <div class="error">${ERRORS.publish!}</div>
            </td>
        </tr>
        <tr>
            <td>Vydat pod UID</td>
            <td>
                <input type="text" size="5" name="uid">
                <div class="error">${ERRORS.uid!}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <label><input type="checkbox" name="forbidDiscussions" value="yes" <#if PARAMS.forbidDiscussions??>checked</#if>/>Zakázat diskuzi</label>
            </td>
        </tr>
        </#if>
        <tr>
            <td>Kategorie</td>
            <td>
                <#assign selected = PARAMS.category!>
                <dl>
                    <#list CATEGORIES as category>
                        <dt>
                            <input type="radio" name="category" value="${category.key}"<#if category.key=selected> checked</#if>>
                            <b>${category.name}</b>
                        </dt>
                        <dd>${category.desc}</dd>
                    </#list>
                </dl>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <input name="preview" type="submit" value="Náhled">
                <input type="submit" value="Uložit">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
