<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Lato">
		<link rel="stylesheet" type="text/css" href="/_static/css/style.css">
	</head>
	<body>
		<div class="content-wrapper">
			<div class="content">
				<h1>Problem Editor</h1>
				<input type="hidden" id="uuid" value="${problem.uuid}"/>
				<div class="editor-wrapper">
					<h3>Problem Title</h3>
					<input id="title-input" type="text" value="${problem.title}"/>
				</div>
				<div class="editor-wrapper">
					<h3>Pre-test Question</h3>
					<div id="pre-question-editor">${problem.preQuestion}</div>
					<label class="timing-label">Duration (In Seconds)</label>
					<input class="timing-input" id="pre-time" type="number" value="${problem.preTime}"></input>
				</div>
				<div class="editor-wrapper">
					<h3>Question</h3>
					<div id="question-editor">${problem.question}</div>
					<label class="timing-label">Duration (In Seconds)</label>
					<input class="timing-input" id="quesiton-time" type="number" value="${problem.questionTime}"></input>
				</div>
				<div class="editor-wrapper">
					<h3>Paired Commenting</h3>
					<select id="comment-algorithm">
						<option value="none" ${problem.commentAlgorithm == "none" ? 'selected="selected"' : ''}>None</option>
						<option value="random" ${problem.commentAlgorithm == "random" ? 'selected="selected"' : ''}>Random Pairs</option>
						<option value="edit-distance" ${problem.commentAlgorithm == "edit-distance" ? 'selected="selected"' : ''}>Edit Distance</option>
						<option value="length" ${problem.commentAlgorithm == "length" ? 'selected="selected"' : ''}>Length</option>
						<option value="inv-edit-distance" ${problem.commentAlgorithm == "inv-edit-distance" ? 'selected="selected"' : ''}>Inverted Edit Distance</option>
						<option value="inv-length" ${problem.commentAlgorithm == "inv-length" ? 'selected="selected"' : ''}>Inverted Length</option>
					</select>
					<label class="timing-label">Duration (In Seconds)</label>
					<input class="timing-input" id="comment-time" type="number" value="${problem.commentTime}"></input>
				</div>
				<div class="editor-wrapper">
					<h3>Post-Commenting Chat</h3>
					<label class="timing-label">Duration (In Seconds) (Leave blank to omit)</label>
					<input class="timing-input" id="chat-time" type="number" value="${problem.chatTime}"></input>
				</div>
				<div class="editor-wrapper">
					<h3>Solution</h3>
					<div id="solution-editor">${problem.solution}</div>
				</div>
				<div class="editor-wrapper">
					<h3>Post-Test Question</h3>
					<div id="post-question-editor">${problem.postQuestion}</div>
					<label class="timing-label">Duration (In Seconds)</label>
					<input class="timing-input" id="post-time" type="number" value="${problem.postTime}"></input>
				</div>
			
				<div class="action-buttons">
					<button id="save-button" class="btn btn-blue">Save</button>
				
					<a href="/antonella" class="btn btn-white">Return to Problem Browser</a>
				
					<form action="/edit" method="post">
						<input type="hidden" name="delete" value="${problem.uuid}"/>
						<input type="hidden" name="uuid" value="${problem.uuid}"/>
						<button class="btn btn-red">Delete</button>
					</form>
				</div>
			</div>
		</div>
		
		<script src="/_static/js/ace-min-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
		<script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
		<script src="/_static/js/editor.js" type="text/javascript"></script>
	</body>
</html>