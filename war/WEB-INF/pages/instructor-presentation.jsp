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
				<div class="full-width-centered">
					<a href="/instructor" class="btn">Back to Dashboard</a>
				<div class="full-width-centered">
				<div class="split-screen">
					<h2>Current Problem: ${problem.title}</h2>
			
					<button id="refresh-check-in-email" class="btn btn-blue">Refresh Participants Emails</button>	
					<button id="refresh-check-in-nickname" class="btn btn-blue">Refresh Participants Names</button>	
					<p id="checked-in"></p>
				</div>
				<br/>
				<div class="split-screen">
					<h2>Current Phase: <b class="current-phase">${problem.currentPhase}</h2>
					<form action="/problem/" method="post">
						<input type="hidden" name="uuid" value="${problem.uuid}"/>
						<input type="hidden" name="advance" value="advance"/>
						<button class="btn btn-red">
							Advance to Next Phase: <b class="next-phase">${problem.nextPhase}</b>
						</button>
					</form>
					<div class="timer timer-big" data-remaining="${problem.secondsLeftInPhase}">0:00</div>
				</div>
			</div>
		</div>
		
		<script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
		<script src="/_static/js/instructor-presentation.js" type="text/javascript"></script>
	</body>
</html>