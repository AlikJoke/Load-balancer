package ru.project.balancer.cluster;

import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Интерфейс, аккумулирующий в себе методы для работы с узлами в кластере для
 * балансировки нагрузки
 * 
 * @author Alimurad A. Ramazanov
 * @since 03.12.2016
 * @version 1.0.1
 */
public interface IClusterInfo {

	/**
	 * Возвращает синхронизированную карту узлов в кластере, где ключами
	 * является хеш от номера порта, имени хоста и адреса
	 * <p>
	 * 
	 * @see {@link ClusterNode}
	 * @return не может быть <code>null</code>
	 */
	ConcurrentMap<Integer, ClusterNode> nodes();

	/**
	 * Возвращает количество узлов в кластере
	 * <p>
	 * 
	 * @return не может быть <code>null</code>
	 */
	Integer getNumberNodes();

	/**
	 * Признак, надо ли перенаправить запрос на другую "ноду"
	 * <p>
	 * 
	 * @param node
	 *            - узел в кластере, для которого производится проверка 
	 *        request
	 *            - запрос
	 * @see {@link ClusterNode, HttpServletRequest}
	 * @return <code>true<code> - если надо | <code>false</code> - иначе
	 */
	boolean needRedirect(ClusterNode node, HttpServletRequest request);

	/**
	 * Инициализирует узел кластера (если он имеется): в случае, если в карте
	 * нод уже есть данная нода, то вернет ее
	 * <p>
	 * 
	 * @see {@link ServletRequest}, {@link ServletResponse}
	 * @param request
	 *            - клиентский запрос
	 * @param response
	 *            - ответ сервера
	 * @return не может быть <code>null</code>
	 */
	ClusterNode initialize(ServletRequest request, ServletResponse response);
}
