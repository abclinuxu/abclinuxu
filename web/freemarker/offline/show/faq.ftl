<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents/>

<h1 style="margin-bottom:1em;">${TOOL.xpath(ITEM, "/data/title")}</h1>

<div>
    ${TOOL.render(TOOL.xpath(ITEM.data,"data/text"), USER?if_exists)}
</div>

<#if XML.data.links[0]?exists>
<div class="cl_perex">
  <h3>Související odkazy</h3>
    <div class="s_sekce">
        <ul>
	    <#list XML.data.links.link as link>
    	        <li>
        	    <a href="${link.@url}">${link}</a>
        	</li>
    	    </#list>
	</ul>
    </div>
</div>
</#if>

<#include "../footer.ftl">
