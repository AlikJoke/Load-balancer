package ru.project.balancer.cluster.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;


public class ClusterInfo implements IClusterInfo {

	private static ConcurrentMap<Integer, ClusterNode> nodes;

	private static List<String> requests;

	private static ClusterInfo info;

	static {
		requests = Lists.newArrayList();
		info = new ClusterInfo();
	}

	public static ClusterInfo getClusterInfo() {
		return info;
	}

	private boolean addRequest(final HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		List<String> paramNames = (List<String>) enumerationAsStream(request.getParameterNames())
				.filter(param -> param != null).map(param -> param + ":" + request.getParameterValues(param.toString()))
				.collect(Collectors.toList());
		String key = request.getRemoteUser() + request.getRequestURL() + request.getContentType()
				+ request.getRemoteAddr() + request.getRequestedSessionId()
				+ StringUtils.collectionToDelimitedString(paramNames, ";");
		try {
			return !requests.contains(key) ? requests.add(key) : false;
		} finally {
			if (requests.contains(key))
				requests.remove(key);
		}
	}

	private static <T> Stream<T> enumerationAsStream(final Enumeration<T> e) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<T>() {
			public T next() {
				return e.nextElement();
			}

			public boolean hasNext() {
				return e.hasMoreElements();
			}

			public void forEachRemaining(Consumer<? super T> action) {
				while (e.hasMoreElements())
					action.accept(e.nextElement());
			}
		}, Spliterator.ORDERED), false);
	}

	@Override
	public ClusterNode initialize(ServletRequest request, ServletResponse response) {
		int hash = request.getServerPort() + request.getServerName().hashCode();
		ClusterNode node = Optional.<ClusterNode>fromNullable(this.nodes().get(hash))
				.or(new ClusterNode(request.getLocalName(), request.getServerName(), request.getServerPort()));
		node.plus();
		this.nodes().putIfAbsent(hash, node);
		return node;
	}

	@Override
	public ConcurrentMap<Integer, ClusterNode> nodes() {
		if (nodes == null)
			nodes = Maps.<Integer, ClusterNode>newConcurrentMap();
		return nodes;
	}

	@Override
	public Integer getNumberNodes() {
		return nodes().size();
	}

	@Override
	public boolean needRedirect(ClusterNode node, HttpServletRequest request) {
		if (this.getNumberNodes() < 2 || !addRequest(request))
			return false;
		final long count = node.getRequestCounter();
		return this.nodes().entrySet().stream().filter(node -> node.getValue().getRequestCounter() < count)
				.collect(Collectors.toList()).size() > 0;
	}

	public static String computeURL(ClusterNode node, ServletRequest request) {
		if (node == null)
			throw new RuntimeException("Node can't be null");
		List<ClusterNode> sortedNodes = Lists.newArrayList(nodes.values());
		Collections.sort(sortedNodes, new Comparator<ClusterNode>() {

			@Override
			public int compare(ClusterNode arg0, ClusterNode arg1) {
				if (arg0.getRequestCounter() == arg1.getRequestCounter())
					return 0;
				else if (arg0.getRequestCounter() < arg1.getRequestCounter())
					return -1;
				else
					return 1;
			}
		});

		StringBuilder sb = new StringBuilder();
		String requestURL = ((HttpServletRequest) request).getRequestURL().toString();
		String requestURI = ((HttpServletRequest) request).getRequestURI();

		sb.append(requestURL.substring(0, requestURL.indexOf("//") + 2));
		sb.append(sortedNodes.get(0).getAddress());
		sb.append(":");
		sb.append(sortedNodes.get(0).getPort());
		sb.append(requestURI);
		return sb.toString();
	}
}
