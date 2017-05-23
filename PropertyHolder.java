package ru.bpc.cm.servlet.cluster.properties;

/**
 * ��������� �������� � ��������. ���� �������� ���������� � ������, ������� �
 * ������ ������ ��������.
 * 
 * @author Alimurad A. Ramazanov
 * @since 23.05.2017
 * @version 1.0.0
 *
 */
public interface PropertyHolder {

	/**
	 * ��������� ������ �� ������� �� ����� �� �����
	 * classpath://ru/bpc/cm/cluster/cluster.properties.
	 * <p>
	 * 
	 * @param name
	 *            - ��� ��������; �� ����� ���� {@code null}.
	 * @return �������� ��������. ���� �� ������� � �����, �� {@code null}.
	 */
	String getProperty(String name);
}
