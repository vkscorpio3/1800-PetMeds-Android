package com.petmeds1800.ui.checkout.steponerootfragment;

import android.support.annotation.NonNull;

import com.petmeds1800.PetMedsApplication;
import com.petmeds1800.api.PetMedsApiService;
import com.petmeds1800.model.entities.SavedShippingAddressRequest;
import com.petmeds1800.model.shoppingcart.response.ShoppingCartListResponse;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Sdixit on 26-09-2016.
 */

public class StepOneRootPresentor implements StepOneRootContract.Presenter {

    private StepOneRootContract.View mView;
    @Inject
    PetMedsApiService mPetMedsApiService;

    public StepOneRootPresentor(@NonNull StepOneRootContract.View view) {
        mView = view;
        mView.setPresenter(this);
        PetMedsApplication.getAppComponent().inject(this);
    }


    @Override
    public void start() {

    }

    @Override
    public void saveShippingAddress(SavedShippingAddressRequest request) {
        mView.hideWarningView();
        mPetMedsApiService.saveShippingAddress(request)
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
                                mView.onSuccess(shoppingCartListResponse);
                            }
                        } else if (shoppingCartListResponse.getStatus().getCode().equals(API_WARNING_CODE)) {
                            mView.showWarningView(shoppingCartListResponse.getStatus().getErrorMessages().get(0));
                        } else {
                            if (mView.isActive()) {
                                mView.showErrorCrouton(shoppingCartListResponse.getStatus().getErrorMessages().get(0), false);
                            }
                        }

                    }
                });
    }
}
