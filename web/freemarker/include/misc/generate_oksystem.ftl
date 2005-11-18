<div class="s_nad_h1"><div class="s_nad_pod_h1" style="background-color: #00A247">
<a class="oksystem" href="http://portal.oksystem.cz/" rel="nofollow">OKsystem</a>
<h1><a href="http://portal.oksystem.cz/">©kolení</a></h1>
</div></div>
<div class="s_sekce">
    <ul>
    <#list ITEMS as item>
        <li><a href="${item.url}" title="${item.description?html}" rel="nofollow">${item.title}</a></li>
    </#list>
    </ul>
    <FORM ACTION="http://portal.oksystem.cz/portal/page" METHOD="POST">
	<INPUT TYPE="hidden" NAME="_pageid" VALUE="53,36308">
	<INPUT TYPE="hidden" NAME="_dad" VALUE="portal">
	<INPUT TYPE="hidden" NAME="_schema" VALUE="PORTAL">
	<INPUT TYPE="text" NAME="p_mainsearch" VALUE="" class="text">
	<INPUT TYPE="submit" VALUE="Hledej" class="button">
    </FORM>
</div>

