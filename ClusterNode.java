package ru.bpc.cm.servlet.cluster.balancing;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.bpc.cm.servlet.cluster.wrapper.RequestAttributesHolder;

public class ClusterNode {

	private int port;
	private String hostName;
	private String address;
	private long requested;
	private double coeff;

	public static ClusterNode getNode() {
		return new ClusterNode();
	}

	ClusterNode() {
		this.port = 8080;
		this.hostName = "localhost";
		this.address = "127.0.0.1";
		this.requested = 0;
		this.coeff = 1;
	}

	public ClusterNode(String hostName, String address, int port, double coeff) {
		this.port = port;
		this.hostName = hostName;
		this.address = address;
		this.requested = 0;
		this.coeff = coeff;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return this.port;
	}

	public String getHostName() {
		return this.hostName;
	}

	public String getAddress() {
		return this.address;
	}
	
	public double getCoeff() {
		return this.coeff;
	}

	public void plus() {
		this.requested++;
	}

	public void minus() {
		this.requested--;
	}

	public long getRequestCounter() {
		return this.requested;
	}

	public void doRedirect(ServletRequest request, ServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (ClusterInfo.getClusterInfo().needRedirect(this, (HttpServletRequest) request)) {
			RequestAttributesHolder.holdRequest((HttpServletRequest) request);
			((HttpServletResponse) response).sendRedirect(ClusterInfo.computeURL(this, request));
		} else
			chain.doFilter(request, response);
	}
}
