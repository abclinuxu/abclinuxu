<div class="host">
    <div class="img-header">
        <p>P�ipojen�:<br /><img src="/images/hosting/gts.png" alt="gts" width="64" height="64"></p>
        <p>Switche:<br /><img src="/images/hosting/hp.gif" alt="hewlett-packard" width="80" height="64"></p>
        <p>Firewall:<br /><img src="/images/hosting/juniper.png" alt="juniper" width="143" height="40"></p>
    </div>

    <h1 class="st_nadpis">
        <img src="/images/site2/sflista/ah.gif" width="18" height="18" alt="abchost.cz logo" />
        <a href="http://www.abchost.cz" title="dedikovan� hosting">
        AbcHost.cz - dedikovan� stroje od profesion�l� z abclinuxu.cz</a>
    </h1>

    <h2 class="st_nadpis">
        <a href="http://www.abchost.cz" title="dedikovan� hosting">AbcHost.cz</a> = <span style="color:red">SERVER</span> +
        <span style="color:blue">KONEKTIVITA</span> [+
        <span style="color:green">VOLITELN� SLU�BY</span>]
    </h2>

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

<h2>Servery</h2>



<table class="prehled">
  <#list SERVERS as server>
    <#if server_index%3==0><tr></#if>
       <td>
           <h4>
               <a href="${server.url}" title="${server.name}">${server.name}</a>
                 <#if server.newArrival><span class="novinka">novinka!</span></#if>
                 <#if server.action><span class="akce">akce!</span></#if>
           </h4>
           <p class="price">${server.price.price} K� ${server.price.paymentPeriod}</p>
           <div class="img-server">
               <a href="${server.url}"><img src="${server.imageUrl}" alt="${server.name}"></a>
           </div>
           <p>
               <b>CPU:</b> ${server.cpu}<br />
               <b>RAM:</b> ${server.ram}<br />
               <b>HD:</b> ${server.disc}<br />
               <b>LAN:</b> ${server.network}<br />
               <b>Konektivita:</b> ${server.bandwidth}<br />
               <b>P�enos:</b> ${server.transfers}
           </p>
           <p class="avail">${server.availability}</p>
       </td>
    <#if server_index%3==2></tr></#if>
  </#list>
</table>

<h2>Dopl�kov� slu�by</h2>

<table>
    <#list SERVICES as service>
        <tr>
            <td>
                <b><a href="${service.url}" title="${service.name}">${service.name}</a></b>
<#--
                <div class="nimg" style="float: right">
                    <a href="${service.url}"><img src="${service.imageUrl}" alt="photo of ${service.name}"></a>
                </div>
                <p>${service.description}</p>
-->
            </td>
            <td class="price">${service.price.price} K� m�s��n�</td>
        </tr>
    </#list>
</table>

<h2>Kontakt</h2>

<p class="kontakt">
    Tel: +420<span>267</span>108<span>384</span><br />
    ICQ: 34043399<a href="http://www.icq.com/whitepages/wwp.php?uin=34043399"><img src="http://status.icq.com/online.gif?icq=34043399&amp;img=5" alt="ICQ Status" /></a><br />
    Skype: Saki<a href="skype:Saki?call"><img src="http://download.skype.com/share/skypebuttons/buttons/call_blue_transparent_70x23.png" style="border: none;" width="70" height="23" alt="Call me!" /></a>
</p>

<p style="font-size:85%;font-style: italic;">
    Ceny jsou uvedeny bez DPH. Pron�jem serveru je v cen� zvolen� varianty.
</p>
</div>
