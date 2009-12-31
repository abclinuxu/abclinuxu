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
    <li><a href="/sprava/redakce/autori/edit/${AUTHOR.id}?action=edit">Osobní údaje</a></li>
    <li><a href="/sprava/redakce/smlouvy">Autorské smlouvy</a></li>
</ul>

<h2>Chystané články</h2>

<h2>Plánováné náměty</h2>

<#if TOPICS?? >
<table class="siroka">
    <tr>
        <th>Název</th>
        <th>Termín odevzdání</th>
        <th>Popis</th>
    </tr>
    <#list TOPICS as topic>
        <tr>
            <td style="text-align: left; vertical-align: top">${(topic.title)!?html}</td>
            <td style="vertical-align: top">
                <#if topic.isInDelay() ><span style="color: red"></#if>
                ${DATE.show(topic.deadline, "CZ_DMY")}
                <#if topic.isInDelay()></span></#if>
            </td>
            <td>
                <textarea rows="5" cols="60" style="font-family: inherit; border: none; background: inherit;">
                    ${(topic.description)!?html}
                </textarea>
            </td>
        </tr>
    </#list>
</table>
<#else>
	<p>Nemáte přiřazené žádné náměty.</p>
</#if>

<#include "../../footer.ftl">