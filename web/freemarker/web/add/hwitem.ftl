<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<#if PREVIEW?exists>
 <h2>N�hled</h2>
 <p>
    Prohl�dn�te si vzhled va�eho z�znamu. Opravte chyby a zvolte tla��tko <code>N�hled</code>.
    Pokud jste s v�sledkem spokojeni, stiskn�te tla��tko <code>Dokon�i</code>.
 </p>

 <div style="padding-left: 30pt">
    <@hwlib.showHardware PREVIEW />
 </div>
</#if>

<h2 class="st_nadpis">N�pov�da</h2>

<p>
   Zadejte pros�m podrobn� informace o tomto druhu hardwaru, zda je v�bec podporov�n
   a na jak� �rovni, kde je mo�n� naj�t ovlada�, jak jej detekuje Linux, technick�
   parametry, v� n�zor na cenu a postup zprovozn�n�. U n�j je vhodn� ps�t postup,
   kter� je nez�visl� na distribuci, aby byl v� z�znam u�ite�n� i lidem, kte��
   si zvolili jinou distribuci.
</p>

<h2 class="st_nadpis">Form�tov�n�</h2>

<p>
    Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
    pr�zdn� ��dky budou nahrazeny nov�m odstavcem. V�ce informac�
    <a href="#formatovani">najdete</a> pod formul��em.
</p>

<form action="${URL.make("/edit")}" method="POST">
    <table width="100%" border="0" cellpadding="5">
        <tr>
            <td class="required">Jm�no</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td class="required">Podpora pod Linuxem</td>
            <td>
                <select name="support" tabindex="2">
                    <#assign support=PARAMS.support?if_exists>
                    <option value="complete"<#if support=="complete"> SELECTED</#if>>kompletn�</option>
                    <option value="partial"<#if support=="partial"> SELECTED</#if>>��ste�n�</option>
                    <option value="none"<#if support=="none"> SELECTED</#if>>��dn�</option>
                </select>
            </td>
        </tr>

        <tr>
            <td class="required">Ovlada� je dod�v�n</td>
            <td>
                <select name="driver" tabindex="3">
                    <#assign driver=PARAMS.driver?if_exists>
                    <option value="kernel"<#if driver=="kernel"> SELECTED</#if>>v j�d�e</option>
                    <option value="xfree"<#if driver=="xfree"> SELECTED</#if>>v XFree86</option>
                    <option value="maker"<#if driver=="maker"> SELECTED</#if>>v�robcem</option>
                    <option value="other"<#if driver=="other"> SELECTED</#if>>n�k�m jin�m</option>
                    <option value="none"<#if driver=="none"> SELECTED</#if>>neexistuje</option>
                    <option>netu��m</option>
                </select>
            </td>
        </tr>

        <tr>
            <td>Adresa ovlada�e</td>
            <td>
                <input type="text" name="driverUrl" value="${PARAMS.driverUrl?if_exists}" size="60" tabindex="4" class="wide">
                <div class="error">${ERRORS.driverUrl?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Cena</td>
            <td>
                <select name="price" tabindex="5">
                    <#assign price=PARAMS.price?if_exists>
                    <option value="verylow"<#if price=="verylow"> SELECTED</#if>>velmi n�zk�</option>
                    <option value="low"<#if price=="low"> SELECTED</#if>>n�zk�</option>
                    <option value="good"<#if price=="good"> SELECTED</#if>>p�im��en�</option>
                    <option value="high"<#if price=="high"> SELECTED</#if>>vysok�</option>
                    <option value="toohigh"<#if price=="toohigh"> SELECTED</#if>>p�emr�t�n�</option>
                    <option>nehodnot�m</option>
                </select>
            </td>
        </tr>

        <tr>
            <td>Zastaral�</td>
            <td>
                <input type="radio" name="outdated" value="yes"<#if PARAMS.outdated?if_exists=="yes"> checked</#if> tabindex="6"> ano
                <input type="radio" name="outdated" value=""<#if (PARAMS.outdated?if_exists!="yes")> checked</#if> tabindex="7"> ne
            </td>
        </tr>

        <tr>
            <td>Identifikace pod Linuxem</td>
            <td>
                <div>
                    Identifikaci za��zen� pod Linuxem se v�nuje
                    <a href="/faq/hardware/jak-zjistim-co-mam-za-hardware">FAQ</a>.
                    Zadejte jen skute�n� relevantn� �daje, bu�te stru�n�.
                    Doporu�ujeme pou��vat zna�ku <code>PRE</code>.
                </div>
                <textarea name="identification" cols="50" rows="4" tabindex="8" class="wide">${PARAMS.identification?if_exists?html}</textarea>
                <div class="error">${ERRORS.identification?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Technick� parametry</td>
            <td>
                <textarea name="params" cols="50" rows="4" tabindex="9" class="wide">${PARAMS.params?if_exists?html}</textarea>
                <div class="error">${ERRORS.params?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Postup zprovozn�n�</td>
            <td>
                <textarea name="setup" cols="50" rows="10" tabindex="10" class="wide">${PARAMS.setup?if_exists?html}</textarea>
                <div class="error">${ERRORS.setup?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td>Pozn�mka</td>
            <td>
                <textarea name="note" cols="50" rows="10" tabindex="11" class="wide">${PARAMS.note?if_exists?html}</textarea>
                <div class="error">${ERRORS.note?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <#if PREVIEW?exists>
                    <input tabindex="12" type="submit" name="preview" value="Zopakuj n�hled">
                    <input tabindex="13" type="submit" name="finish" value="Dokon�i">
                <#else>
                    <input tabindex="12" type="submit" name="preview" value="N�hled">
                </#if>
            </td>
        </tr>
    </table>

    <input type="hidden" name="rid" value="${RELATION.id}">
    <input type="hidden" name="action" value="add2">
</form>

<a name="formatovani"></a>
<h1>N�pov�da k form�tov�n�</h1>

<p>Povolen� HTML <a href="http://www.w3.org/TR/html4/index/elements.html">zna�ky</a>:
A, ACRONYM, B, BR, BLOCKQUOTE, CITE, CODE, DIV, EM, I, HR, H1, H2, H3, LI,
OL, P, PRE, STRONG, TT, UL, VAR. </p>

<p>Nejrychlej�� zp�sob form�tov�n� je rozd�lovat
text do odstavc�. Syst�m detekuje pr�zdn� ��dky
(dvakr�t enter) a nahrad� je HTML zna�kou odstavce.
Pokud ale v textu pou�ijete zna�ku P �i BR,
pak p�edpokl�d�me, �e o form�tov�n� se budete starat
sami a tato konverze nebude aktivov�na.</p>

<p>Pokud neovl�d�te HTML, doporu�uji si p�e��st jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text mus� b�t HTML validn�, proto znak men��tka �i v�t��tka zapisujte takto:
<code>&lt;</code> jako <code>&amp;lt;</code> a <code>&gt;</code> jako <code>&amp;gt;</code>.
Dal��m �ast�m probl�mem je, jak vlo�it v�pis logu �i konfigura�n� soubor. V tomto
p��pad� v� text vlo�te mezi zna�ky <code>PRE</code>, kter� zachovaj� form�tov�n�. </p>

<#include "../footer.ftl">
