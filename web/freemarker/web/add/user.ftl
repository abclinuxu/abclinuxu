<#include "../header.ftl">

<@lib.showMessages/>

<div style="width:250px; float:right; padding:0.5em; margin:0.5em; font-size:small; border-left:1px solid silver">
<p>Děkujeme, že jste se rozhodli registrovat na rodině portálů AbcLinuxu.cz, <a href="http://www.itbiz.cz/">ITBiz.cz</a>, <a href="http://www.abcprace.cz/">AbcPráce.cz</a> a <a href="http://www.hdmag.cz/">HDmag.cz</a>. S jediným účtem se budete moci v budoucnu přihlásit na všech těchto serverech. Snažili jsme se, aby byla registrace co nejjednodušší. Další informace a nastavení si následně můžete upravit v uživatelském profilu.</p>

<p>Registrace vám přinese mnoho výhod oproti neregistrovaným uživatelům. Můžete snadno sledovat navštívené diskuse, najít vaše dotazy v poradně, monitorovat e-mailem zvolené dokumenty, upravit si vzhled a chování portálu a mnoho dalšího.</p>
</div>


<h1>Registrace</h1>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addFormField true, "Jméno", "Jméno musí mít nejméně tři znaky.">
        <@lib.addInputBare "name", 24 />
    </@lib.addFormField>

    <@lib.addFormField true, "Login", "Přihlašovací jméno (login) musí být unikátní a mít nejméně tři znaky. Povolené znaky "+
                "jsou písmena A až Z, číslice, pomlčka, tečka a podtržítko. Login se nedá změnit, je součástí Jabber "+
                "účtu a adresy vaší stránky www.abclinuxu.cz/lide/vas-login.">
         <@lib.addInputBare "login", 24 />
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
    <@lib.addPassword true, "password2", "Zopakujte heslo.", 24 />

    <@lib.addFormField false, "Přezdívka", "Přezdívka musí být unikátní.">
         <@lib.addInputBare "nick", 24 />
         <script type="text/javascript">
            $(document).ready(function() {
               $("#nick").change(function() {
                 new Ajax.Updater('nickError', '/ajax/checkNick', {parameters: { value : $F('nick')}})
               });
             });
         </script>
    </@lib.addFormField>

    <@lib.addInput false, "email", "E-mail", 24 />

    <tr><td></td><td><div class="g-recaptcha" data-sitekey="${RECAPTCHA.key}" <#if CSS_URI?? && CSS_URI?contains("dark")>data-theme="dark"</#if>></div></td></tr>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "register2" />
</@lib.addForm>

<#include "../footer.ftl">
