package co.wrisk.dropwizard.logging.elasticsearch;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.internetitem.logback.elasticsearch.AbstractElasticsearchAppender;
import com.internetitem.logback.elasticsearch.ElasticsearchAccessAppender;
import com.internetitem.logback.elasticsearch.ElasticsearchAppender;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import io.dropwizard.request.logging.layout.LogbackAccessRequestLayoutFactory;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;

/**
 * <p>An {@link io.dropwizard.logging.AppenderFactory} implementation which provides an appender that writes events to Elastic Search.</p>
 * <b>Configuration Parameters:</b>
 * <table summary="Configuration">
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code type}</td>
 * <td><b>REQUIRED</b></td>
 * <td>The appender type. Must be {@code elasticsearch}.</td>
 * </tr>
 * <tr>
 * <td>{@code threshold}</td>
 * <td>{@code ALL}</td>
 * <td>The lowest level of events to write to the server.</td>
 * </tr>
 * </table>
 *
 * @see io.dropwizard.logging.AbstractAppenderFactory
 */
@JsonTypeName("elasticsearch")
public class ElasticsearchAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {


    @NotNull
    @JsonProperty
    private String url;

    @NotNull
    @JsonProperty
    private String index;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @SuppressWarnings("unchecked")
    private AbstractElasticsearchAppender<E> elasticSearchAppender(LayoutFactory<E> layoutFactory) {
        AbstractElasticsearchAppender elasticsearchAppender;
        if (layoutFactory instanceof LogbackAccessRequestLayoutFactory) {
            elasticsearchAppender = new ElasticsearchAccessAppender();
        } else {
            elasticsearchAppender = new ElasticsearchAppender();
        }
        return elasticsearchAppender;
    }

    @Override
    public Appender<E> build(LoggerContext context, String applicationName, LayoutFactory<E> layoutFactory,
                             LevelFilterFactory<E> levelFilterFactory, AsyncAppenderFactory<E> asyncAppenderFactory) {
        final AbstractElasticsearchAppender<E> appender = elasticSearchAppender(layoutFactory);


        appender.setName("elasticsearch-appender");
        appender.setContext(context);
        setUrl(appender);
        appender.setIndex(index);
        appender.addFilter(levelFilterFactory.build(threshold));
        appender.start();

        return appender;
    }

    private void setUrl(AbstractElasticsearchAppender<E> appender) {
        try {
            appender.setUrl(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
