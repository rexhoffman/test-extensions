<%@page import="org.ehoffman.testing.example.webapp.Simple"%>
<html>
<body>
<p id="message">
Current Date time: <%= Simple.getValue() %>
</p>
  <form id="form">
    Some input: <input name="sometext" id="text" type="text" value="defaultValue">
  </form>
</body>
</html>