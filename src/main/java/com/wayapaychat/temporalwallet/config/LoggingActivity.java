package com.wayapaychat.temporalwallet.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wayapaychat.temporalwallet.SpringApplicationContext;
import com.wayapaychat.temporalwallet.pojo.LogMessage;
import com.wayapaychat.temporalwallet.pojo.LogRequest;
import com.wayapaychat.temporalwallet.security.AuthenticatedUserFacade;
import com.wayapaychat.temporalwallet.service.LogService;
import com.wayapaychat.temporalwallet.util.ReqIPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

@Slf4j
@Component
public class LoggingActivity extends DispatcherServlet {

    @Autowired
    ReqIPUtils reqUtil;

    @Autowired
    AuthenticatedUserFacade authenticatedUserFacade;

    @Override
    public void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {

        long startTime = System.currentTimeMillis();
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }
        HandlerExecutionChain handler = getHandler(request);
        try {
            super.doDispatch(request, response);
        } finally {
            log(request, response, handler, System.currentTimeMillis() - startTime);
            updateResponse(response);
        }
    }

    private void log(HttpServletRequest request, HttpServletResponse response, HandlerExecutionChain handler,
                     long timeTaken) {
        System.out.println("response " + response);
        System.out.println("request " + request);
        final String path = request.getRequestURI();
        if(path.startsWith("/swagger") || path.startsWith("/v2/api-docs")
                || path.startsWith("/api/v1/auth/validate-user"))
            return;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		int status = response.getStatus();
//		JsonObject jsonObject = new JsonObject();
//		jsonObject.addProperty("httpStatus", status);
//		jsonObject.addProperty("path", request.getRequestURI());
//		jsonObject.addProperty("httpMethod", request.getMethod());
//		jsonObject.addProperty("timeTakenMs", timeTaken);
//		jsonObject.addProperty("clientIP", reqUtil.getClientIP(request));
//		jsonObject.addProperty("javaMethod", handler.toString());
//		//jsonObject.addProperty("session", request.getSession().getId());
//		jsonObject.addProperty("response", getResponsePayload(response));
//
//		if (status > 299) {
//			String requestData = null;
//			try {
//				jsonObject.addProperty("request", request.getReader().readLine());
//				//requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//				requestData = getRequestData(request);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			jsonObject.addProperty("requestBody", requestData);
//			jsonObject.addProperty("requestParams", request.getQueryString());
//
//
//		}
        LogMessage logMessage = new LogMessage();
        logMessage.setHttpStatus(response.getStatus());
        logMessage.setHttpMethod(request.getMethod());
        logMessage.setClientIP(reqUtil.getClientIP(request));
        logMessage.setTimeTakenMs(timeTaken);
        logMessage.setPath(path);
        logMessage.setResponse(getResponsePayload(response));
        logMessage.setJavaMethod(handler == null ? "null" : handler.getHandler().toString());
        logMessage.setRequestParams(request.getQueryString());

        String requestData = null;
        try {
            requestData = getRequestData(request);
        } catch (IOException e) {
            log.error("An error Occurred in reading request Input :: {}", e.getMessage());
        }
        logMessage.setRequestBody(Objects.toString(requestData, "null"));
        String json = gson.toJson(logMessage);
        log.info(json);

//        Authentication authentication = authenticatedUserFacade.getAuthentication();
//        System.out.println( " Logger = " + authentication);
		logRequestAndResponse(logMessage, 1L);

//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		if(authentication != null && authentication.getPrincipal() instanceof UserPrincipal){
//			UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
//			principal.getUser().ifPresent(user ->
//					CompletableFuture.runAsync(() ->
//							logRequestAndResponse(logMessage, user.getId())));
//			Optional<Users> userObj = principal.getUser();
//
//			if (userObj.isPresent()){
//				this.user = userObj.get();
//			}
//		}

    }


    private String getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response,
                ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, 5120);
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    log.error("Error Occurred in Encoding Response Body: {}", ex.getMessage());
                } catch (Exception ex) {
                    log.error("Error Occurred in Encoding Response Body: {}", ex.getMessage());
                }
            }
        }
        return "[unknown]";
    }


    private String getRequestData(final HttpServletRequest request) throws UnsupportedEncodingException {
        String payload = null;
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
            }
        }
        return payload;
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response,
                ContentCachingResponseWrapper.class);
        assert responseWrapper != null;
        responseWrapper.copyBodyToResponse();
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
        if(userService != null)
            userService.saveLog(pojo);
    }

}
