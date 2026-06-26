package com.vifarmer.ads.lib.iap;

public class ProductDetailCustom {
    private String productId;
    private String productType;
    private boolean isConsumable;

    public ProductDetailCustom(String productId, String productType) {
        this.productId = productId;
        this.productType = productType;
        this.isConsumable = false;
    }

    public ProductDetailCustom(String productId, String productType, boolean isConsumable) {
        this.productId = productId;
        this.productType = productType;
        this.isConsumable = isConsumable;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public boolean isConsumable() {
        return isConsumable;
    }

    public void setConsumable(boolean consumable) {
        isConsumable = consumable;
    }
}
