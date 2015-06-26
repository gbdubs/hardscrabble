$(function(){
	
	// INITIAL CALLS AND PAGE SETUP
	
	checkIn();
	poll();
	initializeTextEditors();
	
	
	
	// SCOPE VARIABLES FOR MULTIPLE FUNCTIONS
	
	
	var currentPhase = "waiting";
	var previousTimerIntervals = [];
	var pollInterval = setInterval(poll, 1000);
	var checkinInterval = setInterval(checkIn, 30000);
	var saveInterval = setInterval(saveCurrentPhase, 20000);
	
	var preTestQuestionPrompt;
	var preTestResponseEditor;
	var questionPrompt;
	var questionResponseEditor;
	var commentPrompt;
	var commentResponseEditor;
	var myResponse;
	var partnerCommments;
	var chatMyResponse;
	var chatTheirComments;
	var chatTheirResponse;
	var chatMyComments;
	var solutionEditor;
	var postTestQuestionPrompt;
	var postTestResponseEditor;
	
	
	
	// TEXT EDITOR INITIALIZATION
	
	function initializeTextEditors(){
		preTestQuestionPrompt = ace.edit("pre-test-question");
		preTestResponseEditor = ace.edit("pre-test-response");
		
		questionPrompt = ace.edit("question");
		questionResponseEditor = ace.edit("question-response");
		
		commentPrompt = ace.edit("comment-prompt");
		commentResponseEditor = ace.edit("comment-response");
		
		myResponse = ace.edit("my-response");
		partnerCommments = ace.edit("partner-comments");
		solutionEditor = ace.edit("solution");
		
		postTestQuestionPrompt = ace.edit("post-test-question");
		postTestResponseEditor = ace.edit("post-test-response");
		
		chatTheirResponse = ace.edit("chat-their-response");
		chatTheirComments = ace.edit("chat-their-comments");
		chatMyComments = ace.edit("chat-my-comments");
		chatMyResponse = ace.edit("chat-my-response");
		
		preTestQuestionPrompt.setReadOnly(true);
		preTestQuestionPrompt.setTheme("ace/theme/cobalt");
		preTestQuestionPrompt.getSession().setMode("ace/mode/java");
		
		preTestResponseEditor.setTheme("ace/theme/cobalt");
		preTestResponseEditor.getSession().setMode("ace/mode/java");
		
		questionPrompt.setReadOnly(true);
		questionPrompt.setTheme("ace/theme/cobalt");
		questionPrompt.getSession().setMode("ace/mode/java");
		
		questionResponseEditor.setTheme("ace/theme/cobalt");
		questionResponseEditor.getSession().setMode("ace/mode/java");
		
		commentPrompt.setReadOnly(true);
		commentPrompt.setTheme("ace/theme/cobalt");
		commentPrompt.getSession().setMode("ace/mode/java");
		
		commentResponseEditor.setTheme("ace/theme/cobalt");
		commentResponseEditor.getSession().setMode("ace/mode/java");
		
		myResponse.setReadOnly(true);
		myResponse.setTheme("ace/theme/cobalt");
		myResponse.getSession().setMode("ace/mode/java");
		
		partnerCommments.setReadOnly(true);
		partnerCommments.setTheme("ace/theme/cobalt");
		partnerCommments.getSession().setMode("ace/mode/java");
		
		solutionEditor.setReadOnly(true);
		solutionEditor.setTheme("ace/theme/cobalt");
		solutionEditor.getSession().setMode("ace/mode/java");
		
		postTestQuestionPrompt.setReadOnly(true);
		postTestQuestionPrompt.setTheme("ace/theme/cobalt");
		postTestQuestionPrompt.getSession().setMode("ace/mode/java");
		
		postTestResponseEditor.setTheme("ace/theme/cobalt");
		postTestResponseEditor.getSession().setMode("ace/mode/java");
		
		chatTheirResponse.setReadOnly(true);
		chatTheirResponse.setTheme("ace/theme/cobalt");
		chatTheirResponse.getSession().setMode("ace/mode/java");
		
		chatMyResponse.setReadOnly(true);
		chatMyResponse.setTheme("ace/theme/cobalt");
		chatMyResponse.getSession().setMode("ace/mode/java");
		
		chatTheirComments.setReadOnly(true);
		chatTheirComments.setTheme("ace/theme/cobalt");
		chatTheirComments.getSession().setMode("ace/mode/java");
		
		chatMyComments.setReadOnly(true);
		chatMyComments.setTheme("ace/theme/cobalt");
		chatMyComments.getSession().setMode("ace/mode/java");
	}
	
	

	// THE CHECK IN FUNCTION (Tells the system that the user is still on the page)
	
	function checkIn(){
		$.post("/check-in").fail(function(){
			console.log("The Check-In Process failed, so it has been halted. Restart the page to begin it again.");
			clearInterval(checkinInterval);
		});
	}
	
	
	
	// THE POLLING FUNCTION (looks for state changes, then updates the page if they occur).
	
	function poll(){
		$.ajax({
	    	url: "/home",
	    	data: {
	    		"getPhase": "phase"
	    	}
	    }).done(function(result) {
			if (result != currentPhase){
				console.log("Switching from phase " + currentPhase + " to Phase: " + result);
				saveCurrentPhase();
				updatePhaseTo(result);
				currentPhase = result;
			}
		}).fail(function() {
			console.log("There was an error in polling, so it has been turned off. Restart the page to resume it.");
			clearInterval(pollInterval);
			clearInterval(saveInterval);
		});
	}
	
	
	
	// Changes to a new phase, and performs the needed page updates.
	
	function updatePhaseTo(phase){
		$(".phase").addClass("hidden");
		updateTimer();
		if (phase == "waiting"){
			$(".waiting-message").removeClass("hidden");
			$(".done-message").addClass("hidden");
			$(".phase-waiting").removeClass("hidden");
		} else if (phase == "done"){
			$(".waiting-message").addClass("hidden");
			$(".done-message").removeClass("hidden");
			$(".phase-done").removeClass("hidden");
		} else if (phase == "pre"){
			$(".phase-pre").removeClass("hidden");
			getPreTestQuestion();
		} else if (phase == "question"){
			$(".phase-question").removeClass("hidden");
			getQuestion();
		} else if (phase == "comment"){
			$(".phase-comment").removeClass("hidden");
			getPartnerQuestionResponse();
		} else if (phase == "chat"){
			$(".phase-chat").removeClass("hidden");
			initializeChat();
			getChatMyResponse();
			getChatMyComments();
			getChatTheirResponse();
			getChatTheirComments();
		} else if (phase == "solution"){
			$(".phase-solution").removeClass("hidden");
			getSolution();
			getPartnerCommentResponse();
			getMyResponse();
		} else if (phase == "post"){
			$(".phase-post").removeClass("hidden");
			getPostTestQuestion();
		}
	}
	
	// A series of functions which simply gather information and display it on the page asynchronously.
	
	function getPreTestQuestion(){
		$.ajax({
			url: "/home",
			data: {
				"getPreQuestion": ""
			}
		}).done(function(result){
			preTestQuestionPrompt.setValue(result, 0);
			preTestResponseEditor.clearSelection();
		}).fail(function(){
			preTestResponseEditor.setValue("Pre Test Question Could Not be Loaded due to a server error.", 0);
			preTestResponseEditor.clearSelection();
		});
	}
	
	function getQuestion(){
		$.ajax({
			url: "/home",
			data: {
				"getQuestion": ""
			}
		}).done(function(result){
			questionPrompt.setValue(result, 0);
			questionPrompt.clearSelection();
		}).fail(function(){
			questionPrompt.setValue("The Question Could Not be Loaded due to a server error.", 0);
			questionPrompt.clearSelection();
		});
	}
	
	function getPostTestQuestion(){
		$.ajax({
			url: "/home",
			data: {
				"getPostQuestion": ""
			}
		}).done(function(result){
			postTestQuestionPrompt.setValue(result, 0);
			postTestQuestionPrompt.clearSelection();
		}).fail(function(){
			postTestQuestionPrompt.setValue("The Post Test Question Could Not be Loaded due to a server error.", 0);
			postTestQuestionPrompt.clearSelection();
		});
	}
	
	function getSolution(){
		$.ajax({
			url: "/home",
			data: {
				"getSolution": ""
			}
		}).done(function(result){
			solutionEditor.setValue(result, 0);
			solutionEditor.clearSelection();
		}).fail(function(){
			solutionEditor.setvalue("The Solution Could Not be Loaded due to a server error.", 0);
			solutionEditor.clearSelection();
		});
	}
	
	function getPartnerQuestionResponse(){
		$.ajax({
			url: "/home",
			data: {
				"getPartnerQuestionResponse": ""
			}
		}).done(function(result){
			commentPrompt.setValue(result, 0);
			commentPrompt.clearSelection();
		}).fail(function(){
			commentPrompt.setValue("Your Partner's Response Could Not be Loaded due to a server error.", 0);
			commentPrompt.clearSelection();
		});
	}
	
	function getPartnerCommentResponse(){
		$.ajax({
			url: "/home",
			data: {
				"getPartnerCommentResponse": ""
			}
		}).done(function(result){
			partnerComments.setValue(result, 0);
			partnerComments.clearSelection();
		}).fail(function(){
			partnerComments.setValue("Your Partner's Comments Could Not be Loaded due to a server error.", 0);
			partnerComments.clearSelection();
		});
	}
	
	function getMyResponse(){
		$.ajax({
			url: "/home",
			data: {
				"getMyResponse": ""
			}
		}).done(function(result){
			myResponse.setValue(result, 0);
			myResponse.clearSelection();
		}).fail(function(){
			myResponse.setValue("Your Responses Could Not be Loaded due to a server error.", 0);
			myResponse.clearSelection();
		});
	}
	
	function getChatMyResponse(){
		$.ajax({
			url: "/home",
			data: {
				"getMyResponse": ""
			}
		}).done(function(result){
			chatMyResponse.setValue(result, 0);
			chatMyResponse.clearSelection();
		}).fail(function(){
			chatMyResponse.setValue("Your Responses Could Not be Loaded due to a server error.", 0);
			chatMyResponse.clearSelection();
		});
	}
	
	function getChatTheirComments(){
		$.ajax({
			url: "/home",
			data: {
				"getPartnerCommentResponse": ""
			}
		}).done(function(result){
			chatTheirComments.setValue(result, 0);
			chatTheirComments.clearSelection();
		}).fail(function(){
			chatTheirComments.setValue("Your Partner's Comments Could Not be Loaded due to a server error.", 0);
			chatTheirComments.clearSelection();
		});
	}
	
	function getChatTheirResponse(){
		$.ajax({
			url: "/home",
			data: {
				"getPartnerQuestionResponse": ""
			}
		}).done(function(result){
			chatTheirResponse.setValue(result, 0);
			chatTheirResponse.clearSelection();
		}).fail(function(){
			chatTheirResponse.setValue("Your Partner's Response Could Not be Loaded due to a server error.", 0);
			chatTheirResponse.clearSelection();
		});
	}
	
	function getChatMyComments(){
		$.ajax({
			url: "/home",
			data: {
				"getMyComments": ""
			}
		}).done(function(result){
			chatMyComments.setValue(result, 0);
			chatMyComments.clearSelection();
		}).fail(function(){
			chatMyComments.setValue("Your Comments on your Partner's Response Could Not be Loaded due to a server error.", 0);
			chatMyComments.clearSelection();
		});
	}
	
	
	
	// THE TIMER (Updates the timer based on the number of seconds on the general clock.  This is only called ONCE per phase change).
	
	function updateTimer(){	
		getTimerSeconds().done(function(result) {
			$(".timer").each(function(){
				var currentTimingPhase = currentPhase;
				var remaining = parseInt(result);
				
				if (remaining == -1){
					$(this).addClass("hidden");
				} else {
					$(this).removeClass("hidden");
				}
				var timerElement = this;
				var counter = setInterval(timer, 1000);
				previousTimerIntervals.push(counter);
				
				function timer() {
				  remaining = remaining - 1;
				  if (remaining <= 0 || currentTimingPhase != currentPhase) {
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
	}

	function getTimerSeconds(){
		return $.ajax({
			url: "/home",
			data: {
				"getTimer": "timer"
			}
		});
	}
	
	
	
	// THE SAVE FUNCTIONS (Saves each of the fields (only of the current phase)). Called both on a 20-second interval, and manually.
	
	function saveCurrentPhase(){
		if (currentPhase == "pre"){
			savePreTestResponse();
			console.log("SAVED PRE-TEST RESPONSE");
		} else if (currentPhase == "question"){
			saveQuestionResponse();
			console.log("SAVED QUESTION RESPONSE");
		} else if (currentPhase == "comment"){
			saveCommentResponse();
			console.log("SAVED COMMENT RESPONSE");
		} else if (currentPhase == "post"){
			savePostTestResponse();
			console.log("SAVED POST-TEST RESPONSE");
		}
	}
	
	function savePreTestResponse(){
		var response = preTestResponseEditor.getValue();
		$.ajax({
			url: "/home",
			method: "POST",
			data: {
				"response": response,
				"currentPhase": currentPhase,
			}
		});
	}

	function savePostTestResponse(){
		var response = postTestResponseEditor.getValue();
		$.ajax({
			url: "/home",
			method: "POST",
			data: {
				"response": response,
				"currentPhase": currentPhase,
			}
		});
	}
	
	function saveQuestionResponse(){
		var response = questionResponseEditor.getValue();
		$.ajax({
			url: "/home",
			method: "POST",
			data: {
				"response": response,
				"currentPhase": currentPhase,
			}
		});
	}

	function saveCommentResponse(){
		var response = commentResponseEditor.getValue();
		$.ajax({
			url: "/home",
			method: "POST",
			data: {
				"response": response,
				"currentPhase": currentPhase,
			}
		});
	}
	
	
	
	
	// CHATROOM VARIABLES AND SETUP
	
	var chatPollingInterval;
	var mostRecentMessage = 0;
	var chatRoomId = $("#chat-room-uuid");
	var userId = $("#user-id");
	var allMessages = "";
	
	var chatArea = $("#chat-box .chat-area");
	var chatTheirTemplate = $("#chat-box #their-message-wrapper-template");
	var chatYourTemplate = $("#chat-box #your-message-wrapper-template");
	var chatBoxInput = $("#chat-box-input");
	
	function addTheirNewMessageToScreen(message){
		var clone = $(chatTheirTemplate).clone().removeClass("hidden");
		$(chatArea).append(clone);
		$("div", clone).text(message);
		$(chatArea).scrollTop(chatArea.prop("scrollHeight"));
	}
	
	function addYourNewMessageToScreen(message){
		var clone = $(chatYourTemplate).clone().removeClass("hidden");
		$(chatArea).append(clone);
		$("div", clone).text(message);
		$(chatBoxInput).val("");
		$(chatArea).scrollTop(chatArea.prop("scrollHeight"));
	}
	
	$(chatBoxInput).keypress(function(e){
		 var code = (e.keyCode ? e.keyCode : e.which);
		 if (code == 13){
			 var message = $(chatBoxInput).val();
			 if (message.trim().length > 0){
				 sendMessageToChatRoom(message);
				 addYourNewMessageToScreen(message);
			 }
		 }
	});
	
	function initializeChat(){
		$(chatArea).removeClass("uninitialized").addClass("initializing");
		$.ajax({
			url: '/chat',
			method: 'GET',
			data: {
				'getChatRoomUuid': 'please'
			}
		}).done(function(data){
			$(chatRoomId).val(data);
			var userIdValue = $(userId).val();
			if (typeof userIdValue != 'undefined'){
				$(chatArea).removeClass("initializing");
				chatPollingInterval = setInterval(pollForNewChatMessages, 1000);
			}
			console.log("Set ChatroomID To: " + chatRoomId);
		}).fail(function(){
			console.log("There was an error in initializing the Chat. Please try again by reloading the page.");
		});
		
		$.ajax({
			url: '/chat',
			method: 'GET',
			data: {
				'getUserId': 'please'
			}
		}).done(function(data){
			$(userId).val(data);
			var chatRoomIdValue = $(chatRoomId).val();
			if (typeof chatRoomIdValue != 'undefined'){
				$(chatArea).removeClass("initializing");
				chatPollingInterval = setInterval(pollForNewChatMessages, 1000);
			}
			console.log("Set UserID To: " + userId);
		}).fail(function(){
			console.log("There was an error in initializing the Chat. Please try again by reloading the page.");
		});
	}
	
	
	function pollForNewChatMessages(){
		
		if ($(chatArea).hasClass("uninitialized")){
			initializeChat();
		} else if ($(chatArea).hasClass("initializing")){
			setTimeout(pollForNewChatMessages(), 1000);
		} else {
			$.ajax({
				url: "/chat",
				method: "GET",
				data: {
					"userId": $(userId).val(),
					"chatRoomUuid": $(chatRoomId).val(),
					"mostRecentMessage": mostRecentMessage
				}
			}).done(function(data){
				console.log(data);
				if (allMessages.indexOf(data) == -1){
					allMessages = allMessages + data;
					data = data.substring(10);
					var messages = data.split("MESSAGEBREAK");
					for (var i in messages){
						var message = messages[i];
						if (message.length > 1){
							mostRecentMessage++;
							addTheirNewMessageToScreen(message);
						}
					}
				}
			}).fail(function(){
				clearInterval(chatPollingInterval);
				console.log("There was a server error in the chat polling.  Reload the page to try again.")
			});
		}
	}
	
	function sendMessageToChatRoom(message){
		$.ajax({
			url: '/chat',
			method: 'post',
			data: {
				"chatRoomUuid": $(chatRoomId).val(),
				"userId": $(userId).val(),
				"message": message
			}
		});
	}
	
	
});
