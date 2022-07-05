package com.wayapaychat.temporalwallet.config;

import com.wayapaychat.temporalwallet.SpringApplicationContext;
import com.wayapaychat.temporalwallet.pojo.LogMessage;
import com.wayapaychat.temporalwallet.pojo.LogRequest;
import com.wayapaychat.temporalwallet.service.LogService;
import com.wayapaychat.temporalwallet.util.HttpRequestResponseUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("preHandle :: " + response.getHeaderNames());
        long startTime = System.currentTimeMillis();

        log(request, response, handler,System.currentTimeMillis() - startTime);

//        HandlerMethod handlerMethod = (HandlerMethod) handler;
//
//        String emailAddress = request.getParameter("username");
//        String password = request.getParameter("password");
//
//        if(StringUtils.isEmpty(emailAddress) || StringUtils.containsWhitespace(emailAddress) ||
//                StringUtils.isEmpty(password) || StringUtils.containsWhitespace(password)) {
//            throw new Exception("Invalid User Id or Password. Please try again.");
//        }

        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
            System.out.println("postHandle :: " + request.getMethod());

//        HttpRequestResponseUtils.updateResponse(response);
        System.out.println("response :: " + response);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception exception) throws Exception {
        System.out.println("afterCompletion :: " + request.getMethod());
    }

    public void afterCompletion(WebRequest webRequest, Exception e) throws Exception {
        System.out.println("afterCompletion :: " + webRequest);
    }

    private void log(HttpServletRequest request, HttpServletResponse response, Object handler,
                     long timeTaken) {

        final String ip = HttpRequestResponseUtils.getClientIpAddress();
        final String url = HttpRequestResponseUtils.getRequestUrl();
        final String page = HttpRequestResponseUtils.getRequestUri();
        final String refererPage = HttpRequestResponseUtils.getRefererPage();
        final String queryString = HttpRequestResponseUtils.getPageQueryString();
        final String userAgent = HttpRequestResponseUtils.getUserAgent();
        final String requestMethod = HttpRequestResponseUtils.getRequestMethod();
        final LocalDateTime timestamp = LocalDateTime.now();

        System.out.println("page" + page);
        System.out.println("refererPage" + refererPage);
        System.out.println("userAgent" + userAgent);

        final String responsePayLoad = HttpRequestResponseUtils.getResponsePayload(response);

        LogMessage logMessage = new LogMessage();
        logMessage.setHttpStatus(response.getStatus());
        logMessage.setHttpMethod(request.getMethod());
        logMessage.setClientIP(ip);
        logMessage.setTimeTakenMs(timeTaken);
        logMessage.setPath(url);
        logMessage.setResponse(responsePayLoad);
        logMessage.setJavaMethod(handler == null ? "null" : handler.toString());
        logMessage.setRequestParams(queryString);


        logRequestAndResponse(logMessage,1L);
    }

    private void logRequestAndResponse(LogMessage message, Long id){
		String httpMethod = message.getHttpMethod(), action;

		switch(httpMethod){
			case "GET":
			case "PUT":
				action = "MODIFY";
				break;
			case "DELETE":
				action = "DELETE";
				break;
			default:
				action = "CREATE";
		}
		LogService userService = ((LogService) SpringApplicationContext.getBean("logServiceImpl"));
		LogRequest pojo = new LogRequest();
		pojo.setAction(action);
		String mess = "TemporalWallet Service: " + message.getPath();
		pojo.setMessage(mess);
		pojo.setJsonRequest(message.getRequestBody());
		pojo.setJsonResponse(message.getResponse());
		pojo.setUserId(id);
		String controller = message.getJavaMethod();
		if(controller != null && !controller.isBlank() && controller.length() > 45){
			controller = controller.substring(46, controller.indexOf("#"));
		}

		pojo.setModule(controller);

        LogRequest pojo2 = new LogRequest();
        pojo2.setAction(action);
        pojo2.setEmail("agbe.terseeer@gmail.com");
        pojo2.setJsonRequest("string");
        pojo2.setJsonResponse("{\"timestamp\":\"Mon Jul 04 16:38:44 WAT 2022\",\"message\":\"retrieved successfully\",\"status\":true,\"data\":{\"id\":\"cac0c592-de2b-436a-96b5-9b1bd2ec4faa\",\"email\":\"agbe.terseer@gmail.com\",\"firstName\":\"Terseer\",\"surname\":\"Agbe\",\"dateOfBirth\":\"2020-01-01\",\"gender\":\"MALE\",\"phoneNumber\":\"2347030355396\",\"userId\":\"2\",\"referenceCode\":\"BKiFiPJKzcfygvTq\",\"smsAlertConfig\":true,\"corporate\":true,\"otherDetails\":{\"organisationName\":\"SlitSoft\",\"organisationEmail\":\"agbe.terseer@gmail.com\",\"organisationPhone\":\"2347030355396\",\"organizationCity\":\"MAkurdi\",\"organizationAddress\":\"LAGOS\",\"organizationState\":\"BENUE\",\"organisationType\":\"ICT\",\"businessType\":\"BEANS\"}}}");
        pojo2.setMessage(mess);
        pojo2.setModule("string");
        pojo2.setUserId(id);
        pojo2.setLocation("string");
        pojo2.setName("string");
        pojo2.setPhoneNumber("string");
		if(userService != null)

		    //ogPojo :: LogRequest(id=null, action=MODIFY, jsonRequest=null, jsonResponse={"timestamp":"Mon Jul 04 16:38:44 WAT 2022","message":"retrieved successfully","status":true,"data":{"id":"cac0c592-de2b-436a-96b5-9b1bd2ec4faa","email":"agbe.terseer@gmail.com","firstName":"Terseer","surname":"Agbe","dateOfBirth":"2020-01-01","gender":"MALE","phoneNumber":"2347030355396","userId":"2","referenceCode":"BKiFiPJKzcfygvTq","smsAlertConfig":true,"corporate":true,"otherDetails":{"organisationName":"SlitSoft","organisationEmail":"agbe.terseer@gmail.com","organisationPhone":"2347030355396","organizationCity":"MAkurdi","organizationAddress":"LAGOS","organizationState":"BENUE","organisationType":"ICT","businessType":"BEANS"}}}, message=Authentication Service: /api/v1/profile/2, module=ProfileController, requestDate=2022-07-04T16:38:44.988065800, responseDate=2022-07-04T16:38:44.988065800, userId=2, email=agbe.terseer@gmail.com, name=TERSEER AGBE, phoneNumber=234703355396, location=null)
            System.out.println("pojo :: " +pojo2);
			userService.saveLog(pojo2);
	}


}
