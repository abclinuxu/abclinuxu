<ul>
  <#assign KERNELS = [v26, v26mm, v24, v22]>
  <#list KERNELS as kernel>
  <li><a href="${kernel.url}" rel="nofollow">(${kernel.release}<#if kernel.preRelease?has_content> - ${kernel.preRelease}</#if>)</a></li>
  </#list>
</ul>