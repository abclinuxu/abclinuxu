<#assign html_header>
<#if EDITOR_MODE?? >
<script type="text/javascript" src="/data/site/jquery/ui.core.js"></script>
<script type="text/javascript" src="/data/site/jquery/ui.dialog.js"></script>
<script type="text/javascript" src="/data/site/ajax/finduser.js"></script>
<script type="text/javascript">
$(document).ready(function() {	
	var buttonParent = $('#findUser');
    buttonParent.append('<input type="button" value="Vyhledat uživatele" id="findUserButton"/>');
    $('#findUserButton').bind('click', function() {
        findUserHandler('findUserResult-login', 'findUserDialog', 'addauthor-name', 'addauthor-surname', 'login');
    });	
});
</script>
</#if>
</#assign>

<#include "../../header.ftl">

<@lib.showMessages/>

<#if EDIT_MODE?? >
    <h1>Úprava autora</h1>
<#else>
    <h1>Vytvoření autora</h1>
</#if>

<p>
    Autor, který není přiřazen k žádnému uživateli, nemůže používat redakční systém. Autor si sám může
    upravit email, telefon, adresu a číslo účtu. Pro souhlas s autorskou smlouvu je potřeba rodné číslo,
    adresa a bankovní spojení. Text o autorovi se zobrazí na jeho stránce. Foto by mělo mít rozměr
    přibližně 100x100 pixelů, pokud bude některý z rozměrů přesahovat tuto mez, obrázek bude automaticky
    zmenšen. Neaktivní autor se nezobrazuje ve výběrech autorů. 
</p>

<form action="${URL.noPrefix("/sprava/redakce/autori/edit")}" method="POST" enctype="multipart/form-data">
    <table class="siroka">
        <tr>
            <td class="required">Jméno:</td>
            <td>
                <input type="text" id="addauthor-name" name="name" value="${(AUTHOR.name)!?html}" size="60" class="siroka"/>
                <div class="error">${ERRORS.name!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Příjmení:</td>
            <td>
                <input type="text" id="addauthor-surname" name="surname" value="${(AUTHOR.surname)!?html}" size="60" class="siroka"/>
                <div class="error">${ERRORS.surname!}</div>
            </td>
        </tr>
        <tr>
            <td>Login:</td>
            <td style="white-space: nowrap">
                <div id="findUser">
                    <input type="text" id="findUserResult-login" name="login" value="${(AUTHOR.login)!}" size="24"/>&nbsp;
                </div>
                <div id="findUserDialog"></div>
                <div class="error">${ERRORS.login!}</div>
            </td>
        </tr>
        <tr>
            <td>Přezdívka:</td>
            <td>
                <input type="text" name="nickname" value="${(AUTHOR.nickname)!?html}" size="24"/>
                <div class="error">${ERRORS.nickname!}</div>
            </td>
        </tr>
        <#if EDIT_MODE??>
            <tr>
                <td>Aktivní:</td>
                <td>
                    <@lib.showOption6 param="active" value="1" caption="ano" type="radio" condition=(AUTHOR.active)!false tabindex=6 />
                    <@lib.showOption6 param="active" value="0" caption="ne" type="radio" condition=((AUTHOR.active)!false)==false tabindex=6 />
                </td>
            </tr>
        </#if>
        <tr>
            <td>Rodné číslo:</td>
            <td>
                <input type="text" name="birthNumber" value="${(AUTHOR.birthNumber)!}" size="60" class="siroka"/>
                <div class="error">${ERRORS.birthNumber!}</div>
            </td>
        </tr>
        <tr>
            <td>Číslo účtu:</td>
            <td>
                <input type="text" name="accountNumber" value="${(AUTHOR.accountNumber)!}" size="60" class="siroka"/>
                <div class="error">${ERRORS.accountNumber!}</div>
            </td>
        </tr>
        <tr>
            <td>Adresa:</td>
            <td>
                <textarea name="address" class="siroka" rows="4">${(AUTHOR.address)!}</textarea>
                <div class="error">${ERRORS.address!}</div>
            </td>
        </tr>
        <tr>
            <td>Email:</td>
            <td>
                <input type="text" name="email" value="${(AUTHOR.email)!?html}" size="60" class="siroka"/>
                <div class="error">${ERRORS.email!}</div>
            </td>
        </tr>
        <tr>
            <td>Telefon:</td>
            <td>
                <input type="text" name="phone" value="${(AUTHOR.phone)!?html}" size="60" class="siroka"/>
                <div class="error">${ERRORS.phone!}</div>
            </td>
        </tr>
        <#if EDIT_MODE?? && AUTHOR.photoUrl?? >
        <tr>
            <td>Současná fotografie:</td>
            <td>
            	<img src="${(AUTHOR.photoUrl)!?html}"/>
                <input type="submit" name="remove_photo" value="Odstraň foto">
            </td>
        </tr>
        </#if>
        <tr>
            <td>Foto:</td>
            <td>
            	<input type="file" name="photo" size="40"/>
                <div class="error">${ERRORS.photo!}</div>
            </td>
        </tr>
        <tr>
            <td>O autorovi:</td>
            <td>
            	<textarea name="about" class="siroka" rows="4">${(AUTHOR.about)!}</textarea>
                <div class="error">${ERRORS.about!}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Dokonči"/></td>
        </tr>
    </table>
    <#if EDIT_MODE??>
        <input type="hidden" name="action" value="edit2"/>
        <input type="hidden" name="rid" value="${(RELATION.id)!}"/>
    <#else>
        <input type="hidden" name="action" value="add2"/>
    </#if>
</form>

<#include "../../footer.ftl">