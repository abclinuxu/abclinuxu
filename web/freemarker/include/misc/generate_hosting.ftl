<div class="char">
    <div class="imgs">
        <p>Pøipojení:<br /><img src="/images/hosting/gts.png" alt="gts" width="64" height="64"></p>
        <p>Switche:<br /><img src="/images/hosting/hp.gif" alt="hewlett-packard" width="80" height="64"></p>
        <p>Firewall:<br /><img src="/images/hosting/juniper.png" alt="juniper" width="143" height="40"></p>
    </div>
    <br />

    <h3>
        AbcHosting = <span style="color:red;">SERVER</span> +
        <span style="color:blue;">KONEKTIVITA</span> [+
        <span style="color:green">VOLITELNÉ SLU®BY</span>]
    </h3>

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
                <h4>${server.price.price} Kè ${server.price.paymentPeriod}</h4>
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
                            <b>Pøenos za mìsíc:</b> ${server.transfers}<br/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </#list>
</table>

<h2>Doplòkové slu¾by</h2>

<table>
    <#list SERVICES as service>
        <tr>
            <td>
                <h3>
                    <a href="${service.url}">${service.name}</a>
                </h3>
                <h4>${service.price.price} Kè mìsíènì</h4>
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
    Ceny jsou uvedeny bez DPH. Pronájem serveru je v cenì zvolené varianty.
</p>
