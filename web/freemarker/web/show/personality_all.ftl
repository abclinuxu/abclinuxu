<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="${URL.make("/kdo-je/edit?action=add")}">Vložit novou osobnost</a>
            </li>
            <li>
                <@lib.showMonitor RELATION "Zašle upozornění na váš email při nové položce v této a v podřazených sekcích."/>
            </li>
        </ul>
    </div>
</#assign>
<#include "../header.ftl">

<h1>Kdo je</h1>

<p>
    Služba Kdo je kdo shromažďuje informace u důležitých osobnostech světa open source
    a Linuxu. Pokud zde někdo důležitý chybí, můžete
    <a href="/kdo-je/edit?action=add">vložit novou osobnost</a>.
</p>

<form action="/hledani" method="GET">
    <input type="text" name="dotaz" size="30" tabindex="1">
    <input type="hidden" name="typ" value="osobnost">
    <input type="submit" value="Hledej mezi osobnostmi" class="button" tabindex="2">
</form>

<p class="dict-abc">
    Filtr:
    <a href="/kdo-je?prefix=a"<@highlight 'a'/>>A</a>
    <a href="/kdo-je?prefix=b"<@highlight 'b'/>>B</a>
    <a href="/kdo-je?prefix=c"<@highlight 'c'/>>C</a>
    <a href="/kdo-je?prefix=d"<@highlight 'd'/>>D</a>
    <a href="/kdo-je?prefix=e"<@highlight 'e'/>>E</a>
    <a href="/kdo-je?prefix=f"<@highlight 'f'/>>F</a>
    <a href="/kdo-je?prefix=g"<@highlight 'g'/>>G</a>
    <a href="/kdo-je?prefix=h"<@highlight 'h'/>>H</a>
    <a href="/kdo-je?prefix=i"<@highlight 'i'/>>I</a>
    <a href="/kdo-je?prefix=j"<@highlight 'j'/>>J</a>
    <a href="/kdo-je?prefix=k"<@highlight 'k'/>>K</a>
    <a href="/kdo-je?prefix=l"<@highlight 'l'/>>L</a>
    <a href="/kdo-je?prefix=m"<@highlight 'm'/>>M</a>
    <a href="/kdo-je?prefix=n"<@highlight 'n'/>>N</a>
    <a href="/kdo-je?prefix=o"<@highlight 'o'/>>O</a>
    <a href="/kdo-je?prefix=p"<@highlight 'p'/>>P</a>
    <a href="/kdo-je?prefix=q"<@highlight 'q'/>>Q</a>
    <a href="/kdo-je?prefix=r"<@highlight 'r'/>>R</a>
    <a href="/kdo-je?prefix=s"<@highlight 's'/>>S</a>
    <a href="/kdo-je?prefix=t"<@highlight 't'/>>T</a>
    <a href="/kdo-je?prefix=u"<@highlight 'u'/>>U</a>
    <a href="/kdo-je?prefix=v"<@highlight 'v'/>>V</a>
    <a href="/kdo-je?prefix=w"<@highlight 'w'/>>W</a>
    <a href="/kdo-je?prefix=x"<@highlight 'x'/>>X</a>
    <a href="/kdo-je?prefix=y"<@highlight 'y'/>>Y</a>
    <a href="/kdo-je?prefix=z"<@highlight 'z'/>>Z</a>
</p>

<table border="0" class="siroka bez-slovniku">
 <#list FOUND.data as rel>
  <#if rel_index % 3 == 0><tr></#if>
   <td><a href="${rel.url}">${TOOL.childName(rel.child)}</a></td>
  <#if rel_index % 3 == 2></tr></#if>
 </#list>
</table>

<#macro highlight(letter)>
    <#if (CURRENT_PREFIX?default('a')==letter)> class="selected"</#if><#rt>
</#macro>

<#include "../footer.ftl">

