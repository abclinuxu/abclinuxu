<@lib.addRTE textAreaId="about" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete upravit svůj profil. Profil slouží jako vaše veřejná domovská stránka,
    na které máte možnost zveřejnit informace o své osobě. O tom, kdo jste, odkud jste, co máte rád,
    jaké je vaše krédo. Fantazii se meze nekladou.
</p>

<p>
    Pro vaši ochranu nejdříve zadejte současné heslo. Pokud máte na internetu svou domovskou stránku,
    vyplňte její URL. Další položkou je rok, kdy jste začal používat Linux. Následuje možnost uložit
    až pět distribucí, které v současnosti používáte. Patička může obsahovat váš oblíbený citát, odkaz
    na vaše stránky a podobně, maximální délka je 120 znaků a smíte použít jen tyto HTML značky: A, ABBR,
    ACRONYM, CITE a CODE. Posledním políčkem je text <i>O&nbsp;mně</i>. Do něj můžete napsat informace o sobě,
    které chcete sdělit čtenářům. Může to být jen pár slov, ale i delší povídání.
</p>

<@lib.addForm URL.noPrefix("/EditUser"), "name='form'">
    <@lib.addPassword true, "PASSWORD", "Heslo" />
    <@lib.addInput false, "www", "Domovská stránka" />
    <@lib.addInput false, "linuxFrom", "Linux používám od roku" />
    <@lib.addFormField false, "Používám tyto distribuce">
        <#assign distros=TOOL.asList(PARAMS.distribution)>
        <#list 0..4 as i>
            <#assign vvalue="">
            <#if distros?size gt i>
                <#assign vvalue=distros[i]>
            </#if>
            <input type="text" name="distribution" value="${vvalue?html}" size="40" />
        </#list>
    </@lib.addFormField>

    <@lib.addTextArea false, "signature", "Patička", 4, "onkeyup='writeRemainingCharsCount(this);'">
        <div id="signatureTextCounter">&nbsp;</div>
    </@lib.addTextArea>

    <@lib.addTextArea false, "about", "O mně", 25>
        <@lib.showRTEControls "about"/>
    </@lib.addTextArea>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "editProfile2" />
    <@lib.addHidden "uid", MANAGED.id />
</lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
