<div class="host">
    <div class="img-header">
        <p>Připojení:<br /><img src="/images/hosting/gts.png" alt="gts" width="64" height="64"></p>
        <p>Switche:<br /><img src="/images/hosting/hp.gif" alt="hewlett-packard" width="80" height="64"></p>
        <p>Firewall:<br /><img src="/images/hosting/juniper.png" alt="juniper" width="143" height="40"></p>
    </div>

    <h1 class="st_nadpis">
        <img src="/images/site2/sflista/ah.gif" width="18" height="18" alt="abchost.cz logo" />
        <a href="http://www.abchost.cz" title="dedikovaný hosting">
        AbcHost.cz - dedikované stroje od profesionálů z abclinuxu.cz</a>
    </h1>

    <h2 class="st_nadpis">
        <a href="http://www.abchost.cz" title="dedikovaný hosting">AbcHost.cz</a> = <span style="color:red">SERVER</span> +
        <span style="color:blue">KONEKTIVITA</span> [+
        <span style="color:green">VOLITELNÉ SLUŽBY</span>]
    </h2>

    <ul>
        <li>kvalitní 1U server v ceně, připraven k okamžitému zapojení</li>
        <li>jednoduchý a rychlý postup sepsání smlouvy</li>
        <li>nainstalován Debian GNU/Linux (nebo Solaris), zákazník má k dispozici root účet</li>
        <li>vlastní IP adresa, neomezený přenos dat do NIX i zahraničí</li>
        <li>zálohované napájení vysokokapacitním UPS a dieselagregátorem</li>
        <li>volitelně vedení sekundárního DNS a poštovního serveru</li>
        <li>volitelně <a href="/images/hosting/netscreen_juniper.pdf">firewall Netscreen</a> (PDF),
        IP balancing, vzdálený restart, zálohování či console server ...</li>
        <li>plně zálohované připojení do peeringového centra NIX 2 x 2 Gbps</li>
        <li>plně zálohované připojení do zahraničí 2 x 2,5 Gbps, dva nezávislí Tier-1 poskytovatelé</li>
        <li>moderní telehouse Nagano, Praha 3</li>
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
           <p class="price">${server.price.price} Kč ${server.price.paymentPeriod}</p>
           <div class="img-server">
               <a href="${server.url}"><img src="${server.imageUrl}" alt="${server.name}"></a>
           </div>
           <p>
               <b>CPU:</b> ${server.cpu}<br />
               <b>RAM:</b> ${server.ram}<br />
               <b>HD:</b> ${server.disc}<br />
               <b>LAN:</b> ${server.network}<br />
               <b>Konektivita:</b> ${server.bandwidth}<br />
               <b>Přenos:</b> ${server.transfers}
           </p>
           <p class="avail">${server.availability}</p>
       </td>
    <#if server_index%3==2></tr></#if>
  </#list>
</table>

<table class="prehled">
  <tr><th align="left">Doplňkové služby</th><th>cena za měsíc</th></tr>
    <#list SERVICES as service>
        <tr>
            <td>
                <a href="${service.url}" title="${service.name}">${service.name}</a>
<#--
                <div class="nimg" style="float: right">
                    <a href="${service.url}"><img src="${service.imageUrl}" alt="photo of ${service.name}"></a>
                </div>
                <p>${service.description}</p>
-->
            </td>
            <td class="price">${service.price.price} Kč</td>
        </tr>
    </#list>
</table>

<h2>Kontakt na Obchodní oddělení</h2>

<p class="kontakt">
<strong>ICQ:</strong> 351447985 <a href="http://www.icq.com/whitepages/wwp.php?uin=351447985" onclick="javascript:window.open(this.href, 'width=640, height=480, resizable=yes');return false;"><img src="http://status.icq.com/online.gif?icq=351447985&amp;img=5" style="margin-bottom:-5px" alt="ICQ Status" /></a>
<br />
<strong>Skype:</strong> sfrudolf <script type="text/javascript" src="http://download.skype.com/share/skypebuttons/js/skypeCheck.js"></script><a href="skype:sfrudolf?call"><img src="http://mystatus.skype.com/smallicon/sfrudolf" style="border: medium none ;" alt="Skype status" height="16" width="16" /></a>
<br />
<strong>Jabber:</strong> sfrudolf@jabber.cz
<br />
<br />
<strong>ICQ:</strong> 34043399 <a href="http://www.icq.com/whitepages/wwp.php?uin=34043399" onclick="javascript:window.open(this.href, 'width=640, height=480, resizable=yes');return false;"><img src="http://status.icq.com/online.gif?icq=34043399&amp;img=5" style="margin-bottom:-5px" alt="ICQ Status" /></a>
<br />
<strong>Skype:</strong> Saki <script type="text/javascript" src="http://download.skype.com/share/skypebuttons/js/skypeCheck.js"></script><a href="skype:saki?call"><img src="http://mystatus.skype.com/smallicon/saki" style="border: medium none ;" alt="Skype status" height="16" width="16" /></a>
<br />
<br />
<strong>Tel.:</strong> +420<span>267</span>108<span>384</span>
</p>

<p style="font-size:85%;font-style: italic;">
    Ceny jsou uvedeny bez DPH. Pronájem serveru je v ceně zvolené varianty.
</p>
</div>
