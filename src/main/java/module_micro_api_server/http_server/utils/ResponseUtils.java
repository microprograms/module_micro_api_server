package module_micro_api_server.http_server.utils;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.microprograms.micro_api_runtime.enums.ReserveResponseCodeEnum;
import com.github.microprograms.micro_api_runtime.model.ResponseCode;

public class ResponseUtils {

	public static void errorResponse(ResponseCode responseCode, Throwable cause, HttpServletResponse response)
			throws IOException {
		JSONObject apiResponse = new JSONObject();
		apiResponse.put("code", responseCode.getCode());
		apiResponse.put("message", responseCode.getMessage());
		if (cause != null) {
			apiResponse.put("stackTrace", ExceptionUtils.getStackTrace(cause));
		}
		rawResponse(apiResponse, response);
	}

	public static void successResponse(String rawApiResponse, HttpServletResponse response) throws IOException {
		successResponse(JSON.parseObject(rawApiResponse), response);
	}

	public static void successResponse(JSONObject apiResponse, HttpServletResponse response) throws IOException {
		apiResponse.put("code", ReserveResponseCodeEnum.success.getCode());
		apiResponse.put("message", ReserveResponseCodeEnum.success.getMessage());
		rawResponse(apiResponse, response);
	}

	public static void rawResponse(JSONObject rawResponse, HttpServletResponse response) throws IOException {
		try (PrintWriter writer = response.getWriter()) {
			writer.println(rawResponse.toJSONString());
		}
	}
}
