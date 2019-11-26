package module_micro_api_server.http_server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.github.microprograms.micro_api_runtime.enums.ReserveResponseCodeEnum;
import com.github.microprograms.micro_api_runtime.exception.ApiNotAvailableException;
import com.github.microprograms.micro_api_runtime.exception.ModuleNotAvailableException;
import com.github.microprograms.micro_api_runtime.exception.PassthroughException;
import com.github.microprograms.micro_api_runtime.model.ResponseCode;

import module_micro_api_server.http_server.utils.RequestUtils;
import module_micro_api_server.http_server.utils.ResponseUtils;

public class HttpRequestHandler extends AbstractHandler {
	private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

	private BundleContext context;

	public HttpRequestHandler(BundleContext context) {
		this.context = context;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException, ServletException {
		try {
			// 设置跨域响应头
			httpResponse.setContentType("application/json;charset=UTF-8");
			httpResponse.setHeader("Access-Control-Allow-Origin", "*");
			httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
			httpResponse.setHeader("Access-Control-Max-Age", "3600");
			httpResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with, Content-Type");
			httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			// 解析请求
			JSONObject rawRequest = RequestUtils.parseRequest(httpRequest);
			String apiName = rawRequest.getString("apiName");
			if (StringUtils.isBlank(apiName)) {
				ResponseCode responseCode = ReserveResponseCodeEnum.missing_required_parameters_exception;
				throw new PassthroughException(responseCode,
						String.format("%s %s", responseCode.getMessage(), "apiName"));
			}

			String response = executeApi(apiName, rawRequest);
			ResponseUtils.successResponse(response, httpResponse);
		} catch (JSONException e) {
			ResponseUtils.errorResponse(ReserveResponseCodeEnum.request_data_cannot_be_resolved_exception, e,
					httpResponse);
		} catch (ModuleNotAvailableException e) {
			ResponseUtils.errorResponse(ReserveResponseCodeEnum.module_not_available_exception, e, httpResponse);
		} catch (ApiNotAvailableException e) {
			ResponseUtils.errorResponse(ReserveResponseCodeEnum.api_not_available_exception, e, httpResponse);
		} catch (PassthroughException e) {
			ResponseUtils.errorResponse(e.getResponseCode(), e.getCause(), httpResponse);
		} catch (Throwable e) {
			log.error("unknown exception", e);
			ResponseUtils.errorResponse(ReserveResponseCodeEnum.unknown_exception, e, httpResponse);
		}
	}

	private String executeApi(String apiName, JSONObject rawRequest)
			throws ApiNotAvailableException, PassthroughException {
		Object api = null;
		Method executeMethod = null;
		try {
			ServiceReference<?> reference = context.getServiceReference(apiName);
			api = context.getService(reference);
			executeMethod = api.getClass().getDeclaredMethod("execute", String.class);
		} catch (Exception e) {
			throw new ApiNotAvailableException(apiName);
		}

		try {
			return (String) executeMethod.invoke(api, rawRequest.toJSONString());
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new ApiNotAvailableException(apiName);
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (PassthroughException.class.getName().equals(target.getClass().getName())) {
				throw new PassthroughException(new SimpleResponseCode(target.getMessage()), target.getCause());
			} else {
				log.error("execute api unknown exception", target);
				throw new PassthroughException(ReserveResponseCodeEnum.unknown_exception, target);
			}
		}
	}

	public static class SimpleResponseCode implements ResponseCode {
		private static final String format = "(%s) %s";
		private static final String regex = "\\(([^()]+)\\)\\s+(.+)";

		private String code;
		private String message;

		public SimpleResponseCode(String format) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(format);
			if (matcher.find()) {
				this.code = matcher.group(1);
				this.message = matcher.group(2);
			}
		}

		public SimpleResponseCode(String code, String message) {
			this.code = code;
			this.message = message;
		}

		@Override
		public String getCode() {
			return code;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return String.format(format, code, message);
		}
	}
}
