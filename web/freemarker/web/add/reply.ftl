<#assign plovouci_sloupec>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>N�pov�da</h1>
    </div></div>

    <p>
        Existuj� dva zp�soby, jak form�tovat va�e p��sp�vky.
        Prvn� z nich je psan� textu podobn�, jako kdy� p�ete
        dopis. Rozd�lujte va�e texty do odstavc�, pokud vlo��te
        pr�zdn� ��dek, syst�m automaticky bude zachov�vat toto
        form�tov�n�. Konce ��dk� nemaj� ��dn� speci�ln� v�znam.
        P�i tomto zp�sobu je pou��t i HTML zna�ky s vyj�mkou zna�ky
        nov�ho ��dku a odstavce. jejich pou�it� automaticky
        p�epne do HTML modu, kde jste pln� odpov�dni za form�tov�n�.
    </p>

    <p>
        Druh� zp�sob v�m d�v� v�t�� volnost p�i form�tov�n�.
        M�te k dispozici relativn� velkou sadu HTML zna�ek.
        Pro za��te�n�ky mohu doporu�it star��
        <a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>
        tohoto form�tovac�ho jazyka. Z r�zn�ch d�vod� jsou
        povoleny jen tyto <a href="http://www.w3.org/TR/html4/index/elements.html">zna�ky</a>:
        P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
        VAR, HR a IT.
    </p>

    <p>
        Pro oba styly plat�, �e text mus� b�t HTML validn�. �ast�m
        probl�mem je, �e n�kdo se sna�� vlo�it text obsahuj�c�
        znak men��tka �i v�t��tka. Tyto znaky se zapisuj� n�sledovn�:
        &lt; jako &amp;lt;,  &gt; jako &amp;gt;. Dal��m �ast�m probl�mem
        je, jak vlo�it v�pis logu �i konfigura�n� soubor. V tomto
        p��pad� pou�ijte zna�ku PRE a v� text vlo�te mezi zna�ky,
        p��li� dlouh� ��dky rozd�lte znakem enter.
    </p>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#if PREVIEW?exists>
 <p>Prohl�dn�te si vzhled va�eho koment��e. Zkontrolujte
 pravopis, obsah i t�n va�eho textu. N�kdy to v�n�
 chce chladnou hlavu. Opravte chyby a zvolte n�hled.
 Pokud jste s p��sp�vkem spokojeni, zvolte OK.</p>
</#if>

<#if ! USER?exists>
 <p>Nejste p�ihl�en. Nov� ��et m��ete zalo�it
  <a HREF="${URL.noPrefix("/EditUser?action=register")}">zde</a>.
 </p>
</#if>

<#if THREAD?exists>
 <h1>P��sp�vek na kter� reagujete</h1>
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
 <h1>N�hled va�eho p��sp�vku</h1>
 <div style="padding-left: 30pt">
  <@lib.showComment PREVIEW, 0, 0, false />
 </div>
</#if>

<h1>Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="replyForm">
  <#if ! USER?exists>
   <div>
    <span class="required">Login a heslo</span>
     <input type="text" name="LOGIN" size="8">
     <input type="password" name="PASSWORD" size="8">
   </div>
   <div>
    <span class="required">nebo va�e jm�no</span>
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
    <span class="required">V� koment��</span>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.replyForm.text, '<b></b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<i></i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<a href=&quot;&quot;></a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<p></p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.replyForm.text, '<pre></pre>');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
        <#if THREAD?exists>
            <a href="javascript:cituj(document.replyForm.text);" id="mono" title="Vlo�� komentovan� p��sp�vek jako citaci">Citace</a>
        </#if>
    </div>
   <div class="error">${ERRORS.text?if_exists}</div>
   <textarea name="text" cols="70" rows="20">${PARAMS.text?if_exists?html}</textarea>
  </div>

  <#if PREVIEW?exists>
   <input type="submit" name="preview" value="Zopakuj n�hled">
   <input type="submit" name="finish" value="Dokon�i">
  <#else>
   <input type="submit" name="preview" value="N�hled">
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
