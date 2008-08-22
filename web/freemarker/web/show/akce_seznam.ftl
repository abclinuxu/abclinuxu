<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>
    <#if SUBPORTAL?exists>
        <@lib.showSubportal SUBPORTAL, true/>
        <#assign counter=VARS.getSubportalCounter(SUBPORTAL)>
    </#if>
    <div class="s_nadpis">Nástroje</div>
    <div class="s_sekce">
        <ul>
        <li><a href="/akce/edit/${RELATION.id}?action=add">Přidat akci</a></li>
        <#if USER?exists && TOOL.permissionsFor(USER, RELATION).canModify()>
            <li>
                <a href="?mode=unpublished">Čekající akce</a>
                <span>
                    (<#if counter?exists>${counter.WAITING_EVENTS}<#else>${VARS.counter.WAITING_EVENTS}</#if>)
                </span>
            </li>
        </#if>
        </ul>
    </div>
    <#if "unpublished"!=PARAMS.mode?default("UNDEF")>
        <#assign subtype=PARAMS.subtype?default("")>
        <div class="s_nadpis">Kalendář</div>
        <div class="s_sekce">
            <table width="100%" class="calendar">
            <tr>
                <td colspan="2"><a rel="nofollow" href="?year=${CALENDAR.prevYear}&amp;month=${CALENDAR.prevMonth}&amp;subtype=${subtype}">&laquo; <@lib.month CALENDAR.prevMonth.toString()/></a></td>
                <td colspan="3">
                    <#if !PARAMS.day?exists && PARAMS.month?exists && ""+CALENDAR.month==PARAMS.month>
                        <span class="cal_selected">
                    <#else>
                        <span>
                    </#if>
    
                        <a rel="nofollow" href="?year=${CALENDAR.year}&amp;month=${CALENDAR.month}&amp;subtype=${subtype}"><@lib.month ""+CALENDAR.month/></a>
                    </span>
    
                    <#if !PARAMS.month?exists && !PARAMS.day?exists && PARAMS.year?exists && ""+CALENDAR.year==PARAMS.year>
                        <span class="cal_selected">
                    <#else>
                        <span>
                    </#if>
    
                    <a rel="nofollow" href="?year=${CALENDAR.year}&amp;subtype=${subtype}">
                        ${CALENDAR.year}
                    </a>
                    </span>
                </td>
                <td colspan="2"><a rel="nofollow" href="?year=${CALENDAR.nextYear}&amp;month=${CALENDAR.nextMonth}&amp;subtype=${subtype}"><@lib.month ""+CALENDAR.nextMonth/> &raquo;</a></td>
            </tr>
            <tr style="font-weight: bold"><td>Po</td><td>Út</td><td>St</td><td>Čt</td><td>Pá</td><td>So</td><td>Ne</td></tr>
            <tr>
                <@lib.repeat CALENDAR.emptyDays><td>&nbsp;</td></@lib.repeat>
                <#list 1..CALENDAR.days as curday>
                    <#if (CALENDAR.emptyDays+curday)%7==1></tr><tr></#if>
    
                    <#assign id="UNDEF", class="UNDEF">
                    <#if CALENDAR.today?exists && curday==CALENDAR.today>
                        <#assign id="today">
                    </#if>
                    <#if PARAMS.day?exists && ""+curday==PARAMS.day>
                        <#assign class="cal_selected">
                    </#if>
                    <#if CALENDAR.eventDays[curday-1]>
                        <#if class!="UNDEF">
                            <#assign class="cal_event "+class>
                        <#else>
                            <#assign class="cal_event">
                        </#if>
                    </#if>
                    <td <#if id!="UNDEF">id="${id}"</#if> <#if class!="UNDEF">class="${class}"</#if>>
                        <a rel="nofollow" href="?year=${CALENDAR.year}&amp;month=${CALENDAR.month}&amp;day=${curday}&amp;subtype=${subtype}">${curday}</a>
                    </td>
                </#list>
            </tr>
            </table>
        </div>
    
        <div class="s_nadpis">Časová osa</div>
        <div class="s_sekce">
        <ul>
            <li <#if "everything"==PARAMS.mode?default("UNDEF")>class="cal_selected"</#if>>
                    <a href="?mode=everything&amp;subtype=${subtype}">Všechny akce</a>
            </li>
            <li <#if "upcoming"==PARAMS.mode?default("UNDEF")>class="cal_selected"</#if>>
                    <a href="?mode=upcoming&amp;subtype=${subtype}">Nadcházející akce</a>
            </li>
            <li <#if "old"==PARAMS.mode?default("UNDEF")>class="cal_selected"</#if>>
                    <a href="?mode=old&amp;subtype=${subtype}">Proběhlé akce</a>
            </li>
        </ul>
        </div>
    
        <div class="s_nadpis">Druh akce</div>
        <div class="s_sekce">
            <#if PARAMS.mode?exists>
                <#assign url="mode="+PARAMS.mode>
            <#else>
                <#assign url="">
                <#if PARAMS.year?exists>
                    <#assign url="year="+PARAMS.year>
                    <#if PARAMS.month?exists>
                        <#assign url=url+"&amp;month="+PARAMS.month>
                        <#if PARAMS.day?exists>
                            <#assign url=url+"&amp;day="+PARAMS.day>
                        </#if>
                    </#if>
                </#if>
            </#if>
    
            <ul>
            <li <#if subtype=="">class="cal_selected"</#if>><a href="/akce?${url}">Všechny druhy</a></li>
            <hr />
            <li <#if subtype=="community">class="cal_selected"</#if>><a href="/akce?${url}&amp;subtype=community">Komunitní</a></li>
            <li <#if subtype=="educational">class="cal_selected"</#if>><a href="/akce?${url}&amp;subtype=educational">Vzdělávací</a></li>
            <li <#if subtype=="company">class="cal_selected"</#if> ><a href="/akce?${url}&amp;subtype=company">Firemní</a></li>
            </ul>
        </div>
    </#if>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#global CITACE = TOOL.getRelationCountersValue(ITEMS.data,"read")/>

<#list ITEMS.data as event>
    <@lib.showEvent event, true, "unpublished"==PARAMS.mode?default("UNDEF")/>
    <hr />
</#list>

<#if ITEMS.data?size==0>
    <h2>Žádné akce!</h2>
</#if>

<#if (ITEMS.currentPage.row > 0) >
    <#assign start=ITEMS.currentPage.row-ITEMS.pageSize><#if (start<0)><#assign start=0></#if>
    <li>
        <a href="/akce?from=${start}&amp;count=${ITEMS.pageSize}">Novější akce</a>
    </li>
</#if>
<#assign start=ITEMS.currentPage.row + ITEMS.pageSize>
<#if (start < ITEMS.total) >
    <li>
        <a href="/akce?from=${start}&amp;count=${ITEMS.pageSize}">Starší akce</a>
    </li>
</#if>

<#include "../footer.ftl">
