<#assign html_header>
    <style type="text/css">
        table.jobTable {
            width: 100%;
            padding: 0px;
            margin: 0px;
            margin-bottom: 5px;
            border-bottom: 1px dotted blue;
        }
        td.right {
            text-align: right;
        }
    </style>
    <script type="text/javascript" src="/data/site/search.js"></script>
    <script language="javascript1.2" type="text/javascript">
    <!--
        var doctypeSet = new MultipleChoiceState(false);
    // -->
    </script>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nabídka volných pracovních pozic serveru jobs.cz</h1>

<form action="/jobs">
    <table border="0" width="60%">
        <tr>
            <td>
                <select name="locality" multiple size="6">
                <#list LOCS as loc>
                    <option value="${loc.locality}" <#if loc.set>selected="selected"</#if>>${loc.locality}</option>
                </#list>    
            </td>    
            <td>
                <select name="skill" multiple size="6">
                <#list SKILLS as skill>
                    <option value="${skill.skill}" <#if skill.set>selected="selected"</#if>>${skill.skill}</option>
                </#list> 
            </td>
        </tr>    
        <tr>
            <td colspan="2" class="right">
                <input type="submit" value="Vybrat"/>
                <input type="reset" />
            </td>
        </tr>
    </table>
</form>

<hr />

<#list JOBS.data as job>
    <table class="jobTable">
        <tr>
            <td colspan="2">
            <a href="http://www.${job.url}?fc=a-jobs&amp;fg=a-produkt&amp;fs=abclinuxu.cz&amp;fm=aliance&amp;ff=box&amp;fi=stickfish&amp;fb=it" rel="nofollow">${job.positionName}</a>
            <td>
        </tr>
        <tr>
            <td style="width:70%;">${job.companyName}</td>
            <td class="right">${job.createDate}</td>
        </tr>
        <tr>
            <td class="right" colspan="2">
            <#list job.localities as locality>
                ${locality}<#if locality_has_next>,</#if>
            </#list>
            </td>
        </tr>
    </table>
</#list>


<#if JOBS.prevPage?exists>
    <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
    <a href="${URL_BEFORE_FROM}${JOBS.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
<#else>
    0 &lt;&lt;
</#if>
${JOBS.thisPage.row}-${JOBS.thisPage.row+JOBS.thisPage.size}
<#if JOBS.nextPage?exists>
    <a href="${URL_BEFORE_FROM}${JOBS.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
    <a href="${URL_BEFORE_FROM}${(JOBS.total - JOBS.pageSize)?string["#"]}${URL_AFTER_FROM}">${JOBS.total}</a>
<#else>
    &gt;&gt; ${JOBS.total}
</#if>

<#include "../footer.ftl">