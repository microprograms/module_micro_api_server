package module_micro_api_server.http_server.utils;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.microprograms.micro_api_runtime.exception.PassthroughException;

import module_micro_api_server.http_server.HttpRequestHandler.SimpleResponseCode;

public class RequestUtils {

	public static JSONObject parseRequest(HttpServletRequest request) throws Exception {
		String method = request.getMethod();
		if ("GET".equals(method)) {
			return _parseGetRequestUrlParams(request);
		} else if ("POST".equals(method)) {
			return _parsePostRequestBody(request);
		} else {
			throw new PassthroughException(
					new SimpleResponseCode("http_method_not_support", String.format("不支持的HTTP请求方法%s", method)));
		}
	}

	private static JSONObject _parseGetRequestUrlParams(HttpServletRequest request) throws IOException {
		String _raw = request.getParameter("_raw");
		if (StringUtils.isNotBlank(_raw)) {
			return JSON.parseObject(_raw);
		}
		JSONObject json = new JSONObject();
		Enumeration<String> en = request.getParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			json.put(name, request.getParameter(name));
		}
		return json;
	}

	private static JSONObject _parsePostRequestBody(HttpServletRequest request) throws IOException {
		try (ServletInputStream is = request.getInputStream()) {
			byte[] bs = new byte[request.getContentLength()];
			IOUtils.read(is, bs);
			return JSON.parseObject(new String(bs));
		}
	}
}
