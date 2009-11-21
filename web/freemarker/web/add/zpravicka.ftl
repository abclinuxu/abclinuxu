<@lib.addRTE textAreaId="content" formId="newsForm" menu="news" />
<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>
<#include "../header.ftl">

<@lib.showMessages/>

<h2>Co je to zprávička?</h2>

<p>Zprávička je krátký text, který upozorňuje naše čtenáře
na zajímavou informaci, stránky či událost ve světě Linuxu,
Open Source či IT. Zprávičky o Microsoftu mažeme, stejně
jako dotazy, žádosti o pomoc či předem nedomluvené reklamy.
</p>

<h2>Jak ji mám napsat?</h2>

<p>
    Pište s háčky a čárkami, jinak vás požádáme o nápravu. Titulek by měl krátce popsat
    hlavní téma zprávičky, bude použit v RSS a vygeneruje se z něj URL.
    Ve výpisu zpráviček však zobrazen nebude, proto vlastní obsah zprávičky musí dávat smysl
    i bez něj. <b>Zprávička by měla mít alespoň dvě věty a obsahovat takové podrobnosti,
    aby dávala smysl, aniž by člověk musel navštívit odkazovanou stránku.</b> Pokud je text příliš
    dlouhý nebo obsahuje více odstavců, bude ve výpise zobrazen jen jeho začátek či první odstavec.
    Povolenými HTML značkami jsou A (odkaz, URL musí být absolutní, tedy začínat na http://),
    ACRONYM, BR a P.
</p>

<h2>A co dále?</h2>

<p>Vaše zprávička bude čekat, než ji některý správce schválí.
Správce může upravit váš text (například jej doplnit, opravit překlep, ...)
nebo změnit kategorii. V případě zamítnutí vám bude poslán email
s vysvětlením. Teprve po schválení bude zprávička zveřejněna.</p>

<#if PARAMS.preview??>
 <h2>Náhled</h2>
 <@lib.showNews RELATION />
<#elseif WAITING_NEWS?size gt 0>
 <h2>Čekající zprávičky</h2>
 <p>
    Následující zprávičky už napsal někdo před vámi a ty nyní čekají na
    schválení administrátorem nebo na svůj čas vydání.
 </p>
 <ul>
    <#list WAITING_NEWS as rel>
        <li>${TOOL.childName(rel)}</li>
    </#list>
 </ul>
</#if>

<form action="${URL.make("/edit")}" method="POST" name="newsForm">
    <p>
        <span class="required">Titulek</span><br>
        <input tabindex="1" type="text" name="title" size="40" maxlength="50" value="${PARAMS.title!?html}">
        <div class="error">${ERRORS.title!}</div>

        <span class="required">Obsah</span>
        <@lib.showRTEControls "content"/>
        <textarea tabindex="2" name="content" id="content" class="siroka" rows="10" tabindex="1">${PARAMS.content!?html}</textarea>
        <div class="error">${ERRORS.content!}</div>
    </p>

    <#if USER?? && USER.hasRole("news admin")>
        <table>
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
                <input type="text" size="5" name="uid" value="${PARAMS.uid!}">
                <div class="error">${ERRORS.uid!}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <label><input type="checkbox" name="forbidDiscussions" value="yes" <#if PARAMS.forbidDiscussions??>checked</#if>/>Zakázat diskuzi</label>
            </td>
        </tr>
        </table>
    </#if>

    <h3>Kategorie</h3>
    <#assign selected = PARAMS.category!"RELEASE">
    <dl>
        <#list CATEGORIES as category>
            <dt>
                <input type="radio" name="category" value="${category.key}"
                <#if category.key=selected>checked</#if> >
                <b>${category.name}</b>
            </dt>
            <dd>${category.desc}</dd>
        </#list>
    </dl>
    <p>
        <input tabindex="3" name="preview" type="submit" value="Náhled">
        <input tabindex="4" type="submit" value="Dokonči">
        <input type="hidden" name="action" value="add2">
    </p>
</form>


<#include "../footer.ftl">
