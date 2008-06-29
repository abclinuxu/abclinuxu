<#assign html_header>
    <script type="text/javascript" src="/data/site/search.js"></script>
    <script language="javascript1.2" type="text/javascript">
    <!--
        var doctypeSet = new MultipleChoiceState(false);
        function resetSelect(obj)
	{
		for(var i=0;i<obj.length; i++)
			obj.options[i].selected = false;
	}
        function cancelSearchOptions()
	{
		var form = document.getElementById('jobSearch');
		resetSelect(document.getElementById('jobLocality'));
		resetSelect(document.getElementById('jobSkill'));
		form.submit();
	}
    // -->
    </script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nabídka volných pracovních pozic serveru jobs.cz</h1>

<form id="jobSearch" action="/jobs">
    <table border="0" width="60%">
        <tr>
            <td>
                <select name="locality" id="jobLocality" multiple size="6">
                <#list LOCS as loc>
                    <option value="${loc.locality}" <#if loc.set>selected="selected"</#if>>${loc.locality}</option>
                </#list>
            </td>
            <td>
                <select name="skill" id="jobSkill" multiple size="6">
                <#list SKILLS as skill>
                    <option value="${skill.skill}" <#if skill.set>selected="selected"</#if>>${skill.skill}</option>
                </#list>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="submit" value="Vybrat"/>&nbsp;
                <input type="button" value="Zrušit filtr" onclick="javascript: cancelSearchOptions();" />
            </td>
        </tr>
    </table>
</form>

<hr />

<#list JOBS.data as job>
    <table class="jobTable siroka">
        <tr>
            <td colspan="2">
            <span class="st_nadpis"><b><a href="${job.url}?fc=a-jobs&amp;fg=a-produkt&amp;fs=abclinuxu.cz&amp;fm=aliance&amp;ff=box&amp;fi=stickfish&amp;fb=it" rel="nofollow">${job.positionName}</a></b></span>
            <td>
        </tr>
        <tr>
            <td style="width:70%;"><small>${job.companyName}</small></td>
            <td style="text-align:right">${job.createDate}</td>
        </tr>
        <tr>
            <td style="text-align:right" colspan="2">
            <#list job.localities as locality>
                ${locality}<#if locality_has_next>,</#if>
            </#list>
            </td>
        </tr>
    </table>
    <hr />
</#list>


<#if JOBS.prevPage?exists>
    <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${JOBS.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>
    0 &lt;&lt;
</#if>
${JOBS.thisPage.row} - ${JOBS.thisPage.row+JOBS.thisPage.size}&nbsp;
<#if JOBS.nextPage?exists>
    <a href="${URL_BEFORE_FROM}${JOBS.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${(JOBS.total - JOBS.pageSize)?string["#"]}${URL_AFTER_FROM}">${JOBS.total}</a>
<#else>
    &gt;&gt; ${JOBS.total}
</#if>

<#include "../footer.ftl">