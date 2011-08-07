package org.ehoffman.logback.capture;

import static ch.qos.logback.classic.Level.ALL;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/** Temporarily captures Logback output (mostly useful for tests). */
public class LogbackCapture {

  /**
   * Start capturing.
   * defaults to the root logger
   * defaults to INFO level
   * defaults to "[%p] %m%n" layout pattern
   */
  public static void start() {
    if (INSTANCE.get() != null) {
      throw new IllegalStateException("already started");
    }
    INSTANCE.set(new LogbackCapture("", Level.INFO, "[%p] %c.%M %m\n"));
  }

  /**
   * Start capturing.
   * @param loggerName if null, defaults to the root logger
   * @param level if null, defaults to all levels
   * @param layoutPattern if null, defaults to "[%p] %m%n"
   */
  public static void start(String loggerName, Level level, String layoutPattern) {
    if (INSTANCE.get() != null) {
      throw new IllegalStateException("already started");
    }
    INSTANCE.set(new LogbackCapture(loggerName, level, layoutPattern));
  }

  /** Stop capturing and return the logs. */
  public static String stop() {
    LogbackCapture instance = INSTANCE.get();
    if (instance == null) {
      throw new IllegalStateException("was not running");
    }
    final String result = instance.stopInstance();
    INSTANCE.remove();
    return result;
  }

  private static final ThreadLocal<LogbackCapture> INSTANCE = new ThreadLocal<LogbackCapture>();

  private final Logger logger;
  private final OutputStreamAppender<ILoggingEvent> appender;
  private final Encoder<ILoggingEvent> encoder;
  private final ByteArrayOutputStream logs;

  private LogbackCapture(String loggerName, Level level, String layoutPattern) {
    logs = new ByteArrayOutputStream(4096);
    encoder = buildEncoder(layoutPattern);
    appender = buildAppender(encoder, logs);
    logger = getLogbackLogger(loggerName, level);
    logger.addAppender(appender);
  }

  private String stopInstance() {
    appender.stop();
    //try {
    String out = logs.toString();
    logs.reset();
    return out;
    //} catch (final UnsupportedEncodingException cantHappen) {
    // return null;
    //}
  }

  private static Logger getLogbackLogger(String name, Level level) {
    if (name == null || "".equals(name)) {
      name = ROOT_LOGGER_NAME;
    }
    if (level == null) {
      level = ALL;
    }

    //resets logger context
    LoggerContext loggerContext =
        (LoggerContext) LoggerFactory.getILoggerFactory();
    if (!loggerContext.isStarted()){
      loggerContext.start();
    }
    //loggerContext.stop();

    loggerContext.getLogger(name);

    Logger logger = ContextSelectorStaticBinder.getSingleton()
        .getContextSelector().getDefaultLoggerContext().getLogger(name);
    logger.setLevel(level);
    return logger;
  }

  private static Encoder<ILoggingEvent> buildEncoder(String layoutPattern) {
    if (layoutPattern == null) {
      layoutPattern = "[%p] %c.%M %m\n";
    }
    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setPattern(layoutPattern);
    //encoder.setCharset(Charset.forName("UTF-16"));
    if (ContextSelectorStaticBinder.getSingleton() != null &&
        ContextSelectorStaticBinder.getSingleton().getContextSelector() != null){
      encoder.setContext(ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext());
    }
    encoder.start();
    return encoder;
  }

  private static OutputStreamAppender<ILoggingEvent> buildAppender(final Encoder<ILoggingEvent> encoder,
      final OutputStream outputStream) {
    OutputStreamAppender<ILoggingEvent> appender = new OutputStreamAppender<ILoggingEvent>();
    appender.setName("logcapture");
    if (ContextSelectorStaticBinder.getSingleton() != null &&
        ContextSelectorStaticBinder.getSingleton().getContextSelector() != null){
      appender.setContext(ContextSelectorStaticBinder.getSingleton().getContextSelector().getDefaultLoggerContext());
    }
    appender.setEncoder(encoder);
    appender.setOutputStream(outputStream);
    appender.addFilter( new Filter<ILoggingEvent>() {
      long thread = Thread.currentThread().getId();
      @Override
      public FilterReply decide(ILoggingEvent event) {
        return (thread==Thread.currentThread().getId())?FilterReply.ACCEPT:FilterReply.DENY;
      }
    });
    appender.start();
    return appender;
  }
}