package org.ehoffman.testing.fixture.services;

import org.ehoffman.module.Module;

public interface HotSwappableProxy {
  public <T> Module<T> setProxyTargetModule(Module<T> o);
  public Object getUnwrappedService();
}
