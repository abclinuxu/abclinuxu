<#include "../../header.ftl">

<@lib.showMessages/>

<#if AUTHOR?? >
<@lib.showSignPost "Rozcestník">
<ul>
  <li><a href="${URL.make("/autori/clanky/?action=add")}" title="Napsat článek">Napsat článek</a></li>
</ul>			
</@lib.showSignPost>
</#if>
<p>
Vítejte v redakčním systému. Zde můžete psát nové články, kontrolovat své honoráře,
prohlížet náměty, upravovat osobní údaje atd.
</p> 

<#if EDITOR_MODE?? >
<div class="two-columns" id="editor-author-menu">
	<div class="two-columns left-column" id="editor-menu">
	<h3>Editor</h3>
	<ul class="left-column" id="editor-menu-left" style="margin-left: 30px;">
		<li><a href="/sprava/redakce/clanky">Články</a></li>
		<li><a href="/sprava/redakce/namety">Náměty</a></li>
		<li><a href="/sprava/redakce/serialy">Seriály</a></li>
		<li><a href="/sprava/redakce/zpravicky">Zprávičky</a></li>
		<li><a href="/sprava/redakce/ankety">Ankety</a></li>
		<li><a href="/sprava/redakce/udalosti">Události</a></li>
	</ul>
	<ul class="right-column" id="editor-menu-right">
		<li><a href="/sprava/redakce/honorare">Honoráře</a></li>
		<li><a href="/sprava/redakce/statistiky">Statistiky</a></li>
		<li><a href="/sprava/redakce/autori">Autoři</a></li>
		<li><a href="/sprava/redakce/smlouvy">Autorské smlouvy</a></li>
	</ul>
	</div><#-- end of editor-menu -->
</#if>	
	
	<#if AUTHOR?? >
	<#if EDITOR_MODE?? > <div class="right-column" id="author-menu-wrapper"/></#if>		
	<h3>Redaktor</h3>
	<ul id="author-menu" style="margin: 0; margin-left: 30px; padding: 0; display: table;">
		<li><a href="/redakce/clanky">Mé články</a></li>
		<li><a href="/redakce/namety">Náměty</a></li>
		<li><a href="/redakce/honorare"">Mé honoráře</a></li>
		<li><a href="/redakce/autori/edit/${AUTHOR.id}?action=edit">Osobní údaje</a></li>
		<li><a href="/redakce/smlouvy">Autorské smlouvy</a></li>
	</ul>
	<#if EDITOR_MODE?? ></div></#if>
	</#if><#-- AUTHOR?? -->		
<#if EDITOR_MODE?? >
</div>
</#if><#-- end two column layout -->

<#if AUTHOR?? >
<div class="clear-float">&nbsp;</div>
<h2>Chystané články</h2>

<h2>Plánováné náměty</h2>
<#if TOPICS?? >
<table class="siroka">
<#list TOPICS as topic>
	<tr>
    	<td style="text-align: left; vertical-align: top">${(topic.title)!?html}</td>
        <td style="vertical-align: top"><#if topic.isInDelay() ><span style="color: red"></#if>
            ${DATE.show(topic.deadline, "CZ_DMY")}
            <#if topic.isInDelay()></span></#if>
        </td>
        <td>
        	<textarea rows="5" cols="60" style="font-family: inherit; border: none; background: inherit;">${(topic.description)!?html}</textarea>
        </td>
    </tr>                
</#list>
</table>
<#else>
	<p>Nejsou pro Vás plánováné žádné náměty.</p>
</#if>

</#if><#-- end AUTHOR?? -->
<#include "../../footer.ftl">