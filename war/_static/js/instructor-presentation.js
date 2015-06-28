$(function(){
	
	refreshCheckedIn("email");
	
	$("#refresh-check-in-nickname").click(function(){
		refreshCheckedIn("nickname");
	});
	
	$("#refresh-check-in-email").click(function(){
		refreshCheckedIn("email");
	});
	
	function refreshCheckedIn(displayType){
		console.log("attempting to refresh check-in list");
		$.ajax({
			method: "GET",
			url: "/check-in",
			data: {
				"display": displayType
			},
			success: function(result){
				$("#checked-in").text(result);
				console.log("refreshed check-in list");
			}
		});
	}
	
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