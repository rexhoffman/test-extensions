package org.ehoffman.module;

import org.apache.commons.pool.PoolableObjectFactory;

public interface PooledModule<T> extends Module<T>, PoolableObjectFactory {
  public int getMaxPoolElements();
}
