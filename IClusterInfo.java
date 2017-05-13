package ru.project.balancer.cluster;

import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * ���������, �������������� � ���� ������ ��� ������ � ������ � �������� ���
 * ������������ ��������
 * 
 * @author Alimurad A. Ramazanov
 * @since 03.12.2016
 * @version 1.0.1
 */
public interface IClusterInfo {

	/**
	 * ���������� ������������������ ����� ����� � ��������, ��� �������
	 * �������� ��� �� ������ �����, ����� ����� � ������
	 * <p>
	 * 
	 * @see {@link ClusterNode}
	 * @return �� ����� ���� <code>null</code>
	 */
	ConcurrentMap<Integer, ClusterNode> nodes();

	/**
	 * ���������� ���������� ����� � ��������
	 * <p>
	 * 
	 * @return �� ����� ���� <code>null</code>
	 */
	Integer getNumberNodes();

	/**
	 * �������, ���� �� ������������� ������ �� ������ "����"
	 * <p>
	 * 
	 * @param node
	 *            - ���� � ��������, ��� �������� ������������ �������� 
	 *        request
	 *            - ������
	 * @see {@link ClusterNode, HttpServletRequest}
	 * @return <code>true<code> - ���� ���� | <code>false</code> - �����
	 */
	boolean needRedirect(ClusterNode node, HttpServletRequest request);

	/**
	 * �������������� ���� �������� (���� �� �������): � ������, ���� � �����
	 * ��� ��� ���� ������ ����, �� ������ ��
	 * <p>
	 * 
	 * @see {@link ServletRequest}, {@link ServletResponse}
	 * @param request
	 *            - ���������� ������
	 * @param response
	 *            - ����� �������
	 * @return �� ����� ���� <code>null</code>
	 */
	ClusterNode initialize(ServletRequest request, ServletResponse response);
}
