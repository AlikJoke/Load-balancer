package ru.bpc.cm.servlet.cluster.balancing;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import jersey.repackaged.com.google.common.base.Function;
import jersey.repackaged.com.google.common.base.Predicate;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;
import ru.bpc.cm.servlet.cluster.properties.ClusterPropertiesFields;
import ru.bpc.cm.servlet.cluster.properties.PropertyHolder;
import ru.bpc.cm.servlet.cluster.properties.PropertyHolderImpl;

public class ClusterInfo implements IClusterInfo {

	private PropertyHolder props;

	private static ConcurrentMap<Integer, ClusterNode> nodes;

	private static List<String> requests;

	static {
		requests = Lists.newArrayList();
	}

	private static ClusterInfo instance;

	private ClusterInfo() {
		this.props = new PropertyHolderImpl();
		this.initContext();
	}

	public static ClusterInfo getClusterInfo() {
		if (instance == null)
			instance = new ClusterInfo();
		return instance;
	}

	private final Function<List<String>, String> fromListToString = new Function<List<String>, String>() {
		@Override
		public String apply(final List<String> input) {
			StringBuilder sb = new StringBuilder();
			for (String param : input)
				sb.append(";").append(param);
			return sb.toString();
		}
	};

	private boolean addRequest(final HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		List<String> paramNames = Lists.transform(Collections.list(request.getParameterNames()),
				new Function<String, String>() {

					@Override
					public String apply(final String input) {
						return input + ":" + request.getParameterValues(input);
					}

				});
		String key = request.getRemoteUser() + request.getRequestURL() + request.getContentType()
				+ request.getRemoteAddr() + request.getRequestedSessionId() + fromListToString.apply(paramNames);
		boolean isContains = !requests.contains(key) ? requests.add(key) : false;
		try {
			return isContains;
		} finally {
			if (!isContains)
				requests.remove(key);
		}
	}

	private void initContext() {
		String cnt = props.getProperty(ClusterPropertiesFields.COUNT);
		if (!StringUtils.hasLength(cnt))
			return;

		int count = Integer.parseInt(cnt);
		for (int i = 1; i < count + 1; i++) {
			final String hostName = props.getProperty(ClusterPropertiesFields.HOST + i);
			final String address = props.getProperty(ClusterPropertiesFields.ADDRESS + i);
			final int port = Integer.parseInt(props.getProperty(ClusterPropertiesFields.PORT + i));
			final double coeff = Double.parseDouble(props.getProperty(ClusterPropertiesFields.COEFF + i));
			this.initialize(hostName, address, port, coeff);
		}

	}

	private ClusterNode initialize(String hostName, String address, int port, double coeff) {
		int hash = port + hostName.hashCode();
		ClusterNode node = this.nodes().get(hash);
		if (node == null)
			node = new ClusterNode(hostName, address, port, coeff);
		this.nodes().putIfAbsent(hash, node);
		return node;
	}

	@Override
	public ClusterNode initialize(ServletRequest request, ServletResponse response) {
		int hash = request.getServerPort() + request.getServerName().hashCode();
		ClusterNode node = this.nodes().get(hash);
		if (node == null)
			node = new ClusterNode(request.getLocalName(), request.getServerName(), request.getServerPort(), 1);
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
		final double count = node.getRequestCounter() * node.getCoeff();
		if (Maps.filterValues(this.nodes(), new Predicate<ClusterNode>() {
			@Override
			public boolean apply(ClusterNode node) {
				return (node.getRequestCounter() + 1) * node.getCoeff() < count;
			}
		}).size() > 0)
			return true;
		return false;
	}

	public static String computeURL(ClusterNode node, ServletRequest request) {
		if (node == null)
			throw new RuntimeException("Node can't be null");
		
		while (true) {
			List<ClusterNode> sortedNodes = Lists.newArrayList(getClusterInfo().nodes().values());
			Collections.sort(sortedNodes, new Comparator<ClusterNode>() {

				@Override
				public int compare(ClusterNode arg0, ClusterNode arg1) {
					if (arg0.getRequestCounter() * arg0.getCoeff() == arg1.getRequestCounter() * arg1.getCoeff())
						return 0;
					else if (arg0.getRequestCounter() * arg0.getCoeff() < arg1.getRequestCounter() * arg1.getCoeff())
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
			if (!isAlive(sb.toString())) {
				getClusterInfo().nodes().remove(node.getPort() + node.getHostName().hashCode());
				continue;
			}
			sb.append(requestURI);
			return sb.toString();
		}
	}

	private static boolean isAlive(String serverAddr) {
		try {
			URL url = new URL(serverAddr + "/SVCM");
			url.openConnection();
			return true;
		} catch (IOException e) {
			// ignore, server is dead
		}
		return false;
	}
}
