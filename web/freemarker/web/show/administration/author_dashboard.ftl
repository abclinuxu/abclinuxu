<#include "../../header.ftl">

<@lib.showMessages/>

<@lib.showSignPost "Rozcestník">
<ul>
  <li>
      <a href="${URL.make("/autori/clanky/?action=add")}" title="Napsat článek">Napsat článek</a> N/A
  </li>
    <#if IS_EDITOR || IS_EDITOR_IN_CHIEF>
        <li>
            <a href="/sprava/redakce/?action=switch">Změnit roli</a>
        </li>
    </#if>
</ul>			
</@lib.showSignPost>

<p>
    Vítejte v redakčním systému. Zde můžete psát nové články, kontrolovat své honoráře,
    prohlížet náměty, upravovat osobní údaje atd.
</p>

<ul>
    <li><a href="/sprava/redakce/clanky">Mé články</a> N/A</li>
    <li><a href="/sprava/redakce/namety">Náměty</a></li>
    <li><a href="/sprava/redakce/honorare"">Mé honoráře</a> N/A</li>
    <li><a href="/sprava/redakce/autori/edit/${AUTHOR.relationId}?action=editSelf">Osobní údaje</a></li>
    <li>
        <a href="/sprava/redakce/smlouvy">Autorské smlouvy</a>
        <#if UNSIGNED_CONTRACT> – <b>nová smlouva čeká na schválení</b></#if>
    </li>
</ul>

<h2>Chystané články</h2>

<h2>Plánováné náměty</h2>

<#if TOPICS?? >
    <table>
        <tr>
            <th>Název</th>
            <th>Termín odevzdání</th>
        </tr>
        <#list TOPICS as topic>
            <tr>
                <td style="text-align: left;">
                    <a href="${URL.make("/sprava/redakce/namety/" + topic.relationId)}">${(topic.title)!?html}</a>
                </td>
                <td style="text-align: right;">
                    <#assign deadlineStyle=""><#if topic.delayed><#assign deadlineStyle=" style=\"color: red\""></#if>
                    <span${deadlineStyle!}>${DATE.show(topic.deadline, "CZ_DMY")}</span>
                </td>
            </tr>
        </#list>
    </table>
<#else>
	<p>Nemáte žádné čekající náměty.</p>
</#if>

<#include "../../footer.ftl">