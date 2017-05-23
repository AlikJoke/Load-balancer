package ru.bpc.cm.servlet.cluster.properties;

/**
 * Настройки серверов в кластере. Файл содержит информацию о портах, адресах и
 * именах хостов серверов.
 * 
 * @author Alimurad A. Ramazanov
 * @since 23.05.2017
 * @version 1.0.0
 *
 */
public interface PropertyHolder {

	/**
	 * Получение одного из свойств по имени из файла
	 * classpath://ru/bpc/cm/cluster/cluster.properties.
	 * <p>
	 * 
	 * @param name
	 *            - имя свойства; не может быть {@code null}.
	 * @return значение свойства. Если не найдено в файле, то {@code null}.
	 */
	String getProperty(String name);
}
