<#if SELECT_MODE?? >
<script type="text/javascript"><!--
    $(document).ready(function() {
    	$('#fua_submit').bind('click', function() {
    	   
    	    var name = $('#fua_name').val();
    	    var uid = $('#fua_uid').val();
    	    var email = $('#fua_email').val();
    	    var city = $('#fua_city').val();
    	
    	    var results = $('#findUserResults');
    	    results.empty();
    	    results.load('/ajax/findUser',
    	         {'name':name,
    	          'uid':uid,
    	          'email':email,
    	          'city':city});
    	});    		   
    });  
    
    function passLogin(login) {    	
        $('#addauthor-login').val(login);
        $('#findUserDialog').dialog().dialog('close');
    } 
      
// -->
</script>
</#if>
<body>
    <#if SELECT_MODE?? >    
    <p>Zde můžete specifikovat další informace o uživateli a vybrat si jeho login.</p>
	<table class="siroka">
	    <tr>
			<td>Osobní číslo:</td>
			<td><input type="text" id="fua_uid" name="uid" value="${(PARAMS.uid)!?html}" size="25"  tabindex="1" /></td>
		</tr>
		<tr>
			<td>Jméno:</td>
			<td><input type="text" id="fua_name" name="name" value="${(PARAMS.name)!?html}" size="40" class="siroka" tabindex="2" /></td>
		</tr>	
		<tr>
			<td>Email:</td>
			<td><input type="text" id="fua_email" name="email" value="${(PARAMS.email)!?html}" size="40" class="siroka" tabindex="3" /></td>
		</tr>
		<tr>
			<td>Bydliště:</td>
			<td><input type="text" id="fua_city" name="city" value="${(PARAMS.city)!?html}" size="40" class="siroka" tabindex="4" /></td>
		</tr>
		<tr> 
			<td></td>
			<td><input type="button" id="fua_submit" value="Vyhledat" /></td>
		</tr>
	</table>
	</#if>
	<!-- results -->
	<#if SELECT_MODE?? ><div id="findUserResults"></#if>
	<#if MANY?? >
		<p>Prosím, upřesněte dotaz, uživatelů bylo nalezeno příliš mnoho.</p>
	<#elseif ZERO?? >
		<p>Nebyl nalezen žádný odpovídající uživatel, prosím zobecněte dotaz.</p>
	<#else>
	  <#list USERS as user>
	  	<a href="javascript:passLogin('${user.login}')">${user.name}</a>&nbsp;
	  </#list>	  
	</#if>
	<#if SELECT_MODE?? ></div></#if>
</body>