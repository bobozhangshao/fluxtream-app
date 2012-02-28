define(["core/Application",
        "applications/log/Diary",
        "libs/jquery.qtip"],
        function(Application, Diary)
{
	
	Date.prototype.format=function(format){var returnStr='';var replace=Date.replaceChars;for(var i=0;i<format.length;i++){var curChar=format.charAt(i);if(i-1>=0&&format.charAt(i-1)=="\\"){returnStr+=curChar}else if(replace[curChar]){returnStr+=replace[curChar].call(this)}else if(curChar!="\\"){returnStr+=curChar}}return returnStr};Date.replaceChars={shortMonths:['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],longMonths:['January','February','March','April','May','June','July','August','September','October','November','December'],shortDays:['Sun','Mon','Tue','Wed','Thu','Fri','Sat'],longDays:['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'],d:function(){return(this.getDate()<10?'0':'')+this.getDate()},D:function(){return Date.replaceChars.shortDays[this.getDay()]},j:function(){return this.getDate()},l:function(){return Date.replaceChars.longDays[this.getDay()]},N:function(){return this.getDay()+1},S:function(){return(this.getDate()%10==1&&this.getDate()!=11?'st':(this.getDate()%10==2&&this.getDate()!=12?'nd':(this.getDate()%10==3&&this.getDate()!=13?'rd':'th')))},w:function(){return this.getDay()},z:function(){var d=new Date(this.getFullYear(),0,1);return Math.ceil((this-d)/86400000)}, W:function(){var d=new Date(this.getFullYear(),0,1);return Math.ceil((((this-d)/86400000)+d.getDay()+1)/7)},F:function(){return Date.replaceChars.longMonths[this.getMonth()]},m:function(){return(this.getMonth()<9?'0':'')+(this.getMonth()+1)},M:function(){return Date.replaceChars.shortMonths[this.getMonth()]},n:function(){return this.getMonth()+1},t:function(){var d=new Date();return new Date(d.getFullYear(),d.getMonth(),0).getDate()},L:function(){var year=this.getFullYear();return(year%400==0||(year%100!=0&&year%4==0))},o:function(){var d=new Date(this.valueOf());d.setDate(d.getDate()-((this.getDay()+6)%7)+3);return d.getFullYear()},Y:function(){return this.getFullYear()},y:function(){return(''+this.getFullYear()).substr(2)},a:function(){return this.getHours()<12?'am':'pm'},A:function(){return this.getHours()<12?'AM':'PM'},B:function(){return Math.floor((((this.getUTCHours()+1)%24)+this.getUTCMinutes()/60+this.getUTCSeconds()/ 3600) * 1000/24)}, g:function(){return this.getHours()%12||12},G:function(){return this.getHours()},h:function(){return((this.getHours()%12||12)<10?'0':'')+(this.getHours()%12||12)},H:function(){return(this.getHours()<10?'0':'')+this.getHours()},i:function(){return(this.getMinutes()<10?'0':'')+this.getMinutes()},s:function(){return(this.getSeconds()<10?'0':'')+this.getSeconds()},u:function(){var m=this.getMilliseconds();return(m<10?'00':(m<100?'0':''))+m},e:function(){return"Not Yet Supported"},I:function(){return"Not Yet Supported"},O:function(){return(-this.getTimezoneOffset()<0?'-':'+')+(Math.abs(this.getTimezoneOffset()/60)<10?'0':'')+(Math.abs(this.getTimezoneOffset()/60))+'00'},P:function(){return(-this.getTimezoneOffset()<0?'-':'+')+(Math.abs(this.getTimezoneOffset()/60)<10?'0':'')+(Math.abs(this.getTimezoneOffset()/60))+':00'},T:function(){var m=this.getMonth();this.setMonth(0);var result=this.toTimeString().replace(/^.+ \(?([^\)]+)\)?$/,'$1');this.setMonth(m);return result},Z:function(){return-this.getTimezoneOffset()*60},c:function(){return this.format("Y-m-d\\TH:i:sP")},r:function(){return this.toString()},U:function(){return this.getTime()/1000}};
	
	var router = new Backbone.Router();
	
	function updateWidgets() {
		$("#widgets").empty();
		require(["applications/log/widgets/clock/ClockWidget"], function(clock) {
			clock.render();
		});
	}
	
	function disableSelection() {
		document.body.onselectstart = function() { return false; };
		document.body.style.MozUserSelect = "none";
		document.body.style.KhtmlUserSelect = "none";
		document.body.unselectable = "on";
	}
		
	var nav, NavModel = Backbone.Model.extend({
		url : "/nav/model.json",
		fetchState: function(navigate) {
			nav.fetch({
				success : function(model, response) {
					if (navigate) {
						router.navigate("/app/log/" + response.state);
						FlxState.saveState("log", response.state);
					}
					$("#currentTimespanLabel").html(response.currentTimespanLabel);
					Diary.handleComments();
					updateWidgets();
				},
				error : function() {
					alert("error");
				}
			});
		}
	});
	
	function initialize() {
		nav = new NavModel();
		disableSelection();
		var now = new Date(),
			format = 'Y-m-d',
			today = now.format(format);
		router.route("/app/log/date/:date", function(date) {gotoDate(date, true);})
		var options = {
				format: format,
				date:today,
				current:today,
				position:'r',
				onChange:function(){
					$('#currentTimespanLabel').DatePickerHide();
					gotoDate($('#currentTimespanLabel').DatePickerGetDate(true), true);
				}
		};
		bindNavigationEvents();
		Diary.handleComments();
		restoreState();
		if (!window.historyStarted) {
			Backbone.history.start({pushState: true});
			window.historyStarted = true;
		}
	}
	
	function restoreState() {
		var savedState = FlxState.getState("log");
		console.log("savedState: " + savedState);
		if (savedState==null) {
			gotoToday();
		} else {
			nav.url = "/nav/" + FlxState.getState("log");
			nav.fetchState(true);
		}
	}
	
	function incrementTimespan() {
		nav.url = "/nav/incrementTimespan.json";
		nav.fetchState(true);
	}
	
	function decrementTimespan() {
		nav.url = "/nav/decrementTimespan.json";
		nav.fetchState(true);
	}
	
	function gotoToday() {
		nav.url = "/nav/setToToday.json";
		nav.fetchState(true);
	}
	
	function gotoDate(date, navigate) {
		if (nav==null) nav = new NavModel();
		nav.url = "/nav/setDate.json?date=" + date;
		nav.fetchState(navigate);
	}
	
	function bindNavigationEvents() {
		$(".menuNextButton").click(function(e) {
			incrementTimespan(); });
		$(".menuPrevButton").click(function(e) {
			decrementTimespan(); });
		$(".menuTodayButton").click(function(e) {
			gotoToday(); });
	}
	
	var Log = {};
	_.extend(Log, Application);
	Log.name = "log";
	Log.gotoDate = gotoDate;
	Log.initialize = initialize;
	return Log;

});