package ru.bpc.cm.servlet.cluster.singleton.implementations;

import java.io.Serializable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ru.bpc.cm.servlet.cluster.singleton.EJBSingletonService;
import weblogic.cluster.singleton.SingletonService;

/**
 * Реализация интерфейса {@link EJBSingletonService} для использования в Oracle
 * WebLogic.
 * 
 * @author Alimurad A. Ramazanov
 * @since 27.05.2017
 * @version 1.0.0
 *
 */
public class WebLogicEJBSingletonService implements EJBSingletonService, Serializable, SingletonService {

	private static final long serialVersionUID = 1909849089292733072L;

	private static final String serviceClassName = "WebLogicEJBSingletonService";

	@Override
	public void activate() {
		Context ic = null;
		try {
			ic = new InitialContext();
			ic.bind(serviceClassName, this);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} finally {
			if (ic != null)
				try {
					ic.close();
				} catch (NamingException e) {
					throw new RuntimeException(e);
				}
		}
	}

	@Override
	public void deactivate() {
		Context ic = null;
		try {
			ic = new InitialContext();
			ic.unbind(serviceClassName);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

}
