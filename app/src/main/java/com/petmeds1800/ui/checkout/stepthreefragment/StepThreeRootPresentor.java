package com.petmeds1800.ui.checkout.stepthreefragment;

import com.petmeds1800.util.Log;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.model.PayPalCheckoutRequest;
import com.petmeds1800.model.entities.AddAddressResponse;
import com.petmeds1800.model.entities.CreditCardPaymentMethodRequest;
import com.petmeds1800.model.shoppingcart.response.ShoppingCartListResponse;
import com.petmeds1800.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Sdixit on 29-09-2016.
 */

public class StepThreeRootPresentor implements StepThreeRootContract.Presenter {

    private StepThreeRootContract.View mView;

    @Inject
    PetMedsApiService mPetMedsApiService;

    @Inject
    @Named(Constants.TAG_REDIRECT_OFF)
    PetMedsApiService mPetMedsApiServiceRedrirectOff;

    public StepThreeRootPresentor(StepThreeRootContract.View view) {
        mView = view;
        mView.setPresenter(this);
        PetMedsApplication.getAppComponent().inject(this);
    }


    @Override
    public void applyCreditCardPaymentMethod(CreditCardPaymentMethodRequest request) {
        mPetMedsApiService.applyCreditCardPaymentMethod(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ShoppingCartListResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        //error handling would be implemented once we get the details from backend team
                        mView.onError(e.getLocalizedMessage());

                    }

                    @Override
                    public void onNext(ShoppingCartListResponse shoppingCartListResponse) {
                        if (shoppingCartListResponse.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                            if (mView.isActive()) {
                                mView.onSuccessCreditCardPayment(shoppingCartListResponse);
                            }
                        } else {
                            if (mView.isActive()) {
                                mView.showErrorCrouton(shoppingCartListResponse.getStatus().getErrorMessages().get(0),
                                        false);
                            }
                        }

                    }
                });


    }

    @Override
    public void getBillingAddressById(String sessionConfig, String addressId) {
        mPetMedsApiService
                .getAddressById(sessionConfig,
                        addressId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AddAddressResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("GetAddressById", e.getMessage());
                        if (mView.isActive()) {
                            mView.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(AddAddressResponse s) {
                        if (s.getStatus().getCode().equals(API_SUCCESS_CODE)) {
                            if (mView.isActive()) {
                                mView.setUpdatedAddressOnSuccess(s.getProfileAddress());
                            }
                        } else {
                            Log.d("GetAddressById", s.getStatus().getErrorMessages().get(0));
                            if (mView.isActive()) {
                                mView.errorOnUpdateAddress();
                            }
                        }

                    }
                });
    }


    @Override
    public void checkoutPayPal(PayPalCheckoutRequest request) {
        mPetMedsApiServiceRedrirectOff.payPalCheckout(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("error",e.getLocalizedMessage());
                        mView.onPayPalError(e.getLocalizedMessage());

                    }

                    @Override
                    public void onNext(Response<String> s) {
                        Log.d("response", s + ">>>");
                        String loactionHeader=s.headers().get("Location");
                       if(loactionHeader==null || loactionHeader.isEmpty()){
                           mView.onPayPalError("");
                       }else{
                           mView.onSuccess(loactionHeader);
                       }

                    }
                });
    }

    @Override
    public void start() {

    }
}
