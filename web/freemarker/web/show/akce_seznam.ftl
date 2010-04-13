<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>

    <@lib.advertisement id="hypertext2nahore" />

    <#if SUBPORTAL??>
        <@lib.showSubportal SUBPORTAL, true/>
        <#assign counter=VARS.getSubportalCounter(SUBPORTAL)>
    </#if>
    <div class="s_nadpis">Nástroje</div>
    <div class="s_sekce">
        <ul>
        <li><a href="/akce/edit/${RELATION.id}?action=add">Přidat akci</a></li>
        <#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
            <li>
                <a href="?mode=unpublished">Čekající akce</a>
                <span>
                    (<#if counter??>${counter.WAITING_EVENTS}<#else>${VARS.counter.WAITING_EVENTS}</#if>)
                </span>
            </li>
        </#if>
        </ul>
    </div>
    <#if "unpublished"!=PARAMS.mode!"UNDEF">
        <#assign subtype=PARAMS.subtype!""?html>
        <div class="s_nadpis">Kalendář</div>
        <div class="s_sekce">
            <table cellspacing="0" class="eventCalendar">
            <tr>
                <td colspan="2">
                  <a rel="nofollow" href="?year=${CALENDAR.prevYear}&amp;month=${CALENDAR.prevMonth}&amp;subtype=${subtype}">&laquo; <@lib.month CALENDAR.prevMonth.toString()/></a></td>
                <td colspan="3" class="month_year">
                    <#if !PARAMS.day?? && PARAMS.month?? && ""+CALENDAR.month==PARAMS.month>
                      <span class="selected">
                    <#else>
                      <span>
                    </#if>
                        <a rel="nofollow" href="?year=${CALENDAR.year}&amp;month=${CALENDAR.month}&amp;subtype=${subtype}"><@lib.month ""+CALENDAR.month/></a>
                      </span>

                    <#if !PARAMS.month?? && !PARAMS.day?? && PARAMS.year?? && ""+CALENDAR.year==PARAMS.year>
                      <span class="selected">
                    <#else>
                      <span>
                    </#if>
                        <a rel="nofollow" href="?year=${CALENDAR.year}&amp;subtype=${subtype}">${CALENDAR.year}</a>
                      </span>
                </td>
                <td colspan="2"><a rel="nofollow" href="?year=${CALENDAR.nextYear}&amp;month=${CALENDAR.nextMonth}&amp;subtype=${subtype}"><@lib.month ""+CALENDAR.nextMonth/> &raquo;</a></td>
            </tr>
            <tr class="weekdays"><td>Po</td><td>Út</td><td>St</td><td>Čt</td><td>Pá</td><td>So</td><td>Ne</td></tr>
            <tr>
                <@lib.repeat CALENDAR.emptyDays><td>&nbsp;</td></@lib.repeat>
                <#list 1..CALENDAR.days as curday>
                    <#if (CALENDAR.emptyDays+curday)%7==1></tr><tr></#if>
                    <#assign id="UNDEF", class="UNDEF">
                    <#if CALENDAR.today?? && curday==CALENDAR.today>
                        <#assign id="today">
                    </#if>
                    <#if PARAMS.day?? && ""+curday==PARAMS.day>
                        <#assign class="selected">
                    </#if>
                    <#if CALENDAR.eventDays[curday-1]>
                        <#if class!="UNDEF">
                            <#assign class="event_day "+class>
                        <#else>
                            <#assign class="event_day">
                        </#if>
                    </#if>
                    <td<#if id!="UNDEF"> id="${id}"</#if><#if class!="UNDEF"> class="${class}"</#if>><#--
                    --><a rel="nofollow" href="?year=${CALENDAR.year}&amp;month=${CALENDAR.month}&amp;day=${curday}&amp;subtype=${subtype}">${curday}</a><#--
                 --></td>
                </#list>
            </tr>
            </table>
            <hr />
            Legenda:<br />
            <table cellspacing="0" class="eventCalendar legenda">
               <tr><td class="event_day"><a href="">XY</a></td><td>den s akcí</td></tr>
               <tr><td id="today"><a href="">XY</a></td><td>dnešní den</td></tr>
               <tr><td class="selected"><a href="">XY</a></td><td>zvolené datum/typ akce</td></tr>
            </table>
        </div>

        <@lib.advertisement id="square" />

        <div class="s_nadpis">Časová osa</div>
        <div class="s_sekce">
        <ul class="event_time">
            <li <#if "everything"==PARAMS.mode!"UNDEF">class="selected"</#if>>
                    <a href="?mode=everything&amp;subtype=${subtype}">Všechny akce</a>
            </li>
            <li <#if "upcoming"==PARAMS.mode!"UNDEF">class="selected"</#if>>
                    <a href="?mode=upcoming&amp;subtype=${subtype}">Nadcházející akce</a>
            </li>
            <li <#if "old"==PARAMS.mode!"UNDEF">class="selected"</#if>>
                    <a href="?mode=old&amp;subtype=${subtype}">Proběhlé akce</a>
            </li>
        </ul>
        </div>

        <div class="s_nadpis">Druh akce</div>
        <div class="s_sekce">
            <#if PARAMS.mode??>
                <#assign url="mode="+PARAMS.mode>
            <#else>
                <#assign url="">
                <#if PARAMS.year??>
                    <#assign url="year="+PARAMS.year>
                    <#if PARAMS.month??>
                        <#assign url=url+"&amp;month="+PARAMS.month>
                        <#if PARAMS.day??>
                            <#assign url=url+"&amp;day="+PARAMS.day>
                        </#if>
                    </#if>
                </#if>
            </#if>

            <ul class="event_type">
            <li <#if subtype=="">class="selected"</#if>><a href="/akce?${url}">Všechny druhy</a></li>
            <hr />
            <li <#if subtype=="community">class="selected"</#if>><a href="/akce?${url}&amp;subtype=community">Komunitní</a></li>
            <li <#if subtype=="educational">class="selected"</#if>><a href="/akce?${url}&amp;subtype=educational">Vzdělávací</a></li>
            <li <#if subtype=="company">class="selected"</#if> ><a href="/akce?${url}&amp;subtype=company">Firemní</a></li>
            </ul>
        </div>
    </#if>

    <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#list ITEMS.data as event>
    <@lib.showEvent event, true, "unpublished"==PARAMS.mode!"UNDEF"/>
    <hr />
</#list>

<#if ITEMS.data?size==0>
    <h2>Nenalezeny žádné akce pro váš výběr</h2>
</#if>

<#if (ITEMS.currentPage.row > 0) >
    <#assign start=ITEMS.currentPage.row-ITEMS.pageSize><#if (start<0)><#assign start=0></#if>
    <li>
        <a href="/akce?from=${start}&amp;count=${ITEMS.pageSize}&amp;subtype=${PARAMS.subtype!}&amp;mode=${PARAMS.mode!}">Novější akce</a>
    </li>
</#if>
<#assign start=ITEMS.currentPage.row + ITEMS.pageSize>
<#if (start < ITEMS.total) >
    <li>
        <a href="/akce?from=${start}&amp;count=${ITEMS.pageSize}&amp;subtype=${PARAMS.subtype!}&amp;mode=${PARAMS.mode!}">Starší akce</a>
    </li>
</#if>

<#include "../footer.ftl">
