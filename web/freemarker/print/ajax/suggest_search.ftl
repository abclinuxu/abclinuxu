<ul><#list QUERIES as query>
<li>${TOOL.limit(query[0],40,"")}<span class="informal"> ${query[1]}&times</span></li>
</#list></ul>