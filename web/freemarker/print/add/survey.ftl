<#include "../header.ftl">

<@lib.showMessages/>

<h1>�vod</h1>

<p>Ka�d� anketa se skl�d� z HTML soubor� obsahuj�c�ch formul��e
a z XML, kter� je ulo�eno v datab�zi jako polo�ka typu 6. Tato polo�ka
ur�uje po�ad� vol�n� jednotliv�ch HTML soubor� (naz�van�ch SCREEN)
a ukl�d�n� v�sledk�. Z�rove� obsahuje pomocn� �daje pro automatickou
anal�zu v�sledk�, zvl�t� pak jednotliv� VOLBY (radio buttony a check boxy).
</p>

<p>
Prvn� screen mus� m�t ID nastaven na START. Ka�d� screen mus� obsahovat
zna�ku template, kter� obsahuje cestu k ur�it�mu HTML souboru. Cesta
mus� za��nat lom�tkem a b�t relativn� v��i �ablon� (WEB-INF/freemarker/web).
Sv� HTML soubory zkop�rujte jak do t�to hlavn� �ablony, tak i do ostatn�ch
(v sou�asnosti WEB-INF/freemarker/lynx).
</p>

<p>
Pokud chcete v n�kter�m kroku ulo�it v�echny �daje z�skan� od u�ivatele
v p�edchoz�ch screenech, vlo�te do dan�ho screenu zna�ku dump. Ta zp�sob�,
�e se v�echny �daje zap�� do XML souboru do dan�ho adres��e pod n�hodn�m
jm�nem za��naj�c�m na prefix a kon��c�m na suffix. �daje budou z�rove� vymaz�ny
z pam�ti, tak�e dal�� dump je ji� nebude zapisovat. Takto m��ete nap��klad
zajistit anonymn� anketu - odd�len� osobn�ch �daj� od ostatn�ch informac�.
Obvykle se tato zna�ka pou��v� v posledn�m screenu, kter� ji� pochopiteln�
neobsahuje formul��, jen pod�kov�n�.
</p>

<p>
HTML soubory mus� obsahovat formul�� s p�esn� dan�mi informacemi, pokud
n�kter� opomenete, anketa nebude fungovat spr�vn�. Z�kladn� informac�
je nastavit spr�vn� URL, kam bude formul�� zas�lat �daje. Toto URL
je /Survey. Kv�li lidem maj�c�m zak�zan� cookies u anket s v�ce screeny
je vhodn� pou��t URL rewriting. To z�sk�te takto: ${URL.noPrefix("/Survey")},
nicm�n� nebude fungovat, pokdu prvn� screen za�len�te do �l�nku. Tento
f�gl funguje, pouze pokud jej pou�ijete v samostatn�m HTML souboru definovan�m
v zna�ce template.
</p>

<p>
Formul�� mus� d�le obsahovat �ty�i skryt� vstupn� pol��ka. Prvn� je
surveyId, kter� obsahuje ��slo polo�ky t�to ankety. Druh� pol��ko je
SCREEN_CURRENT, co� je id aktu�ln�ho screenu. SCREEN_NEXT zase obsahuje
��slo n�sleduj�c�ho screenu, kter� bude zobrazen na dal�� str�nce.
SAVE_PARAMS mus� obsahovat ��rkou odd�len� jm�na v�ech formul��ov�ch
pol��ek, kter� chcete ulo�it. Pokud n�kter� pol��ko zde vynech�te,
nebude ulo�eno a jeho hodnota bude ztracena.
</p>

<h1>P��klad</h1>

<h2>XML definice</h2>

<pre>${"<anketa>
  <screen id=\"START\">
    <template>/ankety/abc1/q1.ftl</template>
  </screen>
  <screen id=\"demografie\">
    <template>/ankety/abc1/q2.ftl</template>
  </screen>
  <screen id=\"last\">
    <template>/ankety/abc1/thanks.ftl</template>
    <dump>
      <dir>/home/www-data/deploy/abclinuxu/WEB-INF/freemarker/web/ankety/abc1/data</dir>
      <prefix>answear_</prefix>
      <suffix>.xml</suffix>
    </dump>
  </screen>
</anketa>"?html}</pre>

<h2>HTML soubor /home/www-data/deploy/abclinuxu/WEB-INF/freemarker/web/ankety/abc1/q2.ftl</h2>

<pre>${"<form action=\"${URL.noPrefix(\"/Survey\")}\" method=\"POST\">
   <input type=\"hidden\" name=\"SCREEN_CURRENT\" value=\"demografie\">
   <input type=\"hidden\" name=\"SCREEN_NEXT\" value=\"last\">
   <input type=\"hidden\" name=\"SAVE_PARAMS\" value=\"speed,sluzby_chybi,kvalita_clanku,frekvence_clanku,vzkaz\">
   <input type=\"hidden\" name=\"surveyId\" value=\"3285\">
</form>"?html}</pre>

<h1>Anketa</h1>

<form action="${URL.make("/EditSurvey")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="120">Jm�no ankety</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" align="middle">Volby</td>
   <td>
    <p>Zde napi�te jm�na v�ech radio button� a check box� z formul���.<br>
    Ka�d� jm�no dejte na samostatn� ��dek.</p>
    <textarea name="choices" cols="40" rows="4" tabindex="2">${PARAMS.choices?if_exists}</textarea>
    <div class="error">${ERRORS.choices?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required" align="middle">XML definice</td>
   <td>
    <pre>${"<!ELEMENT anketa (screen+)>
<!ELEMENT screen (#PCDATA)>
<!ATTLIST screen id (ID) #REQUIRED>
<!ELEMENT template (#PCDATA)>
<!ELEMENT dump (dir,prefix,suffix)>
<!ELEMENT dir (#PCDATA)>
<!ELEMENT prefix (#PCDATA)>
<!ELEMENT suffix (#PCDATA)>"?html}</pre>
    <textarea name="definition" cols="80" rows="20" tabindex="3">${PARAMS.definition?if_exists?html}</textarea>
    <div class="error">${ERRORS.definition?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokra�uj" tabindex="4"></td>
  </tr>
 </table>

 <#if PARAMS.surveyId?exists>
  <input type="hidden" name="action" value="edit2">
  <input type="hidden" name="surveyId" value="${PARAMS.surveyId}">
 <#else>
  <input type="hidden" name="action" value="add2">
 </#if>
</form>


<#include "../footer.ftl">
