//package com.wayapaychat.temporalwallet.config;
//
//import com.wayapaychat.temporalwallet.SpringApplicationContext;
//import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
//import com.wayapaychat.temporalwallet.pojo.LogMessage;
//import com.wayapaychat.temporalwallet.pojo.LogRequest;
//import com.wayapaychat.temporalwallet.pojo.MyData;
//import com.wayapaychat.temporalwallet.service.LogService;
//import com.wayapaychat.temporalwallet.util.HttpRequestResponseUtils;
//import org.apache.commons.compress.utils.IOUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.time.LocalDateTime;
//
//@Component
//public class LogInterceptor implements HandlerInterceptor {
//    private long sTime;
//
//    @Autowired
//    private TokenImpl tokenService;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        System.out.println("preHandle :: " + response.getHeaderNames());
//        long startTime = System.currentTimeMillis();
//
//        sTime = startTime;
//        return true;
//    }
//
//    @Override
//    public void postHandle(
//            HttpServletRequest request, HttpServletResponse response, Object handler,
//            ModelAndView modelAndView) throws Exception {
//        final String requestMethod = HttpRequestResponseUtils.getResponsePayload(response);
//
//
//        System.out.println("requestMethod :: " + requestMethod);
//
//        long startTime = System.currentTimeMillis() - sTime;
//        String token = request.getHeader(SecurityConstants.HEADER_STRING);
//
//        MyData userToken = tokenService.getTokenUser(token);
//        if (userToken != null) {
//            log(request, response, handler,startTime, userToken);
//        }
//
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
//                                Object handler, Exception exception) throws Exception {
//        System.out.println("afterCompletion :: " + request.getMethod());
//        System.out.println("afterCompletion :: " + response.toString());
//    }
//
//
//
//    private void log(HttpServletRequest request, HttpServletResponse response, Object handler,
//                     long timeTaken, MyData userToken) {
//
//        final String ip = HttpRequestResponseUtils.getClientIpAddress();
//        final String url = HttpRequestResponseUtils.getRequestUrl();
//        final String page = HttpRequestResponseUtils.getRequestUri();
//        final String refererPage = HttpRequestResponseUtils.getRefererPage();
//        final String queryString = HttpRequestResponseUtils.getPageQueryString();
//        final String userAgent = HttpRequestResponseUtils.getUserAgent();
//        final String requestMethod = HttpRequestResponseUtils.getRequestMethod();
//
//        final LocalDateTime timestamp = LocalDateTime.now();
//
//        System.out.println("url" + url);
//        System.out.println("page" + page);
//        System.out.println("refererPage" + refererPage);
//        System.out.println("userAgent" + userAgent);
//        System.out.println("requestMethod" + requestMethod);
//
//        final String responsePayLoad = HttpRequestResponseUtils.getResponsePayload(response);
//
//        LogMessage logMessage = new LogMessage();
//        logMessage.setHttpStatus(response.getStatus());
//        logMessage.setHttpMethod(request.getMethod());
//        logMessage.setClientIP(ip);
//        logMessage.setTimeTakenMs(timeTaken);
//        logMessage.setPath(url);
//        logMessage.setResponse(responsePayLoad);
//        logMessage.setJavaMethod(handler == null ? "null" : handler.toString());
//        logMessage.setRequestParams(queryString);
//
//
//        logRequestAndResponse(logMessage,1L,userToken);
//    }
//
//    private void logRequestAndResponse(LogMessage message, Long id,  MyData userToken){
//		String httpMethod = message.getHttpMethod(), action;
//
//		switch(httpMethod){
//			case "GET":
//			case "PUT":
//				action = "MODIFY";
//				break;
//			case "DELETE":
//				action = "DELETE";
//				break;
//			default:
//				action = "CREATE";
//		}
//		LogService userService = ((LogService) SpringApplicationContext.getBean("logServiceImpl"));
//		LogRequest pojo = new LogRequest();
//		pojo.setAction(action);
//		String mess = "TemporalWallet Service: " + message.getPath();
//		pojo.setMessage(mess);
//		pojo.setJsonRequest(HttpRequestResponseUtils.objectToJson(message.getRequestBody()).orElse(""));
//		pojo.setJsonResponse(HttpRequestResponseUtils.objectToJson(message.getResponse()).orElse(""));
//		pojo.setUserId(userToken.getId());
//		pojo.setEmail(userToken.getEmail());
//		pojo.setPhoneNumber(userToken.getPhoneNumber());
//		pojo.setName(userToken.getFirstName() + " " + userToken.getSurname());
//		pojo.setLocation("Lagos, Nigeria");
//		String controller = message.getJavaMethod();
//		if(controller != null && !controller.isBlank() && controller.length() > 45){
//			controller = controller.substring(48, controller.indexOf("#"));
//		}
//
//		pojo.setModule(controller);
////
////        LogRequest pojo2 = new LogRequest();
////        pojo2.setAction(action);
////        pojo2.setEmail("agbe.terseeer@gmail.com");
////        pojo2.setJsonRequest(HttpRequestResponseUtils.objectToJson(message.getRequestBody()).orElse(""));
////        pojo2.setJsonResponse(message.getResponse());
//////        pojo2.setJsonResponse("{\"timestamp\":\"Mon Jul 04 16:38:44 WAT 2022\",\"message\":\"retrieved successfully\",\"status\":true,\"data\":{\"id\":\"cac0c592-de2b-436a-96b5-9b1bd2ec4faa\",\"email\":\"agbe.terseer@gmail.com\",\"firstName\":\"Terseer\",\"surname\":\"Agbe\",\"dateOfBirth\":\"2020-01-01\",\"gender\":\"MALE\",\"phoneNumber\":\"2347030355396\",\"userId\":\"2\",\"referenceCode\":\"BKiFiPJKzcfygvTq\",\"smsAlertConfig\":true,\"corporate\":true,\"otherDetails\":{\"organisationName\":\"SlitSoft\",\"organisationEmail\":\"agbe.terseer@gmail.com\",\"organisationPhone\":\"2347030355396\",\"organizationCity\":\"MAkurdi\",\"organizationAddress\":\"LAGOS\",\"organizationState\":\"BENUE\",\"organisationType\":\"ICT\",\"businessType\":\"BEANS\"}}}");
////        pojo2.setMessage(mess);
////        pojo2.setModule(controller);
////        pojo2.setUserId(id);
////        pojo2.setLocation("string");
////        pojo2.setName("string");
////        pojo2.setPhoneNumber("string");
//
//
//		if(userService != null && pojo.getUserId() !=null)
//		    //ogPojo :: LogRequest(id=null, action=MODIFY, jsonRequest=null, jsonResponse={"timestamp":"Mon Jul 04 16:38:44 WAT 2022","message":"retrieved successfully","status":true,"data":{"id":"cac0c592-de2b-436a-96b5-9b1bd2ec4faa","email":"agbe.terseer@gmail.com","firstName":"Terseer","surname":"Agbe","dateOfBirth":"2020-01-01","gender":"MALE","phoneNumber":"2347030355396","userId":"2","referenceCode":"BKiFiPJKzcfygvTq","smsAlertConfig":true,"corporate":true,"otherDetails":{"organisationName":"SlitSoft","organisationEmail":"agbe.terseer@gmail.com","organisationPhone":"2347030355396","organizationCity":"MAkurdi","organizationAddress":"LAGOS","organizationState":"BENUE","organisationType":"ICT","businessType":"BEANS"}}}, message=Authentication Service: /api/v1/profile/2, module=ProfileController, requestDate=2022-07-04T16:38:44.988065800, responseDate=2022-07-04T16:38:44.988065800, userId=2, email=agbe.terseer@gmail.com, name=TERSEER AGBE, phoneNumber=234703355396, location=null)
//            System.out.println("pojo :: " +pojo);
//			userService.saveLog(pojo);
//	}
//
//
//}
