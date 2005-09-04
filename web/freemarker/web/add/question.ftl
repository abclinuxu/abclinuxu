<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Ne¾ polo¾íte dotaz</h1>

<ul>
    <li>Pøeètìte si èlánek na téma, <a href="/clanky/show/4006">jak øe¹it problémy</a>.</li>
    <li>Zkusili jste hledání a pro¹li jste si <a href="/faq">Èasto kladené otázky</a> (FAQ)?</li>
    <li>Pokud máte problém s MS&nbsp;Windows a není zde pøíèinná souvislost
        s Linuxem, obra»te se na Microsoft, tady jste na ¹patné adrese.</li>
    <li>Dotazy na warez budou smazány.</li>
</ul>

<h1 class="st_nadpis">Jak psát titulek</h1>

<p>Dobøe zvolený titulek pøiláká pozornost ètenáøù, ¹patný zaruèenì odradí zku¹ené u¾ivatele, kteøí
by vám mohli pomoci. Admini mohou ¹patnì formulovaný titulek upravit.</p>

<ul>
<li>Popi¹te vìtou, v èem pøesnì spoèívá vá¹ problém.
<li>Nepou¾ívejte slova jako help, pomoc, prosím, zaèáteèník èi lama.
<li>NEPI©TE VELKÝMI PÍSMENY, nepou¾ívejte vykøièníky, je to nezdvoøilé.
</ul>

<h1 class="st_nadpis">Jak popsat problém</h1>

<p>Sna¾te se uvést co nejvíce relevantních informací,
jako je druh hardwaru èi verze aplikace. Ostatní ètenáøi nemají køi¹»álovou kouli,
aby to sami uhádli. Popi¹te postup, který nevede k cíli.
Èasto je dobré vlo¾it ukázku konfiguraèního souboru, výpis
dmesg èi lspci (HTML znaèka PRE). Nicménì vkládejte jen skuteènì
zajímavé èásti související s problémem, maximálnì deset a¾
patnáct øádek. Pokud pøijdete na øe¹ení sami, vlo¾te jej do diskuse.
Pomù¾ete tak ostatním ètenáøùm.
</p>

<#if ! USER?exists>
 <h1 class="st_nadpis">Proè se pøihlásit</h1>

 <p>Registrovaní ètenáøi si mohou nechat sledovat diskusi, který za¹le emailem
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

<h1>Nápovìda k formátování</h1>

<p>Povolené HTML <a href="http://www.w3.org/TR/html4/index/elements.html">znaèky</a>:
P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
VAR, HR a IT.</p>

<p>Nejrychlej¹í zpùsob formátování je rozdìlovat
text do odstavcù. Systém detekuje prázdné øádky
(dvakrát enter) a nahradí je HTML znaèkou odstavce.
Pokud ale v textu pou¾ijete znaèku P èi BR,
pak pøedpokládáme, ¾e o formátování se budete starat
sami a tato konverze nebude aktivována.</p>

<p>Pokud neovládáte HTML, doporuèuji si pøeèíst jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text musí být HTML validní, proto znak men¹ítka èi vìt¹ítka zapisujte takto:
&lt; jako &amp;lt; a &gt; jako &amp;gt;. Dal¹ím èastým problémem
je, jak vlo¾it výpis logu èi konfiguraèní soubor. V tomto
pøípadì vá¹ text vlo¾te mezi znaèky PRE, pøíli¹ dlouhé øádky rozdìlte klávesou enter.</p>


<#include "../footer.ftl">
