package com.lowtuna.dropwizard.grpc;

import com.codahale.metrics.health.HealthCheck;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;

import java.util.Collection;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class CompositeHealthCheck extends HealthCheck {

    @Singular
    private final Collection<HealthCheck> healthChecks;

    @Override
    protected Result check() {
        long healthyChecks = healthChecks
                                     .stream()
                                     .map(HealthCheck::execute)
                                     .filter(Result::isHealthy)
                                     .count();
        return healthyChecks == healthChecks.size() ?
                       Result.healthy() :
                       Result.unhealthy((healthChecks.size() - healthyChecks )+ " of " + healthChecks.size() + " health checks failed");
    }
}
