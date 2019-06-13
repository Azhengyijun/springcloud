package com.cloud.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务网关还有个作用就是接口的安全性校验，
 * 需要通过 gateway 进行统一拦截。
 * Gateway 提供了多种 Filter 可供选择，如 GatewayFilter、GlobalFilter 等，不同的 Filter 的作用是不一样的，
 * GatewayFilter 处理单个路由的请求，而 GlobalFilter 根据名字大致就能知道其作用，
 * 它是一个全局 Filter，可以过滤所有路由请求
 */
@Component
public class ApiGlobalFilter implements GlobalFilter {

    @Override
    /**
     * 通过 Mono 返回数据到前端
     * 判断 token 是否存在，如果不存在则返回鉴权失败
     */
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        if (StringUtils.isBlank(token)) {
            ServerHttpResponse response = exchange.getResponse();
            Map<String,Object> message = new HashMap<>();
            message.put("status", -1);
            message.put("data", "鉴权失败");
            byte[] bits = message.toString().getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Content-Type", "text/json;charset=UTF-8");
            return response.writeWith(Mono.just(buffer));
        }
        return chain.filter(exchange);
    }
}
