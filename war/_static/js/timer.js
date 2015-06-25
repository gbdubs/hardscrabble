$(function(){
	
	$(".timer").each(function(){
		
		var remaining = $(this).data("remaining");
		var timerElement = this;
		var counter = setInterval(timer, 1000);

		function timer() {
		  remaining = remaining - 1;
		  if (remaining <= 0) {
		     clearInterval(counter);
		     return;
		  }
		  
		  var remainingMinutes = Math.floor(remaining / 60);
		  var remainingSeconds = remaining % 60;
		  if (remainingSeconds < 10){
			  remainingSeconds = "0" + remainingSeconds;
		  }
		
		  var clockface = remainingMinutes + ":" + remainingSeconds;

		  $(timerElement).text(clockface);
		}
		
	});
	
});