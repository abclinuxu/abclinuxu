<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Přidání nové akce</h1>

<p>
Ať už plánujete posezení v hospodě, installfest či nějakou velkou akci, zde je pro takovou věc místo. Po vyplnění a odeslání formuláře bude vaše akce čekat, než ji některý z adminů schválí. Poté ji bude moci každý najít v kalendáři akcí.
</p>

<form action="/akce/edit" method="post" name="eventForm" enctype="multipart/form-data">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td class="required">Název</td>
            <td>
                <input type="text" name="title" value="${PARAMS.title?if_exists}" size="40">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Typ</td>
            <td>
                <select name="subtype">
                    <#assign subtype=PARAMS.subtype?default("")>
                    <option value="community" <#if subtype=="community">SELECTED</#if>>Komunitní</option>
                    <option value="educational" <#if subtype=="educational">SELECTED</#if>>Školní</option>
                    <option value="company" <#if subtype=="company">SELECTED</#if>>Firemní</option>
                </select>
                <div class="error">${ERRORS.subtype?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Kraj</td>
            <td>
                <select name="region">
                    <#assign region=PARAMS.region?default("praha")>
                    <optgroup label="Česká republika">
                        <option value="jihocesky" <#if region=="jihocesky">SELECTED</#if>>Jihočeský</option>
                        <option value="jihomoravsky" <#if region=="jihomoravsky">SELECTED</#if>>Jihomoravský</option>
                        <option value="karlovarsky" <#if region=="karlovarsky">SELECTED</#if>>Karlovarský</option>
                        <option value="kralovehradecky" <#if region=="kralovehradecky">SELECTED</#if>>Královehradecký</option>
                        <option value="liberecky" <#if region=="liberecky">SELECTED</#if>>Liberecký</option>
                        <option value="moravskoslezsky" <#if region=="moravskoslezsky">SELECTED</#if>>Moravskoslezský</option>
                        <option value="olomoucky" <#if region=="olomoucky">SELECTED</#if>>Olomoucký</option>
                        <option value="pardubicky" <#if region=="pardubicky">SELECTED</#if>>Pardubický</option>
                        <option value="plzensky" <#if region=="plzensky">SELECTED</#if>>Plzeňský</option>
                        <option value="praha" <#if region=="praha">SELECTED</#if>>Praha</option>
                        <option value="stredocesky" <#if region=="stredocesky">SELECTED</#if>>Středočeský</option>
                        <option value="ustecky" <#if region=="ustecky">SELECTED</#if>>Ústecký</option>
                        <option value="vysocina" <#if region=="vysocina">SELECTED</#if>>Vysočina</option>
                        <option value="zlinsky" <#if region=="zlinsky">SELECTED</#if>>Zlínský</option>
                    </optgroup>
                    <optgroup label="Slovenská republika">
                        <option value="banskobystricky" <#if region=="banskobystricky">SELECTED</#if>>Banskobystrický</option>
                        <option value="bratislavsky" <#if region=="bratislavsky">SELECTED</#if>>Bratislavský</option>
                        <option value="kosicky" <#if region=="kosicky">SELECTED</#if>>Košický</option>
                        <option value="nitransky" <#if region=="nitransky">SELECTED</#if>>Nitranský</option>
                        <option value="presovsky" <#if region=="presovsky">SELECTED</#if>>Prešovský</option>
                        <option value="trencinsky" <#if region=="trencinsky">SELECTED</#if>>Trenčínský</option>
                        <option value="trnavsky" <#if region=="trnavsky">SELECTED</#if>>Trnavský</option>
                        <option value="zilinsky" <#if region=="zilinsky">SELECTED</#if>>Žilinský</option>
                    </optgroup>
                </select>
                <div class="error">${ERRORS.region?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Začátek od</td>
            <td>
                <input type="text" name="date" id="datetime_input" value="${PARAMS.date?if_exists}">
                <input type="button" id="datetime_btn" value="...">
                    <script type="text/javascript">cal_setupDateTime();</script>
                Formát 2005-01-25 07:12
                <div class="error">${ERRORS.date?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Konec v</td>
            <td>
                <input type="text" name="dateTo" id="datetime_input2" value="${PARAMS.dateTo?if_exists}">
                <input type="button" id="datetime_btn2" value="...">
                    <script type="text/javascript">
                        Calendar.setup({inputField:"datetime_input2",ifFormat:"%Y-%m-%d %H:%M",showsTime:true,button:"datetime_btn2",singleClick:false,step:1,firstDay:1});
                    </script>
                Formát 2005-01-25 07:12, volitelný údaj
                <div class="error">${ERRORS.dateTo?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Přesné umístění</td>
            <td>
                <input type="text" name="location" value="${PARAMS.location?if_exists}" size="40">
                <div class="error">${ERRORS.location?if_exists}</div>
                Zadejte souřadnice či jiné údaje použitelné pro vyhledávání na Google Maps.
            </td>
        </tr>
        <tr>
            <td class="required">
                    Stručný popis
                    <a class="info" href="#">?<span class="tooltip">Text, který bude zobrazen ve výpisu akcí a jako úvod na stránce akce.</span></a>
            </td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.eventForm.descriptionShort, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.eventForm.descriptionShort, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.eventForm.descriptionShort, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.eventForm.descriptionShort, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.eventForm.descriptionShort, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>

                <textarea name="descriptionShort" rows="11" class="siroka"><#if PARAMS.descriptionShort?exists>${PARAMS.descriptionShort?html}</#if></textarea>
                <div class="error">${ERRORS.descriptionShort?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>
                    Detailní popis
                    <a class="info" href="#">?<span class="tooltip">Text, který bude zobrazen pouze na stránce samotné akce.</span></a>
            </td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.eventForm.description, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.eventForm.description, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.eventForm.description, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.eventForm.description, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.eventForm.description, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>

                <textarea name="description" rows="11" class="siroka"><#if PARAMS.description?exists>${PARAMS.description?html}</#if></textarea>
                <div class="error">${ERRORS.description?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>
                Logo
                <a class="info" href="#">?<span class="tooltip">Pokud má vaše organizace/firma/parta nějaké (malé) logo, můžete jej vložit.</span></a>
            </td>
            <td>
                <input type="file" name="logo" size="20" tabindex="3"> Rozměry maximálně 100&times;100.
                <div class="error">${ERRORS.logo?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Vytvoř"></td>
        </tr>
    </table>

    <input type="hidden" name="action" value="add2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "../footer.ftl">
