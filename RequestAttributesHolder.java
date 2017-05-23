package ru.bpc.cm.servlet.cluster.wrapper;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import jersey.repackaged.com.google.common.collect.Maps;

public class RequestAttributesHolder {

	private static ConcurrentMap<String, Map<String, String[]>> attributes;

	static {
		attributes = Maps.newConcurrentMap();
	}

	public static Map<String, String[]> getHoldedParameters(String sessionId) {
		return attributes.get(sessionId);
	}

	@SuppressWarnings("unchecked")
	public static void holdRequest(HttpServletRequest request) {
		attributes.put(request.getRequestedSessionId(), request.getParameterMap());
	}

	public static boolean containsRequest(String sessionId) {
		return attributes.containsKey(sessionId);
	}

	public static void removeRequest(String sessionId) {
		attributes.remove(sessionId);
	}
}
