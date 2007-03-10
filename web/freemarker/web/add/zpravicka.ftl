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

<p>Povolenými HTML značkami jsou odkaz (A), URL musí být absolutní
(začínat http://) a ACRONYM. Obsah vždy pište s háčky a čárkami.
Zprávička by měla mít alespoň dvě věty a obsahovat takové podrobnosti,
aby dávala smysl, aniž by člověk musel navštívit zmíněná URL.
Titulek by měl krátce popsat hlavní téma zprávičky, bude použit v RSS
a vygeneruje se z něj URL (ve výpisu zpráviček však zobrazen nebude).</p>

<h2>A co dále?</h2>

<p>Vaše zprávička bude čekat, než ji některý správce schválí.
Správce může upravit váš text (například jej doplnit, opravit překlep, ...)
nebo změnit kategorii. V případě zamítnutí vám bude poslán email
s vysvětlením. Teprve po schválení bude zprávička zveřejněna.</p>

<#if PARAMS.preview?exists>
 <h2>Náhled</h2>
 <@lib.showNews RELATION />
</#if>

<form action="${URL.make("/edit")}" method="POST" name="newsForm">
    <p>
        <span class="required">Titulek</span><br>
        <input tabindex="1" type="text" name="title" size="40" maxlength="50" value="${PARAMS.title?if_exists?html}">
        <div class="error">${ERRORS.title?if_exists}</div>

        <span class="required">Obsah</span>
        <div class="form-edit">
            <a href="javascript:insertAtCursor(document.newsForm.content, '&lt;a href=&quot;&quot;&gt;', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
        </div>
        <textarea tabindex="2" name="content" cols="60" rows="10" tabindex="1">${PARAMS.content?if_exists?html}</textarea>
        <div class="error">${ERRORS.content?if_exists}</div>
    </p>

    <#if USER?exists && USER.hasRole("news admin")>
        Datum zveřejnění:
        <input type="text" size="16" name="publish" id="datetime_input" value="${PARAMS.publish?if_exists}">
        <input type="button" id="datetime_btn" value="..."><script type="text/javascript">cal_setupDateTime()</script>
        Formát 2005-01-25 07:12
        <div class="error">${ERRORS.publish?if_exists}</div>
    </#if>

    <h3>Kategorie</h3>
    <#assign selected = PARAMS.category?default("RELEASE")>
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
