<div class="host">
    <div class="img-header">
        <p>Pøipojení:<br /><img src="/images/hosting/gts.png" alt="gts" width="64" height="64"></p>
        <p>Switche:<br /><img src="/images/hosting/hp.gif" alt="hewlett-packard" width="80" height="64"></p>
        <p>Firewall:<br /><img src="/images/hosting/juniper.png" alt="juniper" width="143" height="40"></p>
    </div>

    <h1 class="st_nadpis">
        <img src="/images/site2/sflista/ah.gif" width="18" height="18" alt="abchost.cz logo" />
        <a href="http://www.abchost.cz" title="dedikovaný hosting">
        AbcHost.cz - dedikované stroje od profesionálù z abclinuxu.cz</a>
    </h1>

    <h2 class="st_nadpis">
        <a href="http://www.abchost.cz" title="dedikovaný hosting">AbcHost.cz</a> = <span style="color:red">SERVER</span> +
        <span style="color:blue">KONEKTIVITA</span> [+
        <span style="color:green">VOLITELNÉ SLU®BY</span>]
    </h2>

    <ul>
        <li>kvalitní 1U server v cenì, pøipraven k okam¾itému zapojení</li>
        <li>jednoduchý a rychlý postup sepsání smlouvy</li>
        <li>nainstalován Debian GNU/Linux (nebo Solaris), zákazník má k dispozici root úèet</li>
        <li>vlastní IP adresa, neomezený pøenos dat do NIX i zahranièí</li>
        <li>zálohované napájení vysokokapacitním UPS a dieselagregátorem</li>
        <li>volitelnì vedení sekundárního DNS a po¹tovního serveru</li>
        <li>volitelnì <a href="/images/hosting/netscreen_juniper.pdf">firewall Netscreen</a> (PDF),
        IP balancing, vzdálený restart, zálohování èi console server ...</li>
        <li>plnì zálohované pøipojení do peeringového centra NIX 2 x 2 Gbps</li>
        <li>plnì zálohované pøipojení do zahranièí 2 x 2,5 Gbps, dva nezávislí Tier-1 poskytovatelé</li>
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
           <p class="price">${server.price.price} Kè ${server.price.paymentPeriod}</p>
           <div class="img-server">
               <a href="${server.url}"><img src="${server.imageUrl}" alt="${server.name}"></a>
           </div>
           <p>
               <b>CPU:</b> ${server.cpu}<br />
               <b>RAM:</b> ${server.ram}<br />
               <b>HD:</b> ${server.disc}<br />
               <b>LAN:</b> ${server.network}<br />
               <b>Konektivita:</b> ${server.bandwidth}<br />
               <b>Pøenos:</b> ${server.transfers}
           </p>
           <p class="avail">${server.availability}</p>
       </td>
    <#if server_index%3==2></tr></#if>
  </#list>
</table>

<h2>Doplòkové slu¾by</h2>

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
            <td class="price">${service.price.price} Kè mìsíènì</td>
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
    Ceny jsou uvedeny bez DPH. Pronájem serveru je v cenì zvolené varianty.
</p>
</div>
