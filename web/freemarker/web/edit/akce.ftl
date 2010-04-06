<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava akce</h1>

<@lib.addForm URL.make("/edit"), "", true>
    <@lib.addInput true, "title", "Název" />
    <@lib.addSelect true, "subtype", "Typ">
        <@lib.addOption "subtype", "Komunitní", "community" />
        <@lib.addOption "subtype", "Vzdělávací", "educational" />
        <@lib.addOption "subtype", "Firemní", "company" />
    </@lib.addSelect>
    <@lib.addSelect true, "region", "Kraj">
        <optgroup label="Česká republika">
            <@lib.addOption "region", "Jihočeský", "jihocesky" />
            <@lib.addOption "region", "Jihomoravský", "jihomoravsky" />
            <@lib.addOption "region", "Karlovarský", "karlovarsky" />
            <@lib.addOption "region", "Královehradecký", "kralovehradecky" />
            <@lib.addOption "region", "Liberecký", "liberecky" />
            <@lib.addOption "region", "Moravskoslezský", "moravskoslezsky" />
            <@lib.addOption "region", "Olomoucký", "olomoucky" />
            <@lib.addOption "region", "Pardubický", "pardubicky" />
            <@lib.addOption "region", "Plzeňský", "plzensky" />
            <@lib.addOption "region", "Praha", "praha" />
            <@lib.addOption "region", "Středočeský", "stredocesky" />
            <@lib.addOption "region", "Ústecký", "ustecky" />
            <@lib.addOption "region", "Vysočina", "vysocina" />
            <@lib.addOption "region", "Zlínský", "zlinsky" />
        </optgroup>
        <optgroup label="Slovenská republika">
            <@lib.addOption "region", "Banskobystrický", "banskobystricky" />
            <@lib.addOption "region", "Bratislavský", "bratislavsky" />
            <@lib.addOption "region", "Košický", "kosicky" />
            <@lib.addOption "region", "Nitranský", "nitransky" />
            <@lib.addOption "region", "Prešovský", "presovsky" />
            <@lib.addOption "region", "Trenčínský", "trencinsky" />
            <@lib.addOption "region", "Trnavský", "trnavsky" />
            <@lib.addOption "region", "Žilinský", "zilinsky" />
        </optgroup>
    </@lib.addSelect>

    <@lib.addInput true, "date", "Začátek od">
        <input type="button" id="datetime_btn" value="...">
        <script type="text/javascript">
            Calendar.setup({inputField:"date",ifFormat:"%Y-%m-%d %H:%M",showsTime:true,button:"datetime_btn",singleClick:false,step:1,firstDay:1});
        </script>
    </@lib.addInput>

    <@lib.addInput false, "dateTo", "Konec v">
        <input type="button" id="datetime_btn2" value="...">
        <script type="text/javascript">
            Calendar.setup({inputField:"dateTo",ifFormat:"%Y-%m-%d %H:%M",showsTime:true,button:"datetime_btn2",singleClick:false,step:1,firstDay:1});
        </script>
        Formát 2005-01-25 07:12, volitelný údaj
    </@lib.addInput>

    <@lib.addInput false, "location", "Přesné umístění">
        Zadejte souřadnice či jiné údaje použitelné pro vyhledávání na Google Maps.<br />
        Příklady:
        <ul>
            <li>50°5'31.77"N, 14°26'47.789"E</li>
            <li>u maxe, budějovice</li>
        </ul>
    </@lib.addInput>

    <@lib.addFormField true, "Stručný popis", "Text, který bude zobrazen ve výpisu akcí a jako úvod na stránce akce.">
        <@lib.addTextAreaEditor "descriptionShort" />
        <@lib.addTextAreaBare "descriptionShort" />
    </@lib.addFormField>

    <@lib.addFormField false, "Detailní popis", "Text, který bude zobrazen pouze na stránce samotné akce.">
        <@lib.addTextAreaEditor "description" />
        <@lib.addTextAreaBare "description" />
    </@lib.addFormField>

    <#assign logo=TOOL.xpath(RELATION.child,"/data/icon")!"UNDEF">
    <#if logo != "UNDEF">
        <@lib.addCheckBox "removeLogo", "Odstranit současné logo">
            <img src="${logo}" alt="logo">
        </@lib.addCheckBox>
    </#if>

    <@lib.addFormField false, "Logo", "Pokud má vaše organizace/firma/parta nějaké (malé) logo, můžete jej nechat vložit.">
        <@lib.addFileBare "logo">
            Rozměry maximálně 250&times;200.
        </@lib.addFileBare>
    </@lib.addFormField>

    <#if USER.hasRole("root")>
        <@lib.addInput false, "uid", "UID vlastníka" />
    </#if>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />

</@lib.addForm>

<#include "../footer.ftl">
