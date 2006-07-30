<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pokládání dotazu</h1>

<ul>
    <li>Pøeètìte si èlánek
        <a href="/clanky/navody/jak-resit-problemy">Jak øe¹it problémy</a>.</li>
    <li>Zkusili jste <a href="/Search?advancedMode=true" title="Vyhledávání">hledání</a>
        a pro¹li jste si <a href="/faq" title="FAQ Linux">Èasto kladené otázky</a> (FAQ)?</li>
    <li>Pokud máte problém s MS&nbsp;Windows a není zde pøíèinná souvislost
        s Linuxem, obra»te se na Microsoft, tady jste na ¹patné adrese.</li>
    <li>Dotazy na warez budou smazány.</li>
</ul>

<h2>Jak psát titulek</h2>

<p>Dobøe zvolený titulek pøiláká pozornost ètenáøù, ¹patný zaruèenì odradí zku¹ené u¾ivatele, kteøí
by vám mohli pomoci. Admini mohou ¹patnì formulovaný titulek upravit.</p>

<ul>
<li>Popi¹te vìtou, v èem pøesnì spoèívá vá¹ problém.</li>
<li><b>Nepou¾ívejte</b> slova jako <i>help</i>, <i>pomoc</i>, <i>poraïte</i>, <i>prosím</i>,
    <i>zaèáteèník</i> èi <i>lama</i>.</li>
<li>NEPI©TE VELKÝMI PÍSMENY, nepou¾ívejte vykøièníky, je to nezdvoøilé.</li>
<li>Jeden otazník úplnì staèí. Opravdu.</li>
</ul>

<h2>Jak popsat problém</h2>

<ul>
<li>Sna¾te se uvést co nejvíce relevantních informací. Napøíklad:
    <ul>
    <li>druh hardwaru</li>
    <li>verze aplikace</li>
    </ul>
(Ostatní ètenáøi nemají køi¹»álovou kouli, aby to sami uhádli.)</li>

<li>Popi¹te postup, který nevede k cíli. Uveïte, jestli jste postupovali podle nìjakého návodu.
Pokud ano, vlo¾te na nìj odkaz.</li>

<li>Èasto je dobré vlo¾it ukázku konfiguraèního souboru, výpis
<code>dmesg</code> èi <code>lspci</code> (HTML znaèka <code>&lt;PRE&gt;</code>). Nicménì
vkládejte jen skuteènì zajímavé èásti související s problémem, maximálnì deset a¾
patnáct øádek.</li>

<li>Pokud pøijdete na øe¹ení sami, vlo¾te jej do diskuse.
Pomù¾ete tak ostatním ètenáøùm.</li>
</ul>

<p><b>Do jednoho dotazu nevkládejte více problémù</b>. Diskusi pak není mo¾né vhodnì zaøadit do
pøíslu¹ného diskusního fóra a není mo¾né ji výsti¾nì pojmenovat. Pro u¾ivatele,
který by pozdìji hledal odpovìï na nìkterý z uvedených problémù, by bylo obtí¾né takovou
diskusi vyhledat. Dotazy obsahující více problémù mohou být administrátory uzamèeny, pøièem¾
tazatel bude po¾ádán, aby jednotlivé problémy popsal v samostatných diskusích.</p>

<#if ! USER?exists>
 <h1 class="st_nadpis">Proè se pøihlásit</h1>

 <p>Registrovaní ètenáøi si mohou nechat sledovat diskusi, tak¾e jim budou emailem posílány
 reakce ostatních ètenáøù. Zároveò si budete moci ve svém profilu snadno vyhledat
 tuto diskusi. Proto je výhodné se pøihlásit. Nemáte-li u nás je¹tì úèet,
 <a href="${URL.noPrefix("/EditUser?action=add")}">zaregistrujte se</a>.
 </p>
</#if>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form">
 <table cellpadding="5">
  <#if ! USER?exists>
   <tr>
    <td class="required">Login a heslo</td>
    <td>
     <input tabindex="1" type="text" name="LOGIN" size="8">
     <input tabindex="2" type="password" name="PASSWORD" size="8">
    </td>
   </tr>
   <tr>
    <td class="required">nebo va¹e jméno</td>
    <td><input tabindex="3" type="text" size="30" name="author"></td>
   </tr>
  </#if>
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input tabindex="4" type="text" name="title" size="40" maxlength="70">
   </td>
  </tr>
  <tr>
   <td class="required">Dotaz</td>
   <td>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
	<a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
    </div>
    <textarea tabindex="5" name="text" cols="60" rows="20"></textarea><br>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td><input tabindex="6" type="submit" name="preview" value="Náhled"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="addQuez2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">


<#include "../footer.ftl">
