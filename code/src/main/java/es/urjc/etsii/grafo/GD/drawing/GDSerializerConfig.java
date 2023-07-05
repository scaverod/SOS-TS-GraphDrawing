package es.urjc.etsii.grafo.GD.drawing;

import es.urjc.etsii.grafo.io.serializers.AbstractSerializerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "serializers.dot")
public class GDSerializerConfig extends AbstractSerializerConfig {

}