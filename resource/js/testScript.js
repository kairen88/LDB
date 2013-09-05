//create a div element and insert into code window
var newdiv = document.createElement('div');
var divIdName = 'div1';
newdiv.setAttribute('id',divIdName);
newdiv.style.width = "100px";
newdiv.style.height = "100px";
newdiv.style.position = "absolute";
newdiv.style.background = "#FF0000";
newdiv.innerHTML = 'this is 1st DIV';
newdiv.setAttribute('onClick', function(){$(this).css({height: '200px', width: '200px'});});

newdiv.onclick = function(){ newdiv.style.width = "1000px";
newdiv.style.height = "1000px"; };

// document.body.appendChild(newdiv);



//testing hover function
function addElement(position){
	var newdiv = document.createElement('div');
	var divIdName = 'div2';
	newdiv.setAttribute('id',divIdName);
	newdiv.style.width = "100px";
	newdiv.style.height = "100px";
	newdiv.style.position = "absolute";
	newdiv.style.background = "#FFA500";
	newdiv.innerHTML = 'this is 2nd DIV';
	document.body.appendChild(newdiv);
	$('#div2').offset({ top: position.top + 10, left: position.left + 10});
}

// $('.cm-variable').hover(function () {

	// var position = $(this).position();
	// addElement(position);
	// $(this).append($('<span> ***</span>'));
	
// }, function () {

	// $(this).find('span:last').remove();
	
// });



//attach a div element(tag) to each element in code window with class cm-variable
function addTag(position, varIdx)
{
	var newdiv = document.createElement('div');
	var divIdName = 'div' + varIdx;
	newdiv.setAttribute('id',divIdName);
	newdiv.setAttribute('class', 'tag');
	newdiv.style.width = "10px";
	newdiv.style.height = "10px";
	newdiv.style.position = "absolute";
	newdiv.style.background = "#FFA500";
	newdiv.innerHTML = '5';
	
	newdiv.onclick = function(){ 
			newdiv.style.width = "100px";
			newdiv.style.height = "100px"; 
		};
	
	document.body.appendChild(newdiv);
	
	return newdiv;
}

var varIdx = 1; 

$('.cm-variable').each(function(){
	var position = $(this).position();
	var tag = addTag(position, varIdx);
	$(this).append(tag);
	$('#div'+ varIdx).offset({ top: position.top + 5, left: position.left + 35});
	varIdx += 1;
});

//set hover on tags
$('.tag').hover(function () {

	// $(this).css({height: '100px', width: '100px'});
	
}, function () {

	$(this).css({height: '10px', width: '10px'});
	
});

//
function expandTag()
{
	tag.style.width = "1000px";
	tag.style.height = "1000px";
}

//create a div to highlight expressions
function createExpBoarder(length)
		{
			var newdiv = document.createElement('textarea');
			var divIdName = 'expBoarder';
			newdiv.setAttribute('id',divIdName);
			newdiv.setAttribute('class', 'expBoarder');
			newdiv.style.width = length + "px";
			newdiv.style.height = "200px";
			newdiv.style.position = "absolute";
			newdiv.style.background = "#FFA500";			
			
			document.body.appendChild(newdiv);
			
			return newdiv;
		}
		
		

var br = createExpBoarder(300);
//$('.currentLine').append(br);
var position2 = $('.currentLine').find('span.cm-variable-2').position();
//$('#expBoarder').html(position2.top + ' ' + position2.left);

var str = "";
//str = $('.currentLine').parent().html();
str = $('.currentLine').siblings().find(':contains("start")').html();

$('#expBoarder').html( str );
$('#expBoarder').offset({ left: 50.0 });

