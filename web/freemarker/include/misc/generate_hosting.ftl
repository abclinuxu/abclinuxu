<div class="char">
    <div class="imgs">
        <p>P�ipojen�:<br /><img src="/images/hosting/gts.png" alt="gts" width="64" height="64"></p>
        <p>Switche:<br /><img src="/images/hosting/hp.gif" alt="hewlett-packard" width="80" height="64"></p>
        <p>Firewall:<br /><img src="/images/hosting/juniper.png" alt="juniper" width="143" height="40"></p>
    </div>
    <br />

    <h3>
        AbcHosting = <span style="color:red;">SERVER</span> +
        <span style="color:blue;">KONEKTIVITA</span> [+
        <span style="color:green">VOLITELN� SLU�BY</span>]
    </h3>

    <ul>
        <li>kvalitn� 1U server v cen�, p�ipraven k okam�it�mu zapojen�</li>
        <li>jednoduch� a rychl� postup seps�n� smlouvy</li>
        <li>nainstalov�n Debian GNU/Linux (nebo Solaris), z�kazn�k m� k dispozici root ��et</li>
        <li>vlastn� IP adresa, neomezen� p�enos dat do NIX i zahrani��</li>
        <li>z�lohovan� nap�jen� vysokokapacitn�m UPS a dieselagreg�torem</li>
        <li>voliteln� veden� sekund�rn�ho DNS a po�tovn�ho serveru</li>
        <li>voliteln� <a href="/images/hosting/netscreen_juniper.pdf">firewall Netscreen</a> (PDF),
        IP balancing, vzd�len� restart, z�lohov�n� �i console server ...</li>
        <li>pln� z�lohovan� p�ipojen� do peeringov�ho centra NIX 2 x 2 Gbps</li>
        <li>pln� z�lohovan� p�ipojen� do zahrani�� 2 x 2,5 Gbps, dva nez�visl� Tier-1 poskytovatel�</li>
        <li>modern� telehouse Nagano, Praha 3</li>
    </ul>
</div>

<h2>Servery</h2>

<table>
    <#list SERVERS as server>
        <tr>
            <td>
                <h3>
                    <a href="${server.url}">${server.name}</a>
                    <#if action.newArrival>akce!</#if>
                    <#if server.action>novinka!</#if>
                </h3>
                <h4>${server.price.price} K� ${server.price.paymentPeriod}</h4>
                <div class="nimg">
                    <a href="${server.url}"><img src="${server.imageUrl}" alt="photo of ${server.name}"></a>
                </div>
                <table>
                    <tr>
                        <td>
                            <b>CPU:</b> ${server.cpu}<br/>
                            <b>RAM:</b> ${server.ram}<br/>
                            <b>HD:</b> ${server.disc}<br/>
                            <b>LAN:</b> ${server.network}
                        </td>
                        <td align="top">
                            <b>Dostupnost:</b> ${server.availability}<br/>
                            <b>Konektivita:</b> ${server.bandwidth}<br/>
                            <b>P�enos za m�s�c:</b> ${server.transfers}<br/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </#list>
</table>

<h2>Dopl�kov� slu�by</h2>

<table>
    <#list SERVICES as service>
        <tr>
            <td>
                <h3>
                    <a href="${service.url}">${service.name}</a>
                </h3>
                <h4>${service.price.price} K� m�s��n�</h4>
<#--
                <div class="nimg" style="float: right">
                    <a href="${service.url}"><img src="${service.imageUrl}" alt="photo of ${service.name}"></a>
                </div>
                <p>${service.description}</p>
-->
            </td>
        </tr>
    </#list>
</table>

<h2>Kontakt</h2>

<p class="kontakt">
    Tel: +420&nbsp;777&nbsp;993&nbsp;222<br />
    ICQ: 430106328<a href="http://www.icq.com/whitepages/wwp.php?uin=430106328"><img src="http://status.icq.com/online.gif?icq=430106328&amp;img=5" alt="ICQ Status" /></a><br />
    Skype: jk_stickfish.cz<a href="skype:jk_stickfish.cz?call"><img src="http://download.skype.com/share/skypebuttons/buttons/call_blue_transparent_70x23.png" style="border: none;" width="70" height="23" alt="Call me!" /></a>
</p>

<p style="font-size:70%;font-style: italic;">
    Ceny jsou uvedeny bez DPH. Pron�jem serveru je v cen� zvolen� varianty.
</p>
