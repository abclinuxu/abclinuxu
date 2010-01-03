<#include "../header.ftl">

<@lib.showMessages/>

<h1>Registrace</h1>

<p>
    Děkujeme, že jste se rozhodli registrovat na rodině portálů abclinuxu.cz, itbiz.cz, abcprace.cz, abchost.cz a 64bit.cz.
    S jediným účtem se budete moci přihlásit na všech těchto serverech. Snažili jsme se registraci učinit co nejjednodušší,
    další informace a nastavení se následně můžete upravit v nastavení.
</p>

<p>
    Registrace vám přinese mnoho výhod oproti neregistrovaným uživatelům. Můžete snadno sledovat navštívené diskuse,
    najít vaše dotazy v poradně, monitorovat emailem zvolené dokumenty, upravit si vzhled a chování portálu a mnoho
    dalšího.
</p>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addFormField true, "Jméno", "Jméno musí mít nejméně tři znaky.">
        <@lib.addInputBare "name", 24 />
    </@lib.addFormField>

    <@lib.addFormField true, "Login", "Přihlašovací jméno (login) musí být unikátní a mít nejméně tři znaky. Povolené znaky "+
                "jsou písmena A až Z, číslice, pomlčka, tečka a podtržítko. Login se nedá změnit, je součástí Jabber "+
                "účtu a adresy vaši stránky /lide/vas-login.">
         <@lib.addInputBare "login", 24" />
         <script type="text/javascript">
            $(document).ready(function() {
               $("#login").change(function() {
                 new Ajax.Updater('loginError', '/ajax/checkLogin', {parameters: { value : $F('login')}})
               });
             });
         </script>
    </@lib.addFormField>

    <@lib.addFormField true, "Heslo", "Heslo musí mít nejméně čtyři znaky.">
        <@lib.addPasswordBare "password", 24 />
    </@lib.addFormField>
    <@lib.addPassword true, "password2", "Zopakujte heslo", 24 />

    <@lib.addFormField false, "Přezdívka", "Přezdívka musí být unikátní">
         <@lib.addInputBare "nick", 24" />
         <script type="text/javascript">
            $(document).ready(function() {
               $("#nick").change(function() {
                 new Ajax.Updater('nickError', '/ajax/checkNick', {parameters: { value : $F('nick')}})
               });
             });
         </script>
    </@lib.addFormField>

    <@lib.addFormField true, "Aktuální rok", "Vložte aktuální rok. Jedná se o ochranu před spamboty.">
        <@lib.addInputBare "antispam", 4 />
    </@lib.addFormField>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "register2" />
</@lib.addForm>

<#include "../footer.ftl">
