<#assign html_header>
    <script type="text/javascript" src="/data/site/jquery/jquery-1.3.2.js"></script>
    <script type="text/javascript" src="/data/site/jquery/ui.core.js"></script>
    <script type="text/javascript" src="/data/site/jquery/ui.dialog.js"></script>    
<#if EDITOR_MODE?? >    
    <script type="text/javascript" src="/data/site/ajax/finduser.js"></script>    
    <script type="text/javascript"><!--
    $(document).ready(function() {
        var buttonParent = $('#findUser');
        buttonParent.append('<input type="button" value="Vyhledat uživatele" id="findUserButton" tabindex="4"/>');
        $('#findUserButton').bind('click', function() {
    		findUserHandler('addauthor-login', 'findUserDialog', 
    					    'addauthor-name', 'addauthor-surname');
    	}); 
    		   
    });
    
    // -->
    </script>     
</#if>          
</#assign>

<#include "../../header.ftl">

<@lib.showMessages/>

<#if EDIT_MODE?? >
<#if EDITOR_MODE?? >
<h2>Upravit údaje autora</h2>
<p>Tento formulář slouží pro editaci autora. 
Foto by mělo mít rozměr přibližně 100x100 pixelů, pokud bude některý z rozměrů přesahovat tuto mez, obrázek bude automaticky zmenšen. 
Text o autorovi se zobrazí na jeho stránce. Neaktivní autor se nezobrazuje ve výběrech autorů, je to vhodné nastavit například pro příjemce jednorázové odměny.
</p>
<#else>
<h2>Osobní údaje</h2>
<p>Zadejte prosím své osobní kontaktní a bankovní údaje</p>
</#if>
<#else>
<h2>Vytvoření autora</h2>
<p>Tento formulář slouží pro vytvoření nového autora. Autor, který není přiřazen k žádnému uživateli, nemůže používat
redakční systém. Foto by mělo mít rozměr přibližně 100x100 pixelů. Text o autorovi se zobrazí na jeho stránce.</p>
</#if>

<form action="${URL.noPrefix("/sprava/redakce/autori/edit")}" method="POST" enctype="multipart/form-data">
    <table class="siroka">
    	<#if EDITOR_MODE?? >
        <tr>
            <td>Jméno:</td>
            <td>
                <input type="text" id="addauthor-name" name="name" value="${(AUTHOR.name)!?html}" size="60" class="siroka" tabindex="1" />
                <div class="error">${ERRORS.name!}<div>
            </td>
        </tr>
        <tr>
            <td class="required">Příjmení:</td>
            <td>
            <input type="text" id="addauthor-surname" name="surname" value="${(AUTHOR.surname)!?html}" size="60" class="siroka" tabindex="2" />
            <div class="error">${ERRORS.surname!}</div>
            </td>
        </tr>
		<tr>
            <td>Login:</td>
            <td style="white-space: nowrap">
            <div id="findUser">            
            <input type="text" id="addauthor-login" name="login" value="${(AUTHOR.login)!}" size="24" tabindex="3" />&nbsp;
            </div>
            <div id="findUserDialog"></div>
            <div class="error">${ERRORS.login!}</div>
            </td>
        </tr>
        <tr>
            <td>Přezdívka:</td>
            <td>
            <input type="text" name="nickname" value="${(AUTHOR.nickname)!?html}" size="24" tabindex="5" />
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
            <input type="text" name="birthNumber" value="${(AUTHOR.birthNumber)!}" size="60" class="siroka" tabindex="6" />
            <div class="error">${ERRORS.birthNumber!}</div>
            </td>
        </tr>
        </#if>
        <tr>
            <td>Číslo účtu:</td>
            <td>
            <input type="text" name="accountNumber" value="${(AUTHOR.accountNumber)!}" size="60" class="siroka" tabindex="7" />
            <div class="error">${ERRORS.accountNumber!}</div>
            </td>
        </tr>

        <tr>
            <td>Email:</td>
            <td>
            <input type="text" name="email" value="${(AUTHOR.email)!?html}" size="60" class="siroka" tabindex="8" />
            <div class="error">${ERRORS.email!}</div>
            </td>
        </tr>
        <tr>
            <td>Telefon:</td>
            <td>
            <input type="text" name="phone" value="${(AUTHOR.phone)!?html}" size="60" class="siroka" tabindex="9" />
            <div class="error">${ERRORS.phone!}</div>
            </td>
        </tr>
        <tr>
            <td>Adresa:</td>
            <td>
            <textarea name="address" class="siroka" rows="4" tabindex="10">${(AUTHOR.address)!}</textarea>
            <div class="error">${ERRORS.address!}</div>
            </td>
        </tr>
        <#if EDITOR_MODE??>
        <#if EDIT_MODE?? && AUTHOR.photoUrl?? >
        <tr>
        	<td>Současná fotografie:</td>
        	<td><img src="${(AUTHOR.photoUrl)!?html}" />
        	<input type="submit" name="remove_photo" value="Odstraň foto" tabindex="12"> 
        	</td>
        </tr>
        </#if>
        <tr>
            <td>Foto:</td>
            <td><input type="file" name="photo" size="40" tabindex="11" />
            <div class="error">${ERRORS.photo!}</div>
            </td>
        </tr>
        <tr>
        	<td>O autorovi:</td>
        	<td><textarea name="about" class="siroka" rows="4" tabindex="13">${(AUTHOR.about)!}</textarea>
        	<div class="error">${ERRORS.about!}</div>
        	</td>
        </tr>
        </#if>        
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="14" /></td>
        </tr>
    </table>
    <#if EDIT_MODE??>
        <input type="hidden" name="action" value="edit2" />
        <input type="hidden" name="aId" value="${(AUTHOR.id)!}" />
    <#else>
        <input type="hidden" name="action" value="add2" />
    </#if>
</form>

<#include "../../footer.ftl">
