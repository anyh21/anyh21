protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws Exception
{
  if (userSession.getAttribute("CSRF_TOKEN") == null)
  {
    userSession.setAttribute("CSRF_TOKEN", UUID.randomUUID().toString());
  }
  String sessionCsrf = userSession.getAttribute("CSRF_TOKEN").toString();
  res.setHeader("X-CSRF-TOKEN", sessionCsrf);
}  
