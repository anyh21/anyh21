String sessionCsrf = UserService.getAttribute("CSRF_TOKEN").toString();
String requestCsrf = ctx.getParameter("_csrf");
if (!sessionCsrf.equals(requestCsrf))
{
  UserService.setAttribute("CSRF_TOKEN", null);
  throw new PermissionDeniedException();
}
