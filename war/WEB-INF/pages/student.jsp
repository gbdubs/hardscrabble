<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	
	<head>
		<link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Lato">
		<link rel="stylesheet" type="text/css" href="/_static/css/style.css">
	</head>
	
	<body>
	
		<c:if test="${user == null}">
			<div class="centered">
				<h1>Welcome to Class.</h1>
				<h3>Please <a href="${loginUrl}">Log in</a> to continue.</h3>
			</div>	
		</c:if>
		
		<div class="centered phase-waiting phase-done phase">
			<c:if test="${user != null}">
				<div class="waiting-message">
					<h1>Welcome, ${user.nickname}</h1>
					<h3>The Example Will Begin Shortly.</h3>
				</div>
				<div class="done-message hidden">
					<h1>Exercise Complete</h1>
					<h3>The example has now closed. Goodbye!</h3>
				</div>	
				<h3>Or you can <a href="/history">view your previous responses</a></h3>
				<h3>or <a href="${logoutUrl}">log out.</a></h3>
			</c:if>
		</div>
		
		<div class="centered phase-pre phase hidden">
			
			<h1>Pre-Test Question</h1>
			
			<div class="timer">[Not Timed]</div>
			
			<h3>Prompt</h3>
			<div id="pre-test-question"></div>
			
			<h3>Your Response</h3>
			<div id="pre-test-response"></div>
		
		</div>
		
		<div class="centered phase-question phase hidden">
			
			<h1>Question</h1>
			
			<div class="timer">[Not Timed]</div>
			
			<h3>Prompt</h3>
			<div id="question"></div>
			
			<h3>Your Response</h3>
			<div id="question-response"></div>
		
		</div>
		
		<div class="centered phase-comment phase hidden">
			
			<h1>Commenting</h1>
			
			<div class="timer">[Not Timed]</div>
			
			<h3>Partner's Response and Your Comments</h3>
			<div class="split-screen-code-editors">
				<div id="comment-prompt"></div>
				<div id="comment-response"></div>
			</div>
		</div>
		
		<div class="centered phase-chat phase hidden">
			
			<h1>Discuss</h1>
			
			<div class="timer">[Not Timed]</div>
			
			<h3>Your Code</h3>
			<div class="split-screen-code-editors">
				<div id="chat-my-response"></div>
				<div id="chat-their-comments"></div>
			</div>
			
			<h3>Chat</h3>
			<div id="chat-box">
				<input type="hidden" id="chat-room-uuid" value=""/>
				<input type="hidden" id="user-id" value=""/>
				<div class="chat-area">
					<div class="please-wait"><div>Please wait for the Chat to Begin</div></div>
				</div>
				<div id="your-message-wrapper-template" class="message-wrapper your-message-wrapper hidden"><div></div></div>
				<div id="their-message-wrapper-template" class="message-wrapper their-message-wrapper hidden"><div></div></div>
				<input type="text" id="chat-box-input"/>
			</div>
			
			<h3>Their Code</h3>
			<div class="split-screen-code-editors">
				<div id="chat-their-response"></div>
				<div id="chat-my-comments"></div>
			</div>
		</div>
		
		<div class="centered phase-solution phase hidden">
			
			<h1>Solution</h1>
			<div id="solution"></div>
			
			<h3>My Code and Partner's Comments</h3>
			<div class="split-screen-code-editors">
				<div id="my-response"></div>
				<div id="partner-comments"></div>
			</div>
		
		</div>
		
		<div class="centered phase-post phase hidden">
			
			<h1>Post-Test Question</h1>
			
			<div class="timer">[Not Timed]</div>
			
			<div id="post-test-question"></div>
			
			<div id="post-test-response" placeholder="Type in your response here"></div>
		
		</div>
		
		<script src="/_static/js/ace-min-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
		<script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
		<script src="/_static/js/student.js" type="text/javascript"></script>
	</body>
</html>