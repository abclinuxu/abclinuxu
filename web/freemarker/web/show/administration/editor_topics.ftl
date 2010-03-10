<#assign html_header>
<script type="text/javascript">
    $(document).ready(function() {
     $('input:checkbox').click(function() {
            var buttonsChecked = $('input:checkbox:checked');
            if (buttonsChecked.length) {
                $('#notifyButton').removeAttr('disabled');
                }
            else {
                $('#notifyButton').attr('disabled', 'disabled');
                }
            });
        });
</script>
</#assign>

<#include "../../header.ftl">

<@lib.showSignPost "Rozcestník">
<ul>
    <li><a href="${URL.make("/sprava/redakce/namety/edit?action=add")}">Přidat námět</a></li>
</ul>
</@lib.showSignPost>

<@lib.showMessages/>

<h3>Náměty</h3>

<p>
    Seznam všech námětů. Pokud námět není přiřazen ke konkrétnímu autorovi, pak je veřejný a kterýkoliv
    autor si jej smí rezervovat. Stav článku ukazuje, zda již byl námět asociován s nějakým článkem a
    pokud ano, v jakém je článek stavu. Seznam v iniciální konfiguraci skrývá témata, jejichž článek
    byl již publikován.
</p>


<table class="siroka list">
    <thead>
    <tr>
        <th>&nbsp;</th>
        <th style="text-align: left">Název</th>
        <th>Veřejný</th>
        <th>Autor</th>
        <th>Termín</th>
        <th>Stav článku</th>
        <th>Honorář</th>
        <th>&nbsp;</th>
    </tr>
    </thead>
    <tbody>
    <form action="${URL.noPrefix("/sprava/redakce/namety")}" method="POST">
        <tr>
            <td>&nbsp;</td>
            <td style="text-align: left">
                <@lib.filterInput filter=FILTER name="filterTopicsByTitle" size="25"/>
            </td>
            <td>
                <select name="filterTopicsByPublic">
                    <@lib.filterOption filter=FILTER name="filterTopicsByPublic" value=""></@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByPublic" value="1">ano</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByPublic" value="0">ne</@lib.filterOption>
                </select>
            </td>
            <td>
                <select name="filterTopicsByAuthor">
                    <@lib.filterOption filter=FILTER name="filterTopicsByAuthor" value=""></@lib.filterOption>
                    <#list AUTHORS as author>
                          <@lib.filterOption filter=FILTER name="filterTopicsByAuthor" value="${author.relationId}">${author.title}</@lib.filterOption>
                    </#list>
                </select>
            </td>
            <td>
                <select name="filterTopicsByDeadline">
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value=""></@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="-1">žádný</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="0">v prodlení</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="0-1">tento měsíc</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByDeadline" value="1-2">příští měsíc</@lib.filterOption>
                </select>
            </td>
            <td>
                <select name="filterTopicsByState">
                    <@lib.filterOption filter=FILTER name="filterTopicsByState" value="-1">nevydaný</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByState" value="1">rozepsaný</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByState" value="4">odeslaný</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByState" value="3">přijatý</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByState" value="2">připravený</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByState" value="0">vydaný</@lib.filterOption>
                </select>
            </td>
            <td>
                <select name="filterTopicsByRoyalty">
                    <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value=""></@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value="0">běžný</@lib.filterOption>
                    <@lib.filterOption filter=FILTER name="filterTopicsByRoyalty" value="1">jiný</@lib.filterOption>
                </select>
            </td>
            <td style="text-align: left">
                <input type="submit" name="list" value="Filtruj"/>
            </td>
        </tr>
        <#list FOUND.data as topic>
            <tr>
                <td>
                    <@lib.filterCheckBox filter=FILTER name="trid" value="${topic.relationId}"></@lib.filterCheckBox>
                </td>
                <td style="text-align: left;">
                	<a href="${URL.make("/sprava/redakce/namety/" + topic.relationId)}">${(topic.title)!?html}</a>
                </td>
                <td>${(topic.isPublic())!?string("ano","ne")}</td>
                <td>
                    <#if (topic.author)?? >
                        <@lib.showAuthor topic.author />
                    </#if>
                </td>
                <td style="text-align: right">
                    <#if (topic.deadline)??>
                        <#assign deadlineStyle="">
                        <#if topic.delayed && (topic.articleState == "NONE" || topic.articleState == "DRAFT")>
                            <#assign deadlineStyle=" style=\"color: red\"">
                        </#if>
                        <span${deadlineStyle!}>${DATE.show(topic.deadline, "CZ_DMY")}</span>
                    </#if>
                </td>
                <td>
                    <#if topic.articleState == "NONE">
                        žádný
                    <#else>
                        <a href="${URL.url(topic.article)}">
                        <#switch topic.articleState>
                            <#case "DRAFT">rozepsaný<#break>
                            <#case "SUBMITTED">odeslaný<#break>
                            <#case "ACCEPTED">přijatý<#break>
                            <#case "READY">připravený<#break>
                            <#case "PUBLISHED">vydaný
                        </#switch>
                        </a>
                    </#if>
                </td>
                <td>${(topic.royalty)!"běžný"}</td>
                <td style="white-space: nowrap; text-align: left">
                    <a href="${URL.make("/sprava/redakce/namety/edit/${topic.relationId}?action=edit")}">upravit</a>&nbsp;
                    <a href="${URL.make("/sprava/redakce/namety/edit/${topic.relationId}?action=rm")}">smazat</a>
                </td>
            </tr>
        </#list>
        <#if FOUND.data?has_content>
            <tr>
                <td colspan="7">&nbsp;</td>
                <td style="text-align: left">
                    <input type="submit" name="notify" id="notifyButton" value="Notifikace" disabled="disabled"/>
                </td>
            </tr>
        </#if>
    </tbody>
</table>
</form>

<div>
    <#if FOUND.prevPage??>
    <a href="${URL_BEFORE_FROM}0${URL_AFTER_FROM}">0</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${FOUND.prevPage.row}${URL_AFTER_FROM}">&lt;&lt;</a>
    <#else>
    0 &lt;&lt;
    </#if>
    ${FOUND.thisPage.row} - ${FOUND.thisPage.row+FOUND.thisPage.size}&nbsp;
    <#if FOUND.nextPage??>
    <a href="${URL_BEFORE_FROM}${FOUND.nextPage.row?string["#"]}${URL_AFTER_FROM}">&gt;&gt;</a>
    &nbsp;<a href="${URL_BEFORE_FROM}${(FOUND.total - FOUND.pageSize)?string["#"]}${URL_AFTER_FROM}">${FOUND.total}</a>
    <#else>
    &gt;&gt; ${FOUND.total}
    </#if>
</div>
        <#include "../../footer.ftl">
