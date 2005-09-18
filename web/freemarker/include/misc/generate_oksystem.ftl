<div class="s_nad_h1"><div class="s_nad_pod_h1" style="background-color: #00A274">
<h1 style="background-color: #00A274"><a href="http://portal.oksystem.cz/">©kolení a vzdìlání</a></h1>
<div align="right">OKsystem</div>
</div></div>
<div class="s_sekce">
    <ul>
    <#list ITEMS as item>
        <li><a href="${item.url}" title="${item.description?html}">${item.title}</a></li>
    </#list>
    </ul>
    <form action="http://portal.oksystem.cz/">
        <input type="text" size="12">
        <input type="submit" value="Hledej">
    </form>
</div>

