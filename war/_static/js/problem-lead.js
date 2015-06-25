$(function(){
	
	refreshCheckedIn();
	
	$("#refresh-check-in").click(function(){
		refreshCheckedIn();
	});
	
	function refreshCheckedIn(){
		console.log("attempting to refresh check-in list");
		$.ajax({
			method: "GET",
			url: "/check-in",
			success: function(result){
				$("#checked-in").text(result);
				console.log("refreshed check-in list");
			}
		});
	}
});