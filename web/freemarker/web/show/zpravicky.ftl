<#include "../header.ftl">

<script language="javascript1.2" type="text/javascript">
    stav = true;
    function toggle(sender) {
        stav = !stav;
        if (sender.form.elements.length) {
            for (var i = 0; i < sender.form.elements.length; i++) {
                if (sender.form.elements[i].type == 'checkbox') {
                    sender.form.elements[i].checked = stav;
                }
            }
        }
    }
</script>

<h1>Zprávičky</h1>

<#list NEWS as rel>
 <@lib.showNews rel /><hr />
</#list>

<p>
<a href="/History?type=news&amp;from=${NEWS?size}&amp;count=15" title="Další">Starší zprávičky</a> &#8226;
<a href="${URL.make("/zpravicky/edit?action=add")}">Přidat zprávičku</a>
</p>

<div align="center"><img src="/images/site/wap.gif" width="181" height="52" alt="wap.abclinuxu.cz"><br><br></div>

<form action="/zpravicky/hledani" method="POST">
 <input type="text" name="dotaz" size="30" tabindex="1">
 <input type="submit" class="button" value="Prohledej zprávičky" tabindex="2">

  <table>
   <#list CATEGORIES as category>
    <#if category_index%3==0><tr></#if>
     <td>
        <label>
            <input type="checkbox" name="category" value="${category.key}" <#if category.set>checked</#if>>
            ${category.name}
        </label>
     </td>
    <#if category_index%3==2></tr></#if>
   </#list>
   <tr><td colspan="3"><button type="button" onclick="toggle(this)">Vše/nic</button></td></tr>
  </table>

 <input type="hidden" name="parent" value="42932">
 <input type="hidden" name="type" value="zpravicka">
</form>


<#include "../footer.ftl">
