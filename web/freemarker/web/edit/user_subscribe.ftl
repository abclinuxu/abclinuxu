<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Náš portál pro vás připravil několik atraktivních služeb.
</p>

<dl>
    <dt>Týdenní souhrn</dt>
    <dd>
        Je určen těm, kteří nemají čas denně nás navštěvovat. Pokud si jej přihlásíte,
        každý víkend vám zašleme emailem seznam článků a všechny zprávičky, které jsme
        daný týden vydali.
    </dd>
    <dt>Zpravodaj</dt>
    <dd>
        Pokud si jej přihlásíte, začátkem každého měsíce obdržíte email se spoustou zajímavostí
        ze světa Linuxu i z našeho portálu.
    </dd>
    <dt>Emailové rozhraní k poradnám</dt>
    <dd>
        Pro každý nový příspěvek diskuse umístěné v některé poradně se odešle všem přihlášeným
        uživatelům email s jeho obsahem a adresou, na které je možné odpovědět.
    </dd>
    <dt>Reklamní email</dt>
    <dd>
        Je forma, jak pomoci s financováním tohoto portálu. Maximálně jednou za čtrnáct dní (pravděpodobněji
        párkrát za rok) vám doručíme reklamní sdělení některého našeho inzerenta.
    </dd>
</dl>

<p>
    Pro vaši ochranu nejdříve zadejte současné heslo.
</p>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addPassword true, "PASSWORD", "Heslo" />
    <@lib.addCheckbox "weekly", "Týdenní souhrn" />
    <@lib.addCheckbox "monthly", "Měsíční zpravodaj" />
    <@lib.addCheckbox "ad", "Reklamní email" />
    <@lib.addCheckbox "forum", "Diskusní fórum" />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "subscribe2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>

<#include "../footer.ftl">
