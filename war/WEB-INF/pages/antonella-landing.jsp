<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Lato">
		<link rel="stylesheet" type="text/css" href="/_static/css/style.css">
	</head>
	<body>
		<div class="content-wrapper">
			<div class="content narrow centered">
				<h1>Welcome!</h1> 
				<h3>Select a Problem To Edit or Teach, or create a new problem</h3>
	

				<div class="problem-table">
					<div class="problem-table-row header">
						<b>Problem Title</b>
						<b>Last Edited</b>
						<b>Edit Now</b>
						<b>Start Now</b>
					</div>
					<c:forEach items="${allProblems}" var="problem"> 
						<div class="problem-table-row">
							<span>${problem.title}</span>
							<span>${problem.lastEdit}</span>
							<a href="/edit/${problem.uuid}" class="btn btn-blue">Edit</a>
							<a href="/problem/lead/${problem.uuid}" class="btn">Begin</a>
						</div>
					</c:forEach>
		
				</div>
				<div class="full-width-centered">
					<a href="/edit" class="btn">Add New Problem</a>
				</div>
			</div>
		</div>
		<script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
	</body>
</html>