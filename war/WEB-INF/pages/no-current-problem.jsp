<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="http://fonts.googleapis.com/css?family=Lato">
		<link rel="stylesheet" type="text/css" href="/_static/css/style.css">
	</head>
	
	<body>
	
		<div class="centered">
			<c:if test="${user == null}">
				<h1>Welcome to Class. Your Class is not Currently in Session.</h1>
				<h3>Please <a href="${loginUrl}">Log in</a> and wait for your teacher to proceed.</h3>
				<h3>Periodically <a href="/home">Reload</a> this page to see if your class has begun.</h3>
			</c:if>
		
			<c:if test="${user != null}">
				<h1>Welcome, ${user.nickname}</h1>
				<h3>Your class is not currently in session.</h3>
				<h3>But feel free to <a href="/history">view your previous responses</a>.</h3>
				<h3>Periodically <a href="/home">Reload</a> this page to see if your class has begun.</h3>
				<h3>or <a href="${logoutUrl}">log out.</h3>
			</c:if>
		</div>
		
	</body>
</html>