package ru.bpc.cm.servlet.cluster.properties;

import java.io.InputStream;
import java.util.Properties;

import org.springframework.util.StringUtils;

public class PropertyHolderImpl implements PropertyHolder {

	private Properties properties;

	private void init() {
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("ru/bpc/cluster/cluster.properties");
		properties = new Properties();
		if (inputStream == null)
			return;
		
		try {
			properties.load(inputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public PropertyHolderImpl() {
		this.init();
	}

	@Override
	public String getProperty(String name) {
		if (!StringUtils.hasLength(name))
			throw new IllegalArgumentException("Property name can't be empty or null");
		return properties.getProperty(name);
	}
}
