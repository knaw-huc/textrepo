package nl.knaw.huc.service.health;

import static java.lang.String.format;

import com.codahale.metrics.health.HealthCheck;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

public class IndexerHealthCheck extends HealthCheck {

  private final String mapping;
  private final JerseyClient client;

  public IndexerHealthCheck(String mapping) {
    this.client = JerseyClientBuilder.createClient();
    this.mapping = mapping;
  }

  @Override
  protected Result check() {
    Response response;
    try {
      response = this.client.target(this.mapping).request().get();
    } catch (Exception ex) {
      return HealthCheck.Result.unhealthy(format(
          "Http status of mapping endpoint: unknown; reason: %s: %s",
          ex.getClass().getName(), ex.getMessage()
      ));
    }
    if (response.getStatus() == 200) {
      return Result.healthy("Http status of mapping endpoint: 200");
    }
    return Result.unhealthy(format(
        "Http status of mapping endpoint: %d; reason: %s",
        response.getStatus(), response.readEntity(String.class)
    ));
  }

}
