package com.diamondguide.redeemcode.ffftips;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.diamondguide.redeemcode.ffftips.databinding.ActivityIapactivityBinding;
import com.vifarmer.ads.lib.iap.IAPManager;
import com.vifarmer.ads.lib.iap.PurchaseCallback;

public class IAPActivity extends AppCompatActivity {
    private static final String TAG = "IAPActivityTag";
    private ActivityIapactivityBinding binding;

    private int totalCoins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initBilling();

        binding.btnItem1.setOnClickListener(v -> {
            IAPManager.getInstance().purchase(IAPActivity.this, "coin_pack_100");
        });

        binding.btnItem2.setOnClickListener(v -> {
            IAPManager.getInstance().purchase(IAPActivity.this, "coin_pack_500");
        });

        binding.btnSubRemoveAds.setOnClickListener(v -> {
            IAPManager.getInstance().purchase(IAPActivity.this, IAPManager.PRODUCT_ID_TEST);
        });
    }

    private void updateCoins(int amount) {
        totalCoins += amount;
        binding.tvCoins.setText(totalCoins + " Coins");
    }

    private void initView() {
        EdgeToEdge.enable(this);
        binding = ActivityIapactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initBilling() {
        IAPManager.getInstance().setPurchaseListener(new PurchaseCallback() {
            @Override
            public void onProductPurchased(String productId, String transactionDetails) {
                super.onProductPurchased(productId, transactionDetails);
                Log.d(TAG, "onProductPurchased:\nproductId: " + productId + "\ntransactionDetails: " + transactionDetails);

                if ("coin_pack_100".equals(productId)) {
                    updateCoins(100);
                } else if ("coin_pack_500".equals(productId)) {
                    updateCoins(500);
                } else if (IAPManager.PRODUCT_ID_TEST.equals(productId)) {

                }
            }

            @Override
            public void onUserCancelBilling() {
                super.onUserCancelBilling();
                Log.d(TAG, "onUserCancelBilling: ");
            }
        });
        /*binding.tvWeeklyPrice.setText(
            IAPManager.getInstance().getPriceSub(Constants.IAPKeys.id_weekly));
        binding.tvYearlyPrice.setText(
            IAPManager.getInstance().getPriceSub(Constants.IAPKeys.id_yearly));
        binding.tvLifetimePrice.setText(
            IAPManager.getInstance().getPrice(Constants.IAPKeys.id_life_time));*/
    }
}
