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

<h1 class="st_nadpis">Zpr�vi�ky</h1>

<p>Zpr�vi�ky m��ete ��st i p�es mobil: wap.abclinuxu.cz</p>

<p><a href="${URL.make("/news/edit?action=add")}">P�idat zpr�vi�ku</a></p>

<#list NEWS as rel>
 <@lib.showNews rel /><hr>
</#list>

<p>
<a href="/History?type=news&from=${NEWS?size}&count=15" title="Dal��">Star�� zpr�vi�ky</a>
</p>

<div align="center"><img src="/images/site/wap.gif" width="181" height="52" alt="wap.abclinuxu.cz"><br><br></div>

<form action="/Search" method="POST">
 <input type="text" name="query" size="30" tabindex="1">
 <input type="submit" value="Prohledej zpr�vi�ky" tabindex="2">

  <table>
   <#list CATEGORIES as category>
    <#if category_index%3==0><tr></#if>
     <td>
      <input type="checkbox" name="category" value="${category.key}" <#if category.set>checked</#if>>${category.name}
     </td>
    <#if category_index%3==2></tr></#if>
   </#list>
   <tr><td colspan="3"><button type="button" onclick="toggle(this)">V�e/nic</button></td></tr>
  </table>

 <input type="hidden" name="parent" value="42932">
 <input type="hidden" name="type" value="zpravicka">
</form>


<#include "../footer.ftl">
