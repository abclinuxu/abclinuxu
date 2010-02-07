<#include "../../header.ftl">
<@lib.showMessages/>

<@lib.showSignPost "Rozcestník">
    <ul>
        <li>
            <a href="${URL.make("/sprava/mailing/vikend?action=edit")}">Upravit</a>
        </li>
    </ul>
</@lib.showSignPost>

<h1>Víkendový souhrnný email</h1>

<p>
    Zde si můžete prohlédnout a upravit šablonu týdenního souhrnného emailu. Pokud systém detekoval chybu,
    je potřeba ji odstranit dříve, než dojde k rozeslání. Obrázky v HTML verzi musí být předem nahrány na server.
    Pokud je chcete vložit do emaili jako přílohu, vložte na začátek URL konstrukci <code>inline://</code>.
</p>

<p>Aktuální počet příjemců: ${SUBSCRIPTIONS}</p>

<h2>HTML verze</h2>

<@lib.showError 'html'/>

<iframe src="/sprava/mailing/vikend/html" style="width: 100%"></iframe>

<h2>Textová verze</h2>

<@lib.showError 'text'/>

<pre style="width: 80ex;" class="kod">
    ${TEXT_VARIANT!}
</pre>

<#include "../../footer.ftl">