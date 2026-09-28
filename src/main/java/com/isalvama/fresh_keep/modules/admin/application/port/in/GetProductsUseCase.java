package com.isalvama.fresh_keep.modules.admin.application.port.in;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductMetricResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductTypeCountResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductDetailResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductResult;

import java.util.List;
import java.util.UUID;

public interface GetProductsUseCase {

    List<ProductMetricResult> getProductMetrics (GetProductMetricsCommand command);

    List<ProductResult> getProducts(GetProductsCommand command);

    ProductDetailResult getProduct(UUID productId);

    List<ProductTypeCountResult> getProductTypes();
}
