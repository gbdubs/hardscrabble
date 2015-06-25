$(function(){

	var preQuestionEditor = ace.edit("pre-question-editor");
	var questionEditor = ace.edit("question-editor");
	var solutionEditor = ace.edit("solution-editor");
	var postQuestionEditor = ace.edit("post-question-editor");

	preQuestionEditor.setTheme("ace/theme/cobalt");
	preQuestionEditor.getSession().setMode("ace/mode/java");
	
	questionEditor.setTheme("ace/theme/cobalt");
	questionEditor.getSession().setMode("ace/mode/java");
	
	solutionEditor.setTheme("ace/theme/cobalt");
	solutionEditor.getSession().setMode("ace/mode/java");
	
	postQuestionEditor.setTheme("ace/theme/cobalt");
	postQuestionEditor.getSession().setMode("ace/mode/java");
	
	$("#save-button").click(function(){
		save();
	});
	
	function save(){

		var uuid = $("#uuid").val();
		var title = $("#title-input").val();
		var preTestQuestion = preQuestionEditor.getValue();
		var preTime = $("#pre-time").val();
		var question = questionEditor.getValue();
		var qTime = $("#question-time").val();
		var commentAlgorithm = $("#comment-algorithm").val();
		var cTime = $("#comment-time").val();
		var solution = solutionEditor.getValue();
		var postTestQuestion = postQuestionEditor.getValue();
		var postTime = $("#post-time").val();
		var chatTime = $("#chat-time").val();

		$.ajax({
			url: "/edit",
			method: "POST",
			data: {
				"uuid": uuid,
				"title": title,
				"preQuestion": preTestQuestion,
				"preTime": preTime,
				"question": question,
				"questionTime": qTime,
				"commentAlgorithm": commentAlgorithm,
				"commentTime": cTime,
				"solution": solution,
				"postQuestion": postTestQuestion,
				"postTime": postTime,
				"chatTime": chatTime
			}
		}).done(function(resp){
			if (resp == "complete"){
				$("#save-button").text("Saved").addClass("btn-green");
			}
		}).fail(function(){
			$("#save-button").text("Save Failed").addClass("btn-red");
		});
	}
});