<@lib.addRTE textAreaId="content" formId="form" menu="wiki" />
<#assign html_header>
    <link rel="stylesheet" type="text/css" media="all" href="/data/site/calendar/calendar-system.css" />
    <script type="text/javascript" src="/data/site/calendar/calendar.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-en.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-cs-utf8.js"></script>
    <script type="text/javascript" src="/data/site/calendar/calendar-setup.js"></script>
</#assign>

<#include "../../header.ftl">

<@lib.showMessages/>

<#if EDIT_MODE??>
    <h1>Vytvořit novou smlouvu</h1>
    <p>
        Zde můžete vytvořit novou autorskou smlouvu. Zadejte název, popis (obvykle co přináší za změny) a text smlouvy
        s parametry, které budou u každého autora nahrazeny jeho daty. Zároveň máte možnost vložit i obrázek,
        který by měl obsahovat naskenovaný podpis zodpovědné osoby, není-li ještě v systému. V tomto případě se po odeslání
        smlouva opět otevře v režimu úprav, abyste mohli obrázek přemístit dle potřeby. Je-li vybrán obrázek, pak i náhled
        trvale uloží smlouvu.
    </p>
<#else>
    <h1>Upravit smlouvu</h1>
    <p>
        Zadejte název, popis (obvykle co přináší za změny) a text smlouvy s parametry, které budou u každého autora
        nahrazeny jeho daty. Zároveň máte možnost vložit i obrázek, který by měl obsahovat naskenovaný podpis
        zodpovědné osoby, není-li ještě v systému. V tomto případě se po odeslání smlouva opět otevře v režimu úprav,
        abyste mohli obrázek přemístit dle potřeby.        
    </p>
</#if>

<#if PARAMS.preview??>
    <fieldset>
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${PREVIEW.title!}</h1>
        <div>
            ${PREVIEW.content!}
        </div>
    </fieldset>
</#if>
<br />

<form action="${URL.noPrefix("/sprava/redakce/smlouvy/edit")}" method="POST" enctype="multipart/form-data" name="form">
    <table class="siroka">
        <tr>
            <td class="required">Název:</td>
            <td>
                <input type="text" name="title" value="${(CONTRACT_TEMPLATE.title)!?html}" size="30" class="siroka"/>
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">
                Stručný popis:
                <@lib.showHelp>
                    Je vhodné zde několika větami shrnout změny oproti předchozí verzi. Tato položka se zobrazuje i autorům.
                </@lib.showHelp>
            </td>
            <td>
                <input type="text" name="description" value="${(CONTRACT_TEMPLATE.description)!?html}" size="60" class="siroka"/>
                <div class="error">${ERRORS.description!}</div>
            </td>
        </tr>
        <tr>
            <td>
                Obrázek
                <@lib.showHelp>
                    Vyberte obrázek ve formátu PNG, GIF nebo JPEG, pokud jej potřebujete použít v textu smlouvy.
                    Příkladem může být naskenovaný podpis. Soubor bude uložen do adresáře, který není možné
                    procházet. 
                </@lib.showHelp>
            </td>
            <td>
                <input type="file" name="picture" size="40">
                <@lib.showError key="picture"/>
            </td>
        </tr>
        <tr>
            <td class="required">Obsah šablony:</td>
            <td>
                <@lib.showError key="content"/>
                <@lib.showRTEControls "content"/>
            	<textarea name="content" id="content" rows="20" class="siroka">${(CONTRACT_TEMPLATE.content)!}</textarea>
            	<div class="error">${ERRORS.content!}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <input type="submit" name="preview" value="Náhled">
                <input type="submit" value="Uložit"/>
            </td>
        </tr>
    </table>
    <#if EDIT_MODE??>
        <input type="hidden" name="action" value="edit2" />
        <input type="hidden" name="rid" value="${(RELATION.id)!}" />
    <#else>
        <input type="hidden" name="action" value="add2" />
    </#if>
</form>

<h3>Parametry</h3>

<p>
    Následující parametry jsou k dispozici pro psaní šablony smlouvy. Stačí je vložit na správné místo
    textu smlouvy a během podpisu budou nahrazeny za reálné hodnoty autora. Jedná se o syntaxi freemarkeru,
    což umožňuje například vytvořit podmíněně zobrazený text. To je vhodné pro potencionálně prázdné hodnoty.
</p>

<table>
    <tr>
        <td>&#36;{AUTHOR.title}</td>
        <td>Jméno a příjmení autora</td>
    </tr>
    <tr>
        <td>&#36;{AUTHOR.address}</td>
        <td>bydliště, na jedné řádce</td>
    </tr>
    <tr>
        <td>&#36;{AUTHOR.accountNumber}</td>
        <td>číslo bankovního účtu</td>
    </tr>
    <tr>
        <td>&#36;{AUTHOR.email}</td>
        <td>Email</td>
    </tr>
    <tr>
        <td>&#36;{AUTHOR.phone}</td>
        <td>Telefonní číslo</td>
    </tr>
    <tr>
        <td>&#36;{AUTHOR.birthNumber}</td>
        <td>Rodné číslo</td>
    </tr>
    <tr>
        <td>&#36;{TODAY?date}</td>
        <td>Aktuální datum</td>
    </tr>
</table>

<#include "../../footer.ftl">