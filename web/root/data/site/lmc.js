var JobsScroll={offset:1,maxCount:300,count:0,curMode:"down",timer:null,start:function(_a0){
JobsScroll.maxCount=350;
JobsScroll.curMode=_a0;
JobsScroll.timer=setTimeout("JobsScroll.scroll(\""+_a0+"\")",10000);
},scroll:function(_a1){
if(_a1=="down"){
$("jobs-viewport").scrollTop=$("jobs-viewport").scrollTop+JobsScroll.offset;
}else{
$("jobs-viewport").scrollTop=$("jobs-viewport").scrollTop-JobsScroll.offset;
}
if(JobsScroll.count++<JobsScroll.maxCount){
JobsScroll.timer=setTimeout("JobsScroll.scroll(\""+_a1+"\")",1);
}else{
JobsScroll.count=0;
var _a2=_a1=="up"?"down":"up";
JobsScroll.curMode=_a2;
JobsScroll.timer=setTimeout("JobsScroll.scroll(\""+_a2+"\")",10000);
}
},stop:function(){
clearTimeout(JobsScroll.timer);
},restart:function(){
JobsScroll.timer=setTimeout("JobsScroll.scroll(\""+JobsScroll.curMode+"\")",1);
}};
function newsletter_check(_a3,_a4){
var _a5=document.getElementsByName(_a4);
for(var i=0;i<_a5.length;i++){
if(Element.visible(_a5[i].parentNode.parentNode)){
Element.hide(_a5[i].parentNode.parentNode);
}else{
Element.show(_a5[i].parentNode.parentNode);
}
_a5[i].disabled=!_a3;
}
}
