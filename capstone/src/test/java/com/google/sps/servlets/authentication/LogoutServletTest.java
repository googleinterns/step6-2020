package com.google.sps.servlets.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.io.*;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class LogoutServletTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(false);

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  /*
   *  Check the doGet function from Logout Servlet. Create a list of cookies
   *  and call the get function. It should return all the cookies with max age
   *  set to zero. This means that the cookie will be removed.
   **/
  @Test
  public void logOutWithCookiesReturnMaxAgeToZero() throws ServletException, IOException {
    Cookie cookie = new Cookie("SACSID", "testValue");
    Cookie[] cookies = new Cookie[] {cookie};
    when(request.getCookies()).thenReturn(cookies);

    LogoutServlet userServlet = new LogoutServlet();
    userServlet.doGet(request, response);

    ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
    verify(response).addCookie(captor.capture());

    List<Cookie> responseCookies = captor.getAllValues();

    Assert.assertTrue(responseCookies.size() == 1);
    Assert.assertEquals(responseCookies.get(0).getMaxAge(), 0);
  }
}