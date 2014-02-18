package at.tomtasche.inspectroid;

import java.util.Map.Entry;

import com.mba.proxylight.Request;

public class RequestHelper {

	private static final String CRLF = "\r\n";

	public static String dumpRequest(Request request) {
		StringBuffer send = new StringBuffer(request.getMethod()).append(" ");
		String url = request.getUrl();
		if (!url.startsWith("/")) {
			url = url.substring(url.indexOf('/', 8));
		}
		send.append(url).append(" ").append(request.getProtocol()).append(CRLF);
		for (Entry<String, String> h : request.getHeaders().entrySet()) {
			send.append(h.getKey()).append(": ").append(h.getValue())
					.append(CRLF);
		}
		send.append(CRLF);

		return send.toString();
	}
}
