<#include "../header.ftl">

<!-- boxiky -->

  <div class="hp">

   <div class="hpboxec">
      <div class="hptit"><div class="nolink">Reklama</div></div>
	<#include "/include/box_index.txt">
   </div>

    <div class="hpbox2">
      <div class="hptit"><a href="/hardware/dir/1"><img src="/images/site2/hpic/hpic-hw.gif"> Hardware</a></div>
      <div class="hpbody">
       <ul>
          <#list VARS.newHardware as rel>
          <li><a href="/hardware/show/${rel.id}">${TOOL.xpath(rel.child,"data/name")}</a></li>
          </#list>
       </ul>
      </div>
    </div>

    <div class="hpbox1">
      <div class="hptit"><a href="/drivers/dir/318"><img src="/images/site2/hpic/hpic-ovladac.gif"> Ovladaèe</a>
	  </div>
      <div class="hpbody">
       <ul>
            <#list VARS.newDrivers as rel>
             <li><a href="/drivers/show/${rel.id}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
       </ul>
     </div>
    </div>

    <div class="hpbox2">
      <div class="hptit"><a href="/slovnik"><img src="/images/site2/hpic/hpic-slovnik.gif"> Výkladový slovník</a>
	  </div>
      <div class="hpbody">
       <ul>
            <#list DICTIONARY as rel>
            <li><a href="/slovnik/${rel.child.subType}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
	     <p></p>
            <li><a href="${URL.make("/slovnik/edit?action=add")}">Pøidat nový pojem</a></li>
       </ul>
      </div>
    </div>

    <div class="hpbox2">
      <div class="hptit"><a href="ftp://ftp.fi.muni.cz/pub/linux/kernel"><img src="/images/site2/hpic/hpic-stable.gif"> Kernel</a></div>
      <div class="hpbody">
        <#include "/include/kernel.txt">
      </div>
    </div>

    <div class="hpbox1">
      <div class="hptit"><a href="/clanky/show/26394"><img src="/images/site2/hpic/hpic-linux.gif"> Co je to Linux?</a></div>
      <div class="hpbody">
       <ul>
          <li><a href="/clanky/show/12707">Je opravdu zdarma?</a></li>
          <li><a href="/clanky/show/9503">Co jsou to distribuce?</a></li>
          <li><a href="/clanky/show/14665">Náhrady Windows aplikací</a></li>
          <li><a href="/clanky/show/20310">Rozcestník na¹ich seriálù</a></li>
       </ul>
      </div>
    </div>

    <div class="hpbox4">
      <div class="hptit"><a href="http://www.unixshop.cz" target="_blank"><img src="/images/site2/hpic/hpic-unixshop.gif"> unixshop.cz</a></div>
      <div class="hpbody">
        <#include "/include/unixshop.txt">
      </div>
    </div>

  <br class="ac">

 <#include "/include/zprava.txt">
 <@lib.showMessages/>

 <#list ARTICLES as rel>
  <@lib.showArticle rel, "CZ_SHORT"/>
  <@lib.separator double=!rel_has_next/>
 </#list>

 <p>
  <a href="/History?type=articles&from=${ARTICLES?size}&count=10" title="Dal¹í"><img src="/images/site2/older.gif" alt="Star¹í èlánky"></a>
 </p>

<#flush>


<#if FORUM?exists>
 <table width="99%" cellspacing="0" cellpadding="0" border="0" class="hpforum" align="center">
  <tr>
   <th colspan="3">
    <strong><a href="/diskuse.jsp" title="Celé diskusní fórum" class="menu">Diskusní fórum</a></strong>
    <a href="/diskuse.jsp" title="Celé diskusní fórum">
    </a>
   </th>
  </tr>
  <tr>
   <td><b>Dotaz</b></td>
   <td align="center"><b>Reakcí</b></td>
   <td align="right"><b>Poslední</b></td>
  </tr>
  <#list FORUM.data as diz>
   <tr bgcolor="#FFFFFF" onmouseover="javascript:style.backgroundColor='#EFEFEF'" onmouseout="javascript:style.backgroundColor='#FFFFFF'">
    <td>
     <a href="/forum/show/${diz.relationId}">${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a>
    </td>
    <td align="center">${diz.responseCount}</td>
    <td align="right">${DATE.show(diz.updated,"CZ_SHORT")}</td>
   </tr>
   <tr><td colspan="3" class="nopad"><@lib.separator double=!diz_has_next /></td></tr>
  </#list>
   <tr>
    <td colspan="3" align="right" class="nopad">
	 <a href="/diskuse.jsp" class="sheet">Zobrazit diskusní fórum (polo¾it dotaz)</a>
	 <a href="/History?type=discussions&from=${FORUM.nextPage.row}&count=20" class="sheet">Zobrazit star¹í dotazy</a>
	</td>
   </tr>
 </table>

</#if>
  </div>

<#include "../footer.ftl">
