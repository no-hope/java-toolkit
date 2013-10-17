package org.nohope.jaxb2.plugin;

import org.apache.maven.plugin.logging.Log;
import org.nohope.logging.Logger;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
* @since 2013-10-17 11:55
*/
public class Slf4jToMavenLogAdapter implements Log {
    private final Logger log;

    public Slf4jToMavenLogAdapter(final Logger log) {
        this.log = log;
    }

    @Override
    public boolean isDebugEnabled() {
        return this.log.isDebugEnabled();
    }

    @Override
    public void debug(final CharSequence content) {
        this.log.debug(content.toString());
    }

    @Override
    public void debug(final CharSequence content, final Throwable error) {
        this.log.debug(error, content.toString());
    }

    @Override
    public void debug(final Throwable error) {
        this.log.debug(error);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.log.isInfoEnabled();
    }

    @Override
    public void info(final CharSequence content) {
        this.log.info(content.toString());
    }

    @Override
    public void info(final CharSequence content, final Throwable error) {
        this.log.info(error, content.toString());
    }

    @Override
    public void info(final Throwable error) {
        this.log.info(error);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.log.isWarnEnabled();
    }

    @Override
    public void warn(final CharSequence content) {
        this.log.warn(content.toString());
    }

    @Override
    public void warn(final CharSequence content, final Throwable error) {
        this.log.warn(error, content.toString());
    }

    @Override
    public void warn(final Throwable error) {
        this.log.warn(error);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.log.isErrorEnabled();
    }

    @Override
    public void error(final CharSequence content) {
        this.log.error(content.toString());
    }

    @Override
    public void error(final CharSequence content, final Throwable error) {
        this.log.error(error, content.toString());
    }

    @Override
    public void error(final Throwable error) {
        this.log.error(error);
    }
}
