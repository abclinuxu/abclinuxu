<#include "../header.ftl">
<#import "../misc/lib-bazar.ftl" as bazarlib>

<@lib.showMessages/>

<p>
    Pro inzeráty platí následující pravidla. Prosíme, přečtěte si je
    a řiďte se jimi. V krajním případě mohou administrátoři váš inzerát
    smazat.
</p>

<ul>
    <li>
        Inzeráty jsou bezplatnou službou čtenářům tohoto portálu. Výdělečná činnost
        není povolena. V případě pochybností rozhoduje provozovatel. Případné komerčně
        laděné inzeráty je nutné předjednat dopředu s inzertním oddělením provozovatele.
    </li>
    <li>
        Inzeráty musí být v souladu se zaměřením tohoto portálu. Vhodné inzeráty jsou
        například prodej <a href="/hardware">hardwaru</a> či spotřební elektroniky,
        <a href="/software">linuxového softwaru</a>, odborné literatury,
        plyšových tučňáků apod.
    </li>
    <li>
        Je zakázáno vkládat inzerát se stejným či podobným obsahem dříve než za celých
        uplynulých 7 dní. Inzerát nesmí porušovat místní zákony.
    </li>
    <!--<li>
        Inzeráty budou po 30 dnech automaticky smazány. Pokud ale uspějete dříve, smažte
        prosím inzerát ručně.
    </li>-->
    <li>
        Do <b>titulku</b> napište, co prodáváte či hledáte. Čtenáři se pak budou lépe orientovat
        ve výpise inzerátů. Do <b>kontaktu</b> napište své telefonní číslo, Jabber, ICQ apod. Necháte-li
        jej prázdný, systém automaticky nabídne emailový kontakt přes formulář ve vašem profilu.
    </li>
</ul>
<br />

<#if PARAMS.preview??>
    <fieldset>
        <legend>Náhled</legend>
        <@bazarlib.showBazaarAd PREVIEW, USER />
    </fieldset>
</#if>
<br />

<@lib.addForm URL.make("/edit")>
    <@lib.addInput true, "title", "Titulek", 40 />
    <@lib.addFormField true, "Typ inzerátu">
        <@lib.showOption "type", "sell", "Prodej", "radio" />
        <@lib.showOption "type", "buy", "Koupě", "radio" />
        <div class="error">${ERRORS.type!}</div>
    </@lib.addFormField>

    <@lib.addInput false, "price", "Cena", 40 />
    <@lib.addInput false, "contact", "Kontakt", 40 />

    <@lib.addTextArea true, "text", "Obsah inzerátu", 20>
        <@lib.addTextAreaEditor "text" />
    </@lib.addTextArea>

    <#if PARAMS.action=="edit" || PARAMS.action="edit2" >
        <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
            <@lib.addInputBare "rev_descr" />
        </@lib.addFormField>
    </#if>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči", "submit" />
    </@lib.addFormField>

    <#if PARAMS.action=="add" || PARAMS.action="add2" >
        <@lib.addHidden "action", "add2" />
    <#else>
        <@lib.addHidden "action", "edit2" />
    </#if>
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
