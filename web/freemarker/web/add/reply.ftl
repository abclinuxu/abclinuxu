<#assign plovouci_sloupec>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Nápovìda</h1>
    </div></div>

    <p>
        Existují dva zpùsoby, jak formátovat va¹e pøíspìvky.
        První z nich je psaní textu podobnì, jako kdy¾ pí¹ete
        dopis. Rozdìlujte va¹e texty do odstavcù, pokud vlo¾íte
        prázdný øádek, systém automaticky bude zachovávat toto
        formátování. Konce øádkù nemají ¾ádný speciální význam.
        Pøi tomto zpùsobu je pou¾ít i HTML znaèky s vyjímkou znaèky
        nového øádku a odstavce. jejich pou¾ití automaticky
        pøepne do HTML modu, kde jste plnì odpovìdni za formátování.
    </p>

    <p>
        Druhý zpùsob vám dává vìt¹í volnost pøi formátování.
        Máte k dispozici relativnì velkou sadu HTML znaèek.
        Pro zaèáteèníky mohu doporuèit star¹í
        <a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>
        tohoto formátovacího jazyka. Z rùzných dùvodù jsou
        povoleny jen tyto <a href="http://www.w3.org/TR/html4/index/elements.html">znaèky</a>:
        P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
        VAR, HR a IT.
    </p>

    <p>
        Pro oba styly platí, ¾e text musí být HTML validní. Èastým
        problémem je, ¾e nìkdo se sna¾í vlo¾it text obsahující
        znak men¹ítka èi vìt¹ítka. Tyto znaky se zapisují následovnì:
        &lt; jako &amp;lt;,  &gt; jako &amp;gt;. Dal¹ím èastým problémem
        je, jak vlo¾it výpis logu èi konfiguraèní soubor. V tomto
        pøípadì pou¾ijte znaèku PRE a vá¹ text vlo¾te mezi znaèky,
        pøíli¹ dlouhé øádky rozdìlte znakem enter.
    </p>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>Prohlédnìte si vzhled va¹eho komentáøe. Zkontrolujte
 pravopis, obsah i tón va¹eho textu. Nìkdy to vá¾nì
 chce chladnou hlavu. Opravte chyby a zvolte náhled.
 Pokud jste s pøíspìvkem spokojeni, zvolte OK.</p>
</#if>

<#if ! USER?exists>
 <p>Nejste pøihlá¹en. Nový úèet mù¾ete zalo¾it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <h1>Pøíspìvek na který reagujete</h1>
 <@lib.showComment THREAD, 0, 0, false />

 <script language="javascript1.2" type="text/javascript">
    original = "${TOOL.xpath(THREAD.data,"text")?js_string}";
    original = "<blockquote>"+original+"</blockquote>";
    function cituj(input) {
      input.value += original;
    }
 </script>
</#if>

<#if PREVIEW?exists>
 <h1>Náhled va¹eho pøíspìvku</h1>
 <div style="padding-left: 30pt">
  <@lib.showComment PREVIEW, 0, 0, false />
 </div>
</#if>

<h1>Zde mù¾ete provést své úpravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <div>
    <span class="required">Login a heslo</span>
     <input type="text" name="LOGIN" size="8">
     <input type="password" name="PASSWORD" size="8">
   </div>
   <div>
    <span class="required">nebo va¹e jméno</span>
     <input type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}">
     <div class="error">${ERRORS.author?if_exists}</div>
   </div>
  </#if>
  <div>
   <span class="required">Titulek</span><br>
   <#if PARAMS.title?exists>
    <#assign title=PARAMS.title>
   <#elseif THREAD?exists>
    <#assign title=TOOL.xpath(THREAD.data,"title")>
    <#if !title.startsWith("Re: ")><#assign title="Re: "+title></#if>
   </#if>
   <input type="text" name="title" size="60" maxlength="70" value="${title?if_exists?html}">
   <div class="error">${ERRORS.title?if_exists}</div>
  </div>
  <div>
    <span class="required">Vá¹ komentáø</span>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.replyForm.text, '<b></b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<i></i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<a href=&quot;&quot;></a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<p></p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<pre></pre>');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
        <#if THREAD?exists>
            <a href="javascript:cituj(document.replyForm.text);" id="mono" title="Vlo¾í komentovaný pøíspìvek jako citaci">Citace</a>
        </#if>
    </div>
   <div class="error">${ERRORS.text?if_exists}</div>
   <textarea name="text" cols="70" rows="20">${PARAMS.text?if_exists?html}</textarea>
  </div>

  <#if PREVIEW?exists>
   <input type="submit" name="preview" value="Zopakuj náhled">
   <input type="submit" name="finish" value="Dokonèi">
  <#else>
   <input type="submit" name="preview" value="Náhled">
  </#if>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${DISCUSSION.id}">
 <#if THREAD?exists>
  <input type="hidden" name="threadId" value="${THREAD.id}">
 </#if>
 <#if PARAMS.url?exists>
  <input type="hidden" name="url" value="${PARAMS.url}">
 </#if>
</form>


<#include "../footer.ftl">
